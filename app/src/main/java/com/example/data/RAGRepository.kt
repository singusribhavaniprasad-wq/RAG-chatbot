package com.example.data

import android.content.Context
import android.util.Log
import com.example.network.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONObject
import kotlin.math.sqrt

data class EvaluationResult(
    val tags: String,
    val contextRelevance: Float,
    val faithfulness: Float,
    val isHallucinated: Boolean,
    val answerRelevance: Float
)

class RAGRepository(context: Context) {
    private val database = AppDatabase.getDatabase(context)
    private val documentDao = database.documentDao()
    private val chatDao = database.chatDao()
    private val apiService = RetrofitClient.geminiApi

    val allDocuments: Flow<List<Document>> = documentDao.getAllDocuments()
    val distinctSessions: Flow<List<String>> = chatDao.getDistinctSessionIds()
    val evaluatedEntries: Flow<List<ChatEntry>> = chatDao.getEvaluatedEntries()

    suspend fun prepopulateIfEmpty() = withContext(Dispatchers.IO) {
        val documents = DefaultKnowledgeBase.getDefaultDocuments()
        // Determine emptiness via local search or counts
        val existing = documentDao.searchDocumentsLocal("")
        if (existing.size < 50) {
            documentDao.deleteAllDocuments()
            documentDao.insertDocuments(documents)
            Log.d("RAGRepository", "Knowledge base pre-populated with ${documents.size} entries.")
        }
    }

    suspend fun insertDocument(doc: Document): Long = withContext(Dispatchers.IO) {
        documentDao.insertDocument(doc)
    }

    suspend fun deleteDocument(id: Int) = withContext(Dispatchers.IO) {
        documentDao.deleteDocumentById(id)
    }

    fun getChatEntries(sessionId: String): Flow<List<ChatEntry>> {
        return chatDao.getChatEntriesForSession(sessionId)
    }

    suspend fun insertChatEntry(entry: ChatEntry): Long = withContext(Dispatchers.IO) {
        chatDao.insertChatEntry(entry)
    }

    suspend fun updateChatEntry(entry: ChatEntry) = withContext(Dispatchers.IO) {
        chatDao.updateChatEntry(entry)
    }

    suspend fun deleteSession(sessionId: String) = withContext(Dispatchers.IO) {
        chatDao.deleteSessionEntries(sessionId)
    }

    suspend fun clearAllData() = withContext(Dispatchers.IO) {
        chatDao.deleteAllChatEntries()
        documentDao.deleteAllDocuments()
        prepopulateIfEmpty()
    }

    // --- EMBEDDINGS (CLOUD DENSE VECTOR) ---
    
    // Serialise List<Float> to Comma Separated Values
    private fun serializeVector(vec: List<Float>): String = vec.joinToString(",")
    
    // Deserialise CSS to List<Float>
    private fun deserializeVector(json: String?): List<Float> {
        if (json.isNullOrEmpty()) return emptyList()
        return json.split(",").mapNotNull { it.toFloatOrNull() }
    }

    // Generates embeddings for a single document on ingestion
    suspend fun generateEmbeddingForDocument(apiKey: String, doc: Document): Document = withContext(Dispatchers.IO) {
        if (apiKey.trim().isEmpty()) return@withContext doc
        try {
            val contentStr = "${doc.title}\nCategory: ${doc.category}\nTags: ${doc.tags}\nContent: ${doc.content}"
            val request = EmbeddingRequest(
                content = GeminiContent(parts = listOf(GeminiPart(text = contentStr)))
            )
            val response = apiService.embedContent(apiKey, request)
            val vector = response.embedding?.values
            if (vector != null) {
                return@withContext doc.copy(embeddingJson = serializeVector(vector))
            }
        } catch (e: Exception) {
            Log.e("RAGRepository", "Embedding single doc failed: ${e.message}")
        }
        return@withContext doc
    }

