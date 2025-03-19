package com.example.goalmate.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.goalmate.data.localdata.MotivationQuote
import com.google.firebase.database.FirebaseDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import com.example.goalmate.extrensions.QuoteFetchState
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import dagger.hilt.android.qualifiers.ApplicationContext

@HiltViewModel
class MotivationQuoteViewModel @Inject constructor(
    private val database: FirebaseDatabase,
    private val db: FirebaseFirestore,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _motivationQuote = MutableStateFlow<MotivationQuote?>(null)
    val motivationQuote: StateFlow<MotivationQuote?> = _motivationQuote.asStateFlow()

    private val _quoteFetchState = MutableStateFlow<QuoteFetchState>(QuoteFetchState.INITIAL)
    val quoteFetchState: StateFlow<QuoteFetchState> = _quoteFetchState.asStateFlow()


    private fun getRandomQuoteByCategory(category: String, callback: (MotivationQuote?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _quoteFetchState.value = QuoteFetchState.LOADING
                
                // Firebase'den kategori sözlerini çek
                val quotesRef = database.getReference("Kategoriler/$category")
                Log.d("MotivationQuoteViewModel", "Firebase'den $category kategorisi için sözler çekiliyor...")
                val snapshot = quotesRef.get().await()

                if (!snapshot.exists() || snapshot.childrenCount.toInt() == 0) {
                    Log.e("MotivationQuoteViewModel", "Bu kategoride söz bulunamadı: $category")
                    withContext(Dispatchers.Main) {
                        _quoteFetchState.value = QuoteFetchState.ERROR
                        callback(null)
                    }
                    return@launch
                }

                // Kategorideki toplam söz sayısı
                val quoteCount = snapshot.childrenCount.toInt()
                Log.d("MotivationQuoteViewModel", "$category kategorisinde $quoteCount söz bulundu")
                
                // Rastgele bir söz seç (0 ile quoteCount-1 arasında)
                val randomIndex = (0 until quoteCount).random()
                var currentIndex = 0
                var selectedQuote: MotivationQuote? = null
                
                // Seçilen indeksteki sözü bul
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
                
                if (selectedQuote != null) {
                    Log.d("MotivationQuoteViewModel", "Rastgele söz seçildi: ${selectedQuote?.quote}")
                    withContext(Dispatchers.Main) {
                        _motivationQuote.value = selectedQuote
                        _quoteFetchState.value = QuoteFetchState.SUCCESS
                        callback(selectedQuote)
                    }
                } else {
                    Log.e("MotivationQuoteViewModel", "Söz seçiminde hata oluştu")
                    withContext(Dispatchers.Main) {
                        _quoteFetchState.value = QuoteFetchState.ERROR
                        callback(null)
                    }
                }
            } catch (e: Exception) {
                Log.e("MotivationQuoteViewModel", "Söz çekerken hata: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    _quoteFetchState.value = QuoteFetchState.ERROR
                    callback(null)
                }
            }
        }
    }

    fun saveQuoteForGroup(groupId: String, category: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                withContext(Dispatchers.Main) {
                    _quoteFetchState.value = QuoteFetchState.LOADING
                }

                Log.d("MotivationQuoteViewModel", "Kategori için söz aranıyor: $category")

                // Firebase'den bir söz çek
                getRandomQuoteByCategory(category) { quote ->
                    viewModelScope.launch(Dispatchers.IO) {
                        if (quote == null) {
                            Log.e("MotivationQuoteViewModel", "Kategori için söz bulunamadı, varsayılan söz kullanılıyor")
                            val defaultQuote = "Hedeflerinize ulaşmak için her gün küçük adımlar atın."
                            db.collection("groups")
                                .document(groupId)
                                .update("quote", defaultQuote)
                                .await()
                        } else {
                            db.collection("groups")
                                .document(groupId)
                                .update("quote", quote.quote)
                                .await()
                            Log.d("MotivationQuoteViewModel", "Grup için söz kaydedildi: ${quote.quote}")
                        }

                        withContext(Dispatchers.Main) {
                            _quoteFetchState.value = QuoteFetchState.SUCCESS
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("MotivationQuoteViewModel", "Grup için söz kaydedilirken hata: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    _quoteFetchState.value = QuoteFetchState.ERROR
                }
            }
        }
    }
}