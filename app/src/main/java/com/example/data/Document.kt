package com.example.data

import androidx.room.*

@Entity(tableName = "documents")
data class Document(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val category: String,
    val tags: String, // Comma-separated list of tags
    val embeddingJson: String? = null, // serialized List<Float>
    val timestamp: Long = System.currentTimeMillis()
)