    // Batch generate embeddings in chunks of 15-20 to fit payload and limits
    suspend fun batchEmbedAllDocuments(
        apiKey: String,
        documents: List<Document>,
        onProgress: (Int, Int) -> Unit
    ): Int = withContext(Dispatchers.IO) {
        if (apiKey.trim().isEmpty() || documents.isEmpty()) return@withContext 0
        var successCount = 0
        val chunkSize = 15
        val chunks = documents.chunked(chunkSize)
        val totalChunks = chunks.size

        for (i in chunks.indices) {
            val chunk = chunks[i]
            onProgress(successCount, documents.size)

            val requestsList = chunk.map { doc ->
                val textToEmbed = "${doc.title}\nCategory: ${doc.category}\nTags: ${doc.tags}\nContent: ${doc.content}"
                EmbeddingRequest(
                    content = GeminiContent(parts = listOf(GeminiPart(text = textToEmbed))),
                    model = "models/gemini-embedding-2-preview"
                )
            }

            try {
                val batchResponse = apiService.batchEmbedContents(apiKey, BatchEmbeddingRequest(requestsList))
                val embeddingsList = batchResponse.embeddings
                if (embeddingsList != null && embeddingsList.size == chunk.size) {
                    val updatedDocs = chunk.mapIndexed { index, doc ->
                        val vector = embeddingsList[index].values
                        if (vector != null) {
                            successCount++
                            doc.copy(embeddingJson = serializeVector(vector))
                        } else {
                            doc
                        }
                    }
                    documentDao.insertDocuments(updatedDocs)
                } else {
                    Log.e("RAGRepository", "Batch response embeddings sizes mismatch or null")
                }
            } catch (e: Exception) {
                Log.e("RAGRepository", "Error embedding batch chunk $i: ${e.message}")
            }
        }
        onProgress(successCount, documents.size)
        return@withContext successCount
    }

    // --- RETRIEVAL ENGINE (TF-IDF AND DENSE VECTOR) ---

    // Clean text: lowercase and remove punctuation
    private fun cleanNTokenize(text: String): List<String> {
        return text.lowercase()
            .replace(Regex("[^a-zA-Z0-9 ]"), "")
            .split(Regex("\\s+"))
            .filter { it.length > 2 }
    }

    // Mathematical local TF-IDF Cosine Similarity Search
    suspend fun retrieveLocalTfIdf(query: String, topK: Int = 3): List<Pair<Document, Float>> = withContext(Dispatchers.Default) {
        val documents = documentDao.searchDocumentsLocal("")
        if (documents.isEmpty()) return@withContext emptyList()

        val queryTokens = cleanNTokenize(query)
        if (queryTokens.isEmpty()) {
            return@withContext documents.take(topK).map { it to 0.1f }
        }

        // Tokenize search fields
        val corpusTokens = documents.map { doc ->
            cleanNTokenize("${doc.title} ${doc.category} ${doc.tags} ${doc.content}")
        }

        // Gather dictionary
        val allUniqueTerms = (queryTokens + corpusTokens.flatten()).distinct()
        val termToIndex = allUniqueTerms.withIndex().associate { it.value to it.index }
        val dimensions = allUniqueTerms.size

        // Calculate IDF scores
        val docCount = documents.size.toFloat()
        val idf = FloatArray(dimensions)
        for (i in 0 until dimensions) {
            val term = allUniqueTerms[i]
            val docsContaining = corpusTokens.count { term in it }
            // Smooth natural log IDF calculation
            idf[i] = kotlin.math.ln(((1.0 + docCount) / (1.0 + docsContaining))).toFloat() + 1f
        }

        // Build TF-IDF vector
        fun tfIdfVector(tokens: List<String>): FloatArray {
            val vector = FloatArray(dimensions)
            val termCounts = tokens.groupingBy { it }.eachCount()
            val maxCount = termCounts.values.maxOrNull()?.toFloat() ?: 1f
            for ((term, count) in termCounts) {
                val index = termToIndex[term] ?: continue
                val tf = count.toFloat() / maxCount
                vector[index] = tf * idf[index]
            }
            return vector
        }

        val queryVector = tfIdfVector(queryTokens)

        fun computeCosineSimilarity(v1: FloatArray, v2: FloatArray): Float {
            var dot = 0f
            var normA = 0f
            var normB = 0f
            for (i in 0 until dimensions) {
                dot += v1[i] * v2[i]
                normA += v1[i] * v1[i]
                normB += v2[i] * v2[i]
            }
            if (normA == 0f || normB == 0f) return 0f
            return dot / (sqrt(normA) * sqrt(normB))
        }

        val results = documents.mapIndexed { idx, doc ->
            val docVector = tfIdfVector(corpusTokens[idx])
            val similarity = computeCosineSimilarity(queryVector, docVector)
            doc to similarity
        }

        return@withContext results
            .filter { it.second > 0.05f }
            .sortedByDescending { it.second }
            .take(topK)
    }

