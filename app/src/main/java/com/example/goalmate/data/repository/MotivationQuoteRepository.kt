package com.example.goalmate.data.repository

import com.example.goalmate.data.localdata.MotivationQuote
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MotivationQuoteRepository @Inject constructor(
    private val firebaseDatabase: FirebaseDatabase
) {
    // Firebase'den belirli bir kategori için rastgele bir söz alır
    suspend fun getRandomQuoteByCategory(category: String): Flow<MotivationQuote?> = flow {
        try {
            // Firebase'den kategori sözlerini çek
            val snapshot = withContext(Dispatchers.IO) {
                firebaseDatabase.getReference("Kategoriler/$category").get().await()
            }
            
            if (!snapshot.exists() || snapshot.childrenCount.toInt() == 0) {
                emit(null)
                return@flow
            }
            
            // Rastgele bir söz seç
            val quoteCount = snapshot.childrenCount.toInt()
            val randomIndex = (0 until quoteCount).random()
            var currentIndex = 0
            var selectedQuote: MotivationQuote? = null
            
            snapshot.children.forEach { quoteSnapshot ->
                if (currentIndex == randomIndex) {
                    val quoteId = quoteSnapshot.key ?: ""
                    val quoteText = quoteSnapshot.getValue(String::class.java) ?: ""
                    selectedQuote = MotivationQuote(
                        id = quoteId,
                        quote = quoteText,
                        category = category
                    )
                    return@forEach
                }
                currentIndex++
            }
            
            emit(selectedQuote)
        } catch (e: Exception) {
            emit(null)
        }
    }
    
    // Kategori listesini getirir
    suspend fun getAllCategories(): List<String> {
        return try {
            val snapshot = withContext(Dispatchers.IO) {
                firebaseDatabase.getReference("Kategoriler").get().await()
            }
            
            if (!snapshot.exists()) {
                emptyList()
            } else {
                snapshot.children.mapNotNull { it.key }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}