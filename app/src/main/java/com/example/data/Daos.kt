package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentDao {
    @Query("SELECT * FROM documents ORDER BY timestamp DESC")
    fun getAllDocuments(): Flow<List<Document>>

    @Query("SELECT * FROM documents WHERE id = :id")
    suspend fun getDocumentById(id: Int): Document?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(document: Document): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocuments(documents: List<Document>)

    @Update
    suspend fun updateDocument(document: Document)

    @Query("DELETE FROM documents WHERE id = :id")
    suspend fun deleteDocumentById(id: Int)

    @Query("DELETE FROM documents")
    suspend fun deleteAllDocuments()

    @Query("SELECT * FROM documents WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%' OR tags LIKE '%' || :query || '%'")
    suspend fun searchDocumentsLocal(query: String): List<Document>
}

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_entries WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getChatEntriesForSession(sessionId: String): Flow<List<ChatEntry>>

    @Query("SELECT sessionId FROM chat_entries GROUP BY sessionId ORDER BY MAX(timestamp) DESC")
    fun getDistinctSessionIds(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatEntry(entry: ChatEntry): Long

    @Update
    suspend fun updateChatEntry(entry: ChatEntry)

    @Query("DELETE FROM chat_entries WHERE sessionId = :sessionId")
    suspend fun deleteSessionEntries(sessionId: String)

    @Query("SELECT * FROM chat_entries WHERE isEvaluated = 1")
    fun getEvaluatedEntries(): Flow<List<ChatEntry>>

    @Query("DELETE FROM chat_entries")
    suspend fun deleteAllChatEntries()
}
