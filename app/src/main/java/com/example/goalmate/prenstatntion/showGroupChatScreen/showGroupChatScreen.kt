package com.example.goalmate.prenstatntion.showGroupChatScreen

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.goalmate.R
import com.example.goalmate.data.localdata.ChatMessage
import com.example.goalmate.viewmodel.GroupsAddViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.collectAsState
import com.example.goalmate.extrensions.MessagesState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.width
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.layout.imePadding
import androidx.compose.ui.platform.LocalContext
import android.os.Build
import androidx.annotation.RequiresApi

/**
 * Mesaj veri sınıfı - Her bir mesajın özelliklerini içerir
 * @param id Mesaj benzersiz kimliği
 * @param senderId Gönderen kullanıcı kimliği
 * @param senderName Gönderen kullanıcı adı
 * @param message Mesaj içeriği
 * @param timestamp Mesaj gönderim zamanı (milisaniye)
 * @param isCurrentUser Mesajın şu anki kullanıcıya ait olup olmadığı
 */


/**
 * Grup sohbet ekranı - Gruba ait mesajları gösterir ve mesaj gönderme işlevini sağlar
 * @param navController Ekranlar arası geçiş için kullanılan navigasyon kontrolcüsü
 */
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowGroupChatScreen(
    groupedId : String,
    navController: NavController,
    groupsAddViewModel: GroupsAddViewModel
){
    // Mesaj girişi için durum değişkeni
    var messageText by remember { mutableStateOf("") }

    // kullanıcı profilini al
    val userProfile by groupsAddViewModel.profileImages.collectAsState()
    
    val context = LocalContext.current

    // Mesajları dinlemeye başla - Effect kullanımı
    LaunchedEffect(key1 = groupedId) {
        groupsAddViewModel.getGroupMessages(groupedId, context)
    }
    
    // Schedule periodic message cleanup
    LaunchedEffect(Unit) {
        groupsAddViewModel.scheduleMessageCleanup(context)
    }

    
    // Mesaj listesini al
    val messages by groupsAddViewModel.messages.collectAsState()
    
    // Mesaj durumunu takip et (yükleme, hata, vb.)
    val messagesState by groupsAddViewModel.chatMessage.collectAsState()

    Log.e("groupeId","groupeId = $groupedId")

    Scaffold (
        // Üst Bar - Başlık ve butonları içerir
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colorResource(id = R.color.arkaplan))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 22.dp, bottom = 8.dp, start = 16.dp, end = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Sol tarafta geri butonu ve grup başlığı
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Geri butonu - Önceki ekrana dönüş sağlar
                        IconButton(
                            onClick = {
                                navController.popBackStack()
                            },
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_back),
                                contentDescription = "Geri",
                                tint = colorResource(R.color.yazirengi)
                            )
                        }

                        // Grup adı başlığı
                        Text(
                            text = "Spor Yapılacak",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily(Font(R.font.noto_regular)),
                                fontSize = 22.sp
                            ),
                            color = colorResource(id = R.color.yazirengi)
                        )


                    }
                    
                    // Sağ tarafta liderlik tablosu ve menü simgeleri
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Liderlik tablosu butonu - Üyelerin skorlarını gösterir
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(colorResource(id = R.color.yazirengi))
                                .border(
                                    width = 1.dp,
                                    color = colorResource(id = R.color.kutubordrengi),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            IconButton(
                                onClick = {
                                    /* Liderlik tablosunu açma işlevi */
                                },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.leaderboard),
                                    contentDescription = "Liderlik Tablosu",
                                    tint = colorResource(R.color.beyaz),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        
                        // Üç nokta menü butonu - Ek işlemleri gösterir
                        IconButton(
                            onClick = {
                            /* Menü açma işlevi */
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Diğer Seçenekler",
                                tint = colorResource(R.color.yazirengi)
                            )
                        }
                    }
                }
                HorizontalDivider(
                    thickness = 1.dp,
                    color = colorResource(id = R.color.kutubordrengi).copy(alpha = 0.2f)
                )
            }
        },
        // Alt Bar - Mesaj yazma alanı ve gönder butonunu içerir
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colorResource(id = R.color.arkaplan))
                    .imePadding()
            ) {
                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = colorResource(id = R.color.kutubordrengi).copy(alpha = 0.1f)
                )
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Mesaj girdi alanı - Kullanıcının mesaj yazabileceği alan
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 0.dp
                        )
                    ) {
                        OutlinedTextField(
                            value = messageText,
                            onValueChange = { messageText = it },
                            placeholder = { 
                                Text(
                                    "Gruba mesaj yaz...",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = 14.sp
                                    )
                                ) 
                            },
                            modifier = Modifier
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                disabledContainerColor = Color.White,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                cursorColor = colorResource(id = R.color.kutubordrengi),
                                focusedTextColor = colorResource(id = R.color.yazirengi),
                                unfocusedTextColor = colorResource(id = R.color.yazirengi)
                            ),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 14.sp,
                                fontFamily = FontFamily(Font(R.font.noto_regular))
                            ),
                            maxLines = 4,
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Send
                            ),
                            keyboardActions = KeyboardActions(
                                onSend = {
                                    if (messageText.isNotBlank()) {
                                        // Mevcut kullanıcı bilgilerini al
                                        val currentUserId = groupsAddViewModel.getCurrentUserId() ?: return@KeyboardActions
                                        val currentUserName = groupsAddViewModel.getCurrentUserName() ?: "Misafir"
                                        
                                        // Mesajı Firebase'e gönder
                                        groupsAddViewModel.createMessagesFirebase(
                                            groupId = groupedId,
                                            senderId = currentUserId,
                                            senderName = currentUserName,
                                            message = messageText,
                                            isCurrentUser = true,
                                            context = context
                                        )
                                        
                                        // Mesaj alanını temizle
                                        messageText = ""
                                    }
                                }
                            )
                        )
                    }
                    
                    // Gönder butonu - Yazılan mesajı gönderir
                    IconButton(
                        onClick = { 
                            if (messageText.isNotBlank()) {
                                // Mevcut kullanıcı bilgilerini al
                                val currentUserId = groupsAddViewModel.getCurrentUserId() ?: return@IconButton
                                val currentUserName = groupsAddViewModel.getCurrentUserName() ?: "Misafir"
                                
                                // Mesajı Firebase'e gönder
                                groupsAddViewModel.createMessagesFirebase(
                                    groupId = groupedId,
                                    senderId = currentUserId,
                                    senderName = currentUserName,
                                    message = messageText,
                                    isCurrentUser = true,
                                    context = context
                                )
                                
                                // Mesaj alanını temizle
                                messageText = ""
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(colorResource(id = R.color.kutubordrengi))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Mesaj Gönder",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        // Ana içerik alanı - Mesajların görüntülendiği yer
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colorResource(id = R.color.arkaplan))
                .padding(paddingValues)
        ) {
            // Mesaj listesi
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize(),
                reverseLayout = false, // Eski mesajlar üstte, yeniler altta
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                // Tarih ayırıcısı - Basitleştirilmiş bir şekilde bugün olarak gösteriliyor
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            modifier = Modifier.padding(vertical = 4.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = colorResource(id = R.color.kutubordrengi).copy(alpha = 0.1f)
                            )
                        ) {
                            Text(
                                text = "Bugün",
                                style = MaterialTheme.typography.bodySmall,
                                color = colorResource(id = R.color.yazirengi),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                // Bilgilendirme mesajı - 24 saat sonra mesajların silineceğini belirt
                item {
                    Text(
                        text = "Mesajlar gönderildikten 24 saat sonra otomatik olarak silinir",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 10.sp,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        ),
                        color = colorResource(id = R.color.yazirengi).copy(alpha = 0.5f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                }
                
                // Mesajları listele
                items(messages) { message ->
                    MessageItem(message, userProfile)
                }
                
                // Mesajlar arasında görünmeyen bir boşluk - klavye açıldığında mesajların klavyenin üstünde kalmasını sağlar
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
            
            // Yükleme durumu
            when (messagesState) {
                is MessagesState.Loading -> {
                    // Yükleniyor göstergesi
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = colorResource(id = R.color.kutubordrengi)
                        )
                    }
                }
                is MessagesState.Error -> {
                    // Hata mesajı
                    val errorMessage = (messagesState as MessagesState.Error).message
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = errorMessage,
                            color = Color.Red,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
                else -> { /* Mesajlar zaten gösteriliyor */ }
            }
        }
    }
}

