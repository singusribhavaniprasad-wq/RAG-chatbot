package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_entries")
data class ChatEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sessionId: String,
    val role: String, // "user" or "model"
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    
    // RAG Metadata
    val retrievedDocsJson: String? = null, // JSON of List<String> or titles used
    val qnaTags: String? = null, // comma-separated tags for this Q&A entry
    
    // Evaluations
    val isEvaluated: Boolean = false,
    val precisionAtK: Float = 0f,
    val recallAtK: Float = 0f,
    val mrrScore: Float = 0f,
    val faithfulnessScore: Float = 0f,
    val relevanceScore: Float = 0f,
    val hallucinationRate: Float = 0f // 0f means no hallucination, 1f means hallucinated
)
