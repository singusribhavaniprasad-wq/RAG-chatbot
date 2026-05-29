package com.example.network

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    val contents: List<GeminiContent>,
    val generationConfig: GeminiGenerationConfig? = null,
    val systemInstruction: GeminiContent? = null
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    val role: String? = null,
    val parts: List<GeminiPart>
)

@JsonClass(generateAdapter = true)
data class GeminiPart(
    val text: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiGenerationConfig(
    val temperature: Float? = null,
    val responseMimeType: String? = null,
    val responseSchema: GeminiSchema? = null
)

@JsonClass(generateAdapter = true)
data class GeminiSchema(
    val type: String, // "OBJECT", "ARRAY", "STRING", "NUMBER", "INTEGER", "BOOLEAN"
    val properties: Map<String, GeminiSchema>? = null,
    val items: GeminiSchema? = null,
    val description: String? = null,
    val required: List<String>? = null
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    val candidates: List<GeminiCandidate>?
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    val content: GeminiContent?
)

@JsonClass(generateAdapter = true)
data class EmbeddingRequest(
    val content: GeminiContent,
    val model: String = "models/gemini-embedding-2-preview"
)

@JsonClass(generateAdapter = true)
data class EmbeddingResponse(
    val embedding: GeminiValues?
)

@JsonClass(generateAdapter = true)
data class GeminiValues(
    val values: List<Float>?
)

@JsonClass(generateAdapter = true)
data class BatchEmbeddingRequest(
    val requests: List<EmbeddingRequest>
)

@JsonClass(generateAdapter = true)
data class BatchEmbeddingResponse(
    val embeddings: List<GeminiValues>?
)