/**
 * Mesaj Öğesi - Bir mesajı görsel olarak gösterir
 * @param message Görüntülenecek mesaj bilgisi
 */
@Composable
fun MessageItem(
    message: ChatMessage,
    userProfiles: Map<String, String> = emptyMap()
) {
    // Mesajın hizalama yönü (kendi mesajlarımız sağda, diğerleri solda)
    val alignment = if (message.isCurrentUser) Alignment.End else Alignment.Start
    
    // Mesaj arka plan rengi (kendi mesajlarımız hafif mavi, diğerleri beyaz)
    val backgroundColor = if (message.isCurrentUser) 
        colorResource(id = R.color.kutubordrengi).copy(alpha = 0.2f) 
    else 
        Color.White
    
    // Mesaj yazı rengi (her ikisi için de koyu renk)
    val textColor = colorResource(id = R.color.yazirengi)
    
    // Mesaj konteyneri
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = if (message.isCurrentUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        // Profil fotoğrafı - sadece başkasının mesajı için göster
        if (!message.isCurrentUser) {
            // Profil fotoğrafı veya yer tutucu
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(colorResource(id = R.color.kutubordrengi).copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                // Profil fotoğrafı varsa göster, yoksa ilk harfi göster
                val profileUrl = userProfiles[message.senderId]
                if (profileUrl.isNullOrEmpty()) {
                    Text(
                        text = message.senderName.firstOrNull()?.toString() ?: "?",
                        color = colorResource(id = R.color.yazirengi),
                        style = MaterialTheme.typography.titleMedium
                    )
                } else {
                    // Profil fotoğrafını yükle
                    AsyncImage(
                        model = profileUrl,
                        contentDescription = "Profil fotoğrafı: ${message.senderName}",
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(8.dp))
        }
        
        // Mesaj kartı - Mesaj içeriğini içeren görsel öğe
        Card(
            modifier = Modifier
                .widthIn(max = 260.dp)
                .padding(end = if (message.isCurrentUser) 0.dp else 40.dp, start = if (message.isCurrentUser) 40.dp else 0.dp),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (message.isCurrentUser) 16.dp else 4.dp,
                bottomEnd = if (message.isCurrentUser) 4.dp else 16.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = backgroundColor
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 1.dp
            )
        ) {
            // Mesaj içerik alanı
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                // Başkasının mesajıysa gönderen adını göster
                if (!message.isCurrentUser) {
                    Text(
                        text = message.senderName,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily(Font(R.font.noto_regular)),
                            fontSize = 13.sp
                        ),
                        color = if (message.senderId == "admin") 
                            colorResource(id = R.color.kutubordrengi) 
                        else 
                            colorResource(id = R.color.kutubordrengi)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
                
                // Mesaj metni
                Text(
                    text = message.message,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily(Font(R.font.noto_regular)),
                        fontSize = 14.sp
                    ),
                    color = textColor
                )
                
                // Mesaj gönderim zamanı
                Text(
                    text = formatTimestamp(message.timestamp),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 10.sp
                    ),
                    color = colorResource(id = R.color.yazirengi).copy(alpha = 0.6f),
                    textAlign = TextAlign.End,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                )
            }
        }
    }
}