    // Dense Vector Cosine Similarity Search
    suspend fun retrieveDenseVector(
        apiKey: String,
        query: String,
        topK: Int = 3
    ): Pair<List<Pair<Document, Float>>, String?> = withContext(Dispatchers.IO) {
        if (apiKey.trim().isEmpty()) {
            return@withContext Pair(emptyList(), "Gemini API Key is empty. Provide a key to run Dense Vector retrieval.")
        }

        val queryRequest = EmbeddingRequest(
            content = GeminiContent(parts = listOf(GeminiPart(text = query)))
        )

        val queryVector: List<Float>
        try {
            val response = apiService.embedContent(apiKey, queryRequest)
            queryVector = response.embedding?.values ?: return@withContext Pair(emptyList(), "Embeddings API returned empty values.")
        } catch (e: Exception) {
            return@withContext Pair(emptyList(), "Network Error calculating query embedding: ${e.message}")
        }

        val allDocs = documentDao.searchDocumentsLocal("")
        val embeddedDocs = allDocs.filter { !it.embeddingJson.isNullOrEmpty() }

        if (embeddedDocs.isEmpty()) {
            return@withContext Pair(
                emptyList(),
                "No documents possess calculated dense embeddings. Click 'Precompute Embeddings' in the Knowledge Base tab."
            )
        }

        val queryVectorArray = queryVector.toFloatArray()

        fun cosineSim(v1: FloatArray, v2: List<Float>): Float {
            if (v1.size != v2.size || v1.isEmpty()) return 0f
            var dot = 0f
            var normA = 0f
            var normB = 0f
            for (i in v1.indices) {
                val f1 = v1[i]
                val f2 = v2[i]
                dot += f1 * f2
                normA += f1 * f1
                normB += f2 * f2
            }
            if (normA == 0f || normB == 0f) return 0f
            return dot / (sqrt(normA) * sqrt(normB))
        }

        val results = embeddedDocs.map { doc ->
            val docVec = deserializeVector(doc.embeddingJson)
            val sim = cosineSim(queryVectorArray, docVec)
            doc to sim
        }

        val topHits = results
            .sortedByDescending { it.second }
            .take(topK)

        val warning = if (embeddedDocs.size < allDocs.size) {
            "Note: Dense search ran on ${embeddedDocs.size}/${allDocs.size} documents. Remaining entities do not have embeddings."
        } else {
            null
        }

        return@withContext Pair(topHits, warning)
    }

    // --- ANSWER GENERATION (Grounded Prompting) ---
    
    suspend fun generateGroundedResponse(
        apiKey: String,
        query: String,
        retrievedDocs: List<Document>
    ): String = withContext(Dispatchers.IO) {
        if (apiKey.trim().isEmpty()) {
            return@withContext "API Key missing! Configure your API key securely via the Secrets panel in AI Studio."
        }

        val systemInstructionMsg = """
            You are DocuRAG, an advanced domain-specific Q&A assistant for Android & Kotlin developers.
             Your job is to answer user questions strictly using retrieved knowledge base documents.
             
            RULES:
            1. Use retrieved context to synthesize grounded, concise, and professional explanations.
            2. Fully cite retrieved documents as primary sources (e.g., [State Hoisting in Jetpack Compose]).
            3. If the retrieved documents do not contain relevant facts to satisfy the query, you MUST write exactly:
               "The database does not contain sufficient information to satisfy this query."
            4. ABSOLUTELY DO NOT hallucinate, invent, assume, or provide specifications outside of the provided text.
        """.trimIndent()

        val contextString = if (retrievedDocs.isEmpty()) {
            "NO RELEVANT CONTEXT RETRIEVED."
        } else {
            retrievedDocs.joinToString("\n\n") { doc ->
                "--- DOCUMENT: ${doc.title} ---\nCategory: ${doc.category}\nTags: ${doc.tags}\nContent: ${doc.content}"
            }
        }

        val promptStr = """
            [RETRIEVED CONTEXT]
            $contextString
            
            [USER QUESTION]
            $query
            
            Grounded Answer (remember: strictly limit details to facts mentioned in retrieved context):
        """.trimIndent()

        val request = GeminiRequest(
            contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = promptStr)))),
            systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = systemInstructionMsg))),
            generationConfig = GeminiGenerationConfig(temperature = 0.2f)
        )

        try {
            val response = apiService.generateContent(apiKey, request)
            return@withContext response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "Empty response from Gemini."
        } catch (e: Exception) {
            return@withContext "AI Response Error: ${e.message}"
        }
    }

    // --- EVALUATION AND AUTO-TAGGING PIPELINE (LLM-as-a-Judge) ---

    suspend fun evaluateAndTagResponse(
        apiKey: String,
        query: String,
        retrievedDocs: List<Document>,
        answer: String
    ): EvaluationResult = withContext(Dispatchers.IO) {
        if (apiKey.trim().isEmpty() || answer.startsWith("API Key missing") || answer.startsWith("AI Response Error")) {
            return@withContext EvaluationResult(
                tags = "unclassified",
                contextRelevance = 0f,
                faithfulness = 0f,
                isHallucinated = false,
                answerRelevance = 0f
            )
        }

        val contextText = retrievedDocs.joinToString("\n\n") { doc ->
            "Title: ${doc.title}\nTags: ${doc.tags}\nContent: ${doc.content}"
        }

        val evalSystemInstruction = """
            You are systemic LLM-as-a-Judge.
            Your job is to analyze RAG outputs: Query, Retrieved Context, and Response, and write structural JSON metrics.
            You must output a single raw compact JSON object exactly matching this schema:
            {
               "tags": "tag1, tag2, tag3, tag4",  // Generate 4-8 comma-separated highly descriptive technical tags regarding topics, issues, terminology
               "context_relevance": 0.85,          // 0.0 to 1.0 (how relevant and helpful are retrieved documents to answer user query)
               "faithfulness": 0.90,               // 0.0 to 1.0 (is answer fully grounded in, and supported by context docs without extra assumptions)
               "is_hallucinated": false,           // true/false (true if answer claims facts, versions, packages or tips not stated in context)
               "answer_relevance": 0.95            // 0.0 to 1.0 (does answer address user query directly, helpful, concise)
            }
            Do not include any surrounding markdown syntax (such as ```json) or explanation. Output ONLY the raw JSON string.
        """.trimIndent()

        val evaluationPrompt = """
            [USER QUERY]
            $query

            [RETIREVED CONTEXT]
            $contextText

            [GENERATED ANSWER]
            $answer

            JSON Metrics Output:
        """.trimIndent()

        val request = GeminiRequest(
            contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = evaluationPrompt)))),
            systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = evalSystemInstruction))),
            generationConfig = GeminiGenerationConfig(temperature = 0.1f)
        )

        try {
            val response = apiService.generateContent(apiKey, request)
            val jsonStr = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text?.trim()
                ?: "{}"
            
            // Clean markdown wrapper if any
            val cleanedJson = jsonStr.replace("```json", "").replace("```", "").trim()
            val json = JSONObject(cleanedJson)

            return@withContext EvaluationResult(
                tags = json.optString("tags", "general, helper"),
                contextRelevance = json.optDouble("context_relevance", 0.5).toFloat(),
                faithfulness = json.optDouble("faithfulness", 0.5).toFloat(),
                isHallucinated = json.optBoolean("is_hallucinated", false),
                answerRelevance = json.optDouble("answer_relevance", 0.5).toFloat()
            )
        } catch (e: Exception) {
            Log.e("RAGRepository", "Evaluation API parse failed: ${e.message}")
            // Fallback tags via offline analysis
            val tokens = cleanNTokenize(query + " " + answer)
            val computedTags = tokens.take(5).joinToString(", ")
            return@withContext EvaluationResult(
                tags = computedTags.ifEmpty { "general" },
                contextRelevance = 0.5f,
                faithfulness = 0.5f,
                isHallucinated = false,
                answerRelevance = 0.5f
            )
        }
    }
}
