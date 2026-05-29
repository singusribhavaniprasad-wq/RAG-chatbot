package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

enum class SearchMode {
    LOCAL_TFIDF,
    CLOUD_DENSE
}

sealed interface RAGState {
    object Idle : RAGState
    object Retrieving : RAGState
    object Generating : RAGState
    object Evaluating : RAGState
    data class Error(val message: String) : RAGState
}

class RAGViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = RAGRepository(application)

    // --- State Holders ---
    val allDocuments: StateFlow<List<Document>> = repository.allDocuments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val distinctSessions: StateFlow<List<String>> = repository.distinctSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedSessionId = MutableStateFlow("")
    val selectedSessionId: StateFlow<String> = _selectedSessionId.asStateFlow()

    private val _searchMode = MutableStateFlow(SearchMode.LOCAL_TFIDF)
    val searchMode: StateFlow<SearchMode> = _searchMode.asStateFlow()

    private val _ragState = MutableStateFlow<RAGState>(RAGState.Idle)
    val ragState: StateFlow<RAGState> = _ragState.asStateFlow()

    // Active session entries
    val chatEntries: StateFlow<List<ChatEntry>> = _selectedSessionId
        .flatMapLatest { sessionId ->
            if (sessionId.isEmpty()) flowOf(emptyList())
            else repository.getChatEntries(sessionId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // App API Key State
    private val _apiKey = MutableStateFlow(BuildConfig.GEMINI_API_KEY)
    val apiKey: StateFlow<String> = _apiKey.asStateFlow()

    // Last Turn grounding state
    private val _lastRetrievedDocs = MutableStateFlow<List<Pair<Document, Float>>>(emptyList())
    val lastRetrievedDocs: StateFlow<List<Pair<Document, Float>>> = _lastRetrievedDocs.asStateFlow()

    private val _denseWarning = MutableStateFlow<String?>(null)
    val denseWarning: StateFlow<String?> = _denseWarning.asStateFlow()

    // Bulk Embedding Progress
    private val _embeddingProgress = MutableStateFlow(Pair(0, 0)) // Current, Total
    val embeddingProgress: StateFlow<Pair<Int, Int>> = _embeddingProgress.asStateFlow()

    private val _isEmbedding = MutableStateFlow(false)
    val isEmbedding: StateFlow<Boolean> = _isEmbedding.asStateFlow()

    // Historical evaluations for metrics graphs
    val evaluatedEntries: StateFlow<List<ChatEntry>> = repository.evaluatedEntries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            repository.prepopulateIfEmpty()
            
            // Set initial session
            repository.distinctSessions.firstOrNull()?.firstOrNull()?.let {
                _selectedSessionId.value = it
            } ?: run {
                createNewSession()
            }
        }
    }

    fun updateApiKey(key: String) {
        _apiKey.value = key
    }

    fun toggleSearchMode(mode: SearchMode) {
        _searchMode.value = mode
    }

    fun selectSession(sessionId: String) {
        _selectedSessionId.value = sessionId
    }

    fun createNewSession() {
        val newId = "session_" + UUID.randomUUID().toString().take(8)
        _selectedSessionId.value = newId
    }

    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            repository.deleteSession(sessionId)
            if (_selectedSessionId.value == sessionId) {
                val sessions = distinctSessions.value.filter { it != sessionId }
                if (sessions.isNotEmpty()) {
                    _selectedSessionId.value = sessions.first()
                } else {
                    createNewSession()
                }
            }
        }
    }

    // --- RAG WORKFLOW ---
    fun askQuestion(query: String) {
        if (query.trim().isEmpty()) return
        val currentSession = _selectedSessionId.value
        if (currentSession.isEmpty()) return

        viewModelScope.launch {
            // 1. Insert user message in database
            val userEntry = ChatEntry(
                sessionId = currentSession,
                role = "user",
                content = query
            )
            repository.insertChatEntry(userEntry)

            _ragState.value = RAGState.Retrieving
            _lastRetrievedDocs.value = emptyList()
            _denseWarning.value = null

            // 2. Perform Retrieval
            val retrieved: List<Pair<Document, Float>>
            var warning: String? = null

            if (_searchMode.value == SearchMode.LOCAL_TFIDF) {
                retrieved = repository.retrieveLocalTfIdf(query, topK = 3)
            } else {
                val (hits, warn) = repository.retrieveDenseVector(_apiKey.value, query, topK = 3)
                retrieved = hits
                _denseWarning.value = warn
                warning = warn
            }

            _lastRetrievedDocs.value = retrieved
            val docEntities = retrieved.map { it.first }

            // Calculate retrieval Precision@K and MRR based on exact tag match or category overlap
            // Since this is evaluated programmatically, we check if the query contains words matching document tags
            val k = retrieved.size
            val relevantCount = retrieved.count { (doc, score) ->
                doc.tags.split(",").map { it.trim() }.any { tag -> query.contains(tag, ignoreCase = true) } || score > 0.35f
            }
            val precisionAtK = if (k > 0) relevantCount.toFloat() / k else 0f
            val recallAtK = if (documentsWithQueryMatchCount(query) > 0) relevantCount.toFloat() / documentsWithQueryMatchCount(query) else 1f
            
            // Reciprocal Rank calculation
            val firstRelevantIdx = retrieved.indexOfFirst { (doc, score) ->
                doc.tags.split(",").map { it.trim() }.any { tag -> query.contains(tag, ignoreCase = true) } || score > 0.35f
            }
            val mrr = if (firstRelevantIdx != -1) 1f / (firstRelevantIdx + 1f) else 0f

            _ragState.value = RAGState.Generating

            // 3. Generate Grounded Answer
            val answer = if (warning != null && retrieved.isEmpty()) {
                "Retrieval Error: $warning\n\nPlease ensure your API Key is valid and you have precomputed document embeddings in the 'Knowledge Base' tab."
            } else {
                repository.generateGroundedResponse(_apiKey.value, query, docEntities)
            }

            _ragState.value = RAGState.Evaluating

            // 4. Auto-tag and Evaluate response (LLM-as-a-Judge)
            val eval = repository.evaluateAndTagResponse(_apiKey.value, query, docEntities, answer)

            // 5. Save model response with metrics
            val modelEntry = ChatEntry(
                sessionId = currentSession,
                role = "model",
                content = answer,
                retrievedDocsJson = docEntities.joinToString("|") { it.title },
                qnaTags = eval.tags,
                isEvaluated = true,
                precisionAtK = precisionAtK,
                recallAtK = recallAtK,
                mrrScore = mrr,
                faithfulnessScore = eval.faithfulness,
                relevanceScore = eval.answerRelevance,
                hallucinationRate = if (eval.isHallucinated) 1f else 0f
            )
            repository.insertChatEntry(modelEntry)
            _ragState.value = RAGState.Idle
        }
    }

    private fun documentsWithQueryMatchCount(query: String): Int {
        val total = allDocuments.value
        if (total.isEmpty()) return 1
        return total.count { doc ->
            doc.tags.split(",").map { it.trim() }.any { tag -> query.contains(tag, ignoreCase = true) }
        }.coerceAtLeast(1)
    }

    // --- DYNAMIC DOCUMENT INGESTION ---
    fun ingestCustomDocument(title: String, category: String, content: String, onComplete: (Boolean) -> Unit) {
        if (title.trim().isEmpty() || content.trim().isEmpty()) {
            onComplete(false)
            return
        }

        viewModelScope.launch {
            // First create a temporary Document
            var tempDoc = Document(
                title = title,
                category = category,
                tags = "user-ingested",
                content = content
            )

            // Auto-tag the newly ingested document using Gemini
            _ragState.value = RAGState.Evaluating
            try {
                val key = _apiKey.value
                if (key.trim().isNotEmpty()) {
                    // Call Gemini to auto-generate tags
                    val evalResult = repository.evaluateAndTagResponse(
                        key, 
                        "Tag this text",
                        emptyList(),
                        "Title: $title\nCategory: $category\nContent: $content"
                    )
                    tempDoc = tempDoc.copy(tags = evalResult.tags)
                }
            } catch (e: Exception) {
                // Keep default tag
            }

            // Generate cloud embedding if API is working
            try {
                val key = _apiKey.value
                if (key.trim().isNotEmpty()) {
                    tempDoc = repository.generateEmbeddingForDocument(key, tempDoc)
                }
            } catch (e: Exception) {
                // Save without embedding
            }

            repository.insertDocument(tempDoc)
            _ragState.value = RAGState.Idle
            onComplete(true)
        }
    }

    fun deleteDocument(id: Int) {
        viewModelScope.launch {
            repository.deleteDocument(id)
        }
    }

    // Bulk precomputation of embeddings
    fun startBulkEmbedding() {
        val docs = allDocuments.value
        if (docs.isEmpty()) return
        val key = _apiKey.value
        if (key.trim().isEmpty()) {
            _ragState.value = RAGState.Error("API Key is missing. Verify in prompt secrets.")
            return
        }

        _isEmbedding.value = true
        _embeddingProgress.value = Pair(0, docs.size)

        viewModelScope.launch {
            try {
                val count = repository.batchEmbedAllDocuments(key, docs) { current, total ->
                    _embeddingProgress.value = Pair(current, total)
                }
                _ragState.value = RAGState.Idle
            } catch (e: Exception) {
                _ragState.value = RAGState.Error("Bulk Embedding Failed: ${e.message}")
            } finally {
                _isEmbedding.value = false
            }
        }
    }

    fun resetAllData() {
        viewModelScope.launch {
            repository.clearAllData()
        }
    }
}

class RAGViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RAGViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RAGViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