/**
 * Zaman bilgisini formatlar ve mesajın ne zaman silineceğini hesaplar
 * @param timestamp Milisaniye cinsinden zaman
 * @return Formatlanmış zaman bilgisi (Örn: 15:30 · 23s kaldı)
 */
private fun formatTimestamp(timestamp: Long): String {
    val currentTime = System.currentTimeMillis()
    val messageTime = timestamp
    val timeDiff = currentTime - messageTime
    
    // Gönderim saati formatı: HH:mm
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    val timeStr = sdf.format(Date(messageTime))
    
    // Kalan süre hesaplaması (24 saat = 86,400,000 milisaniye)
    val timeLeft = 86400000 - timeDiff
    
    // Eğer kalan süre negatifse (mesaj 24 saati geçmiş), sadece gönderim saatini göster
    if (timeLeft <= 0) {
        return timeStr
    }
    
    // Kalan süreyi formatlama
    val hoursLeft = timeLeft / 3600000
    val minutesLeft = (timeLeft % 3600000) / 60000
    
    return when {
        hoursLeft > 0 -> "$timeStr · ${hoursLeft}s kaldı"
        minutesLeft > 0 -> "$timeStr · ${minutesLeft}dk kaldı"
        else -> "$timeStr · <1dk kaldı"
    }
}