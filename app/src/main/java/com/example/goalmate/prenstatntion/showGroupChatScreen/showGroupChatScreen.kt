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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.text.style.TextOverflow
import com.example.goalmate.data.localdata.GroupCloseVoteState
import com.example.goalmate.data.localdata.Habit
import com.example.goalmate.extrensions.GroupDetailState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.lazy.rememberLazyListState
import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.icons.automirrored.filled.Send
import com.example.goalmate.utils.NetworkUtils
import kotlin.math.abs

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
    groupName: String,
    members : Int,
    daysLeft : Long,
    habitType : String,
    navController: NavController,
    groupsAddViewModel: GroupsAddViewModel
){
    // Snackbar state'i
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Snackbar gösterme fonksiyonu
    val showSnackbar: (String) -> Unit = { message ->
        scope.launch {
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
        }
    }

    // Mesaj girişi için durum değişkeni
    var messageText by remember { mutableStateOf("") }

    // kullanıcı profilini al
    val userProfile by groupsAddViewModel.profileImages.collectAsState()

    // Seçenekleri aç
    var expanded by remember { mutableStateOf(false) }


    val context = LocalContext.current

    // Mesajları dinlemeye başla - Effect kullanımı
    LaunchedEffect(key1 = groupedId) {
        groupsAddViewModel.getGroupMessages(groupedId, context)
    }

    // Periyodik mesaj temizliği zamanlanıyor
    LaunchedEffect(Unit) {
        groupsAddViewModel.scheduleMessageCleanup(context)
    }

// Alışkanlık tamamlama diyalog durumu
    var showHabitDialog by remember { mutableStateOf(false) }
    val habitCompletedToday by groupsAddViewModel.habitCompletedToday.collectAsState()

// Ekran açıldığında alışkanlık tamamlama durumu kontrol ediliyor
    LaunchedEffect(groupedId) {
        groupsAddViewModel.checkHabitCompletion(groupedId,context)
        if (habitCompletedToday[groupedId] == false) {
            showHabitDialog = true
        }
    }

    // Oylama dialog durumu
    var showVoteDialog by remember { mutableStateOf(false) }
    
    // Oylama durumunu al
    val voteState = groupsAddViewModel.groupCloseVoteState.collectAsState().value[groupedId]

    Log.e("daysLeft","Kalan gün: $daysLeft")
    // Mesaj listesini al
    val messages by groupsAddViewModel.messages.collectAsState()
    
    // Mesaj durumunu takip et (yükleme, hata, vb.)
    val messagesState by groupsAddViewModel.chatMessage.collectAsState()

    Log.e("groupeId","groupeId = $groupedId")

    // Habit tamamlandığında  dialog
    if (showHabitDialog) {
        AlertDialog(
            onDismissRequest = { /* İletişim kutusu dışarıya tıklanarak kapatılamaz */ },
            title = {
                Text(
                    text = "Günlük Alışkanlık Kontrolü",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Bugünkü alışkanlığınızı tamamladınız mı?",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        groupsAddViewModel.markHabitAsCompleted(groupedId, true ,context) // tamamlandı olarak işaretle
                        showHabitDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(id = R.color.kutubordrengi)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Tamamladım")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        showHabitDialog = false
                        groupsAddViewModel.markHabitAsCompleted(groupedId, false , context) // tamamlanmadı olarak işaretle
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Gray
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Tamamlamadım")
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )
    }

    Scaffold (
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    containerColor = colorResource(id = R.color.kutubordrengi),
                    contentColor = Color.White,
                    action = {
                        TextButton(
                            onClick = { data.dismiss() }
                        ) {
                            Text(
                                text = "Tamam",
                                color = Color.White
                            )
                        }
                    }
                ) {
                    Text(data.visuals.message)
                }
            }
        },
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
                        .padding(top = 48.dp, bottom = 20.dp, start = 16.dp, end = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Sol tarafta geri butonu ve başlık
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        // Geri butonu
                        IconButton(
                            onClick = {
                                navController.popBackStack(navController.graph.startDestinationId , false)
                                navController.navigate("groupListScreen")
                            },
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_back),
                                contentDescription = "Geri",
                                tint = colorResource(R.color.yazirengi)
                            )
                        }

                        // Grup bilgileri (isim ve detaylar)
                        Column(
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .weight(1f)
                        ) {
                            // Grup adı
                            Text(
                                text = groupName,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily(Font(R.font.noto_regular)),
                                    fontSize = 18.sp
                                ),
                                color = colorResource(id = R.color.yazirengi),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            
                            // Grup durumu ve kalan süre
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    painter = painterResource(
                                        when {
                                            daysLeft > 7 -> R.drawable.open
                                            daysLeft > 1 -> R.drawable.zaman
                                            else -> R.drawable.close
                                        }
                                    ),
                                    contentDescription = null,
                                    tint = when {
                                        daysLeft > 7 -> colorResource(id = R.color.yesil2)
                                        daysLeft > 3 -> colorResource(id = R.color.kutubordrengi)
                                        else -> colorResource(id = R.color.pastelkirmizi)
                                    },
                                    modifier = Modifier.size(14.dp)
                                )
                                
                                Text(
                                    text = when {
                                        daysLeft <= 1 -> "Son gün"
                                        else -> "$daysLeft gün kaldı"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = when {
                                        daysLeft > 7 -> colorResource(id = R.color.yesil2)
                                        daysLeft > 3 -> colorResource(id = R.color.kutubordrengi)
                                        else -> colorResource(id = R.color.pastelkirmizi)
                                    },
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    text = " • ",
                                    color = colorResource(id = R.color.yazirengi).copy(alpha = 0.7f),
                                    fontSize = 12.sp
                                )

                                Text(
                                    text = "$members Üye",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colorResource(id = R.color.yazirengi).copy(alpha = 0.7f),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                    
                    // Sağ taraftaki butonlar
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        // Liderlik tablosu butonu
                        IconButton(
                            onClick = {
                                /* Liderlik tablosu açma işlevi */
                                navController.navigate("ScoreBoardScreen/${groupedId}")
                            }
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.leaderboard),
                                contentDescription = "Liderlik Tablosu",
                                tint = colorResource(R.color.yazirengi),
                                modifier = Modifier.size(24.dp)
                            )
                        }


                        Box{
                            // Menü butonu
                            IconButton(onClick = {expanded = true}) {

                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "Diğer Seçenekler",
                                    tint = colorResource(R.color.yazirengi)
                                )
                            }

                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = {expanded = false},
                                containerColor = colorResource(R.color.arkaplan)
                            ) {
                                DropdownMenuItem(
                                    text = { Text(text = "Kurallar") },
                                    onClick = {
                                        // kurallar ekranına yönlendireceğiz
                                        navController.navigate("RulesScreen")
                                    }
                                )


                                DropdownMenuItem(
                                    text = { Text(text = "Guruptan Ayrıl") },
                                    onClick = {
                                        expanded = false
                                        leaveGroup(
                                            daysLeft = daysLeft,
                                            frequency = habitType,
                                            groupsAddViewModel = groupsAddViewModel,
                                            groupedId = groupedId,
                                            members = members,
                                            navController = navController,
                                            onShowSnackbar = showSnackbar
                                        )
                                    }
                                )

                                // Grup kapatma menü öğesini güncelle
                                if (groupsAddViewModel.getCurrentUserId() == groupsAddViewModel.groupDetailState.collectAsState().value.let { 
                                    if (it is GroupDetailState.Success) it.group.createdBy else null
                                }) {
                                    DropdownMenuItem(
                                        text = { Text(text = "Grubu Kapat") },
                                        onClick = {
                                            expanded = false
                                            if (voteState == null) {
                                                // Oylama başlat
                                                groupsAddViewModel.initiateGroupCloseVote(groupedId,context)
                                                showSnackbar("Grup kapatma oylaması başlatıldı")
                                            } else {
                                                showVoteDialog = true
                                            }
                                        },
                                        enabled = voteState?.canAdminInitiateVote ?: true
                                    )
                                }
                            }

                        }

                    }
                }
                HorizontalDivider(
                    thickness = 1.dp,
                    color = colorResource(id = R.color.kutubordrengi).copy(alpha = 0.2f)
                )
                
                // Oylama banner'ını göster
                if (voteState != null) {
                    VotingBanner(
                        voteState = voteState,
                        onVoteClick = { isYesVote ->
                            groupsAddViewModel.submitVote(groupedId, isYesVote)
                            showSnackbar(if (isYesVote) "Evet oyu kullandınız" else "Hayır oyu kullandınız")
                        },
                        context = context
                    )
                }





            }
        },
        // Alt Bar - Mesaj yazma alanı ve gönder butonunu içerir
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colorResource(id = R.color.arkaplan))
                    .padding(bottom = 4.dp)
                    .imePadding()
                    .navigationBarsPadding()
            ) {
                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = colorResource(id = R.color.kutubordrengi).copy(alpha = 0.1f)
                )
                
                if (habitCompletedToday[groupedId] != true) {
                    // Show warning message when habit is not completed
                    Text(
                        text = "Mesaj göndermek için önce günlük alışkanlığınızı tamamlamalısınız",
                        style = MaterialTheme.typography.bodySmall,
                        color = colorResource(id = R.color.pastelkirmizi),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .clickable {
                                showHabitDialog = true
                            }
                            .fillMaxWidth()
                            .padding(vertical = 40.dp)
                    )
                } else {
                    // Show message input when habit is completed
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        // Mesaj girdi alanı
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
                                    .fillMaxWidth()
                                    .heightIn(min = 40.dp),
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
                                        sendMessage(
                                            messageText = messageText,
                                            groupedId = groupedId,
                                            groupsAddViewModel = groupsAddViewModel,
                                            context = context
                                        ) {
                                            messageText = ""
                                        }
                                    }
                                )
                            )
                        }
                        
                        // Gönder butonu
                        IconButton(
                            onClick = { 
                                sendMessage(
                                    messageText = messageText,
                                    groupedId = groupedId,
                                    groupsAddViewModel = groupsAddViewModel,
                                    context = context
                                ) {
                                    messageText = ""
                                }
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(colorResource(id = R.color.kutubordrengi))
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Mesaj Gönder",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = if (habitCompletedToday[groupedId] == true) 64.dp else 80.dp),
                reverseLayout = true,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.Bottom,
                state = rememberLazyListState()
            ) {
                // Mesajları ters sırada göster
                items(messages.reversed()) { message ->
                    MessageItem(message, userProfile)
                }

                // Tarih ayırıcısı - En alta ekle
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
                }

                // Bilgilendirme mesajı
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
                }
            }
            
            // Yükleme durumu
            when (messagesState) {
                is MessagesState.Loading -> {
                    // Yükleniyor göstergesi koymayacağız

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
    // Rastgele isim renkleri için renk listesi
    val nameColors = listOf(
        Color(0xFF1E88E5), // Mavi
        Color(0xFF43A047), // Yeşil
        Color(0xFFE53935), // Kırmızı
        Color(0xFF8E24AA), // Mor
        Color(0xFFFFB300), // Amber
        Color(0xFF00897B), // Teal
        Color(0xFFD81B60), // Pembe
        Color(0xFF6D4C41)  // Kahverengi
    )
    
    // Kullanıcı ID'sine göre tutarlı bir renk seçimi
    val nameColor = nameColors[abs(message.senderId.hashCode()) % nameColors.size]
    
    // Mesaj arka plan rengi
    val backgroundColor = if (message.isCurrentUser) 
        colorResource(id = R.color.kutubordrengi).copy(alpha = 0.7f)
    else 
        Color.White
    
    // Mesaj yazı rengi
    val textColor = if (message.isCurrentUser) 
        Color.White 
    else 
        Color.Black
    
    // Ana mesaj satırı
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp, horizontal = 2.dp),
        horizontalArrangement = if (message.isCurrentUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        // Profil resmi - sadece başka kullanıcıların mesajları için göster
        if (!message.isCurrentUser) {
            Box(
                modifier = Modifier
                    .padding(end = 2.dp)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(colorResource(id = R.color.kutubordrengi).copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                val profileUrl = userProfiles[message.senderId]
                if (profileUrl.isNullOrEmpty()) {
                    Text(
                        text = message.senderName.firstOrNull()?.toString() ?: "?",
                        color = colorResource(id = R.color.yazirengi),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    AsyncImage(
                        model = profileUrl,
                        contentDescription = "Profil fotoğrafı",
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }

        Card(
            shape = RoundedCornerShape(
                topStart = 8.dp,
                topEnd = 8.dp,
                bottomStart = if (message.isCurrentUser) 8.dp else 0.dp,
                bottomEnd = if (message.isCurrentUser) 0.dp else 8.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = backgroundColor
            ),
            // İçeriğe göre otomatik boyutlanma, maksimum genişlik sınırlamasıyla
            modifier = Modifier
                .widthIn(max = 250.dp)
                .wrapContentSize()
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 6.dp, vertical = 3.dp)
            ) {
                // Başkasının mesajıysa isim gösterilir
                if (!message.isCurrentUser) {
                    Text(
                        text = message.senderName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = nameColor,
                        modifier = Modifier.padding(bottom = 1.dp)
                    )
                }

                // Mesaj metni ve saat bilgisi yan yana
                Row(
                    modifier = Modifier.wrapContentWidth(),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.End
                ) {
                    // Mesaj metni
                    Text(
                        text = message.message,
                        fontSize = 13.sp,
                        color = textColor,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        modifier = Modifier.padding(start = 4.dp, bottom = 0.dp)
                    ) {
                        // Zaman bilgisi
                        Text(
                            text = formatMessageTime(message.timestamp),
                            fontSize = 8.sp,
                            color = if(message.isCurrentUser) Color.White.copy(alpha = 0.7f) else Color.Gray
                        )
                        
                        // Çift tik göstergesi - sadece kendi mesajlarımız için
                        if (message.isCurrentUser) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_double_check),
                                contentDescription = "Okundu",
                                tint = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

fun leaveGroup(daysLeft: Long, frequency: String, groupsAddViewModel: GroupsAddViewModel, groupedId: String, members: Int, navController: NavController, onShowSnackbar: (String) -> Unit) {
    val minDaysRequired = when(frequency.lowercase()) {
        "günlük" -> 1
        "haftalık" -> 7
        "aylık" -> 30
        else -> 0
    }

    if (daysLeft < minDaysRequired) {
        onShowSnackbar("Alışkanlık süresinin sonuna kadar gruptan ayrılamazsınız.")
    } else {
        groupsAddViewModel.leaveGroup(groupedId)
        if (members <= 1) { // Eğer son üye de ayrılıyorsa
            groupsAddViewModel.closeGroup(groupedId)
            onShowSnackbar("Son üye ayrıldığı için grup kapatıldı.")
        } else {
            onShowSnackbar("Gruptan başarıyla ayrıldınız.")
        }
        navController.navigate("GroupListScreen") {
            popUpTo(0) { inclusive = true }
        }
    }
}

/**
 * Mesaj zamanını sadece saat:dakika formatında gösterir
 */
private fun formatMessageTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

// Oylama banner'ı composable
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun VotingBanner(
    voteState: GroupCloseVoteState,
    onVoteClick: (Boolean) -> Unit,
    context: Context
) {
    var isExpanded by remember { mutableStateOf(true) }
    var remainingTime by remember { mutableStateOf(voteState.votingEndTime - System.currentTimeMillis()) }



    LaunchedEffect(voteState.votingEndTime) {
        val currentServerTime = NetworkUtils.getTime(context = context)
        while (remainingTime > 0) {
            delay(1000)
            remainingTime = voteState.votingEndTime - currentServerTime
        }
    }

    if (isExpanded) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = colorResource(id = R.color.kutubordrengi).copy(alpha = 0.1f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                // Başlık satırı
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Sol taraf - başlık ve süre
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.close),
                            contentDescription = null,
                            tint = colorResource(id = R.color.kutubordrengi),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Grup Kapatma Oylaması",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = colorResource(id = R.color.kutubordrengi)
                        )
                        if (remainingTime > 0) {
                            Text(
                                text = " • ${formatRemainingTime(remainingTime)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = colorResource(id = R.color.kutubordrengi),
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Gizle butonu
                    IconButton(
                        onClick = { isExpanded = false },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_hide),
                            contentDescription = "Gizle",
                            tint = colorResource(id = R.color.kutubordrengi),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Oy durumu
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Evet oyları
                    VoteCount(
                        count = voteState.yesVotes,
                        label = "Evet",
                        color = colorResource(id = R.color.yesil2)
                    )

                    VerticalDivider(
                        modifier = Modifier.height(20.dp),
                        color = colorResource(id = R.color.kutubordrengi).copy(alpha = 0.2f)
                    )

                    // Hayır oyları
                    VoteCount(
                        count = voteState.noVotes,
                        label = "Hayır",
                        color = colorResource(id = R.color.pastelkirmizi)
                    )

                    VerticalDivider(
                        modifier = Modifier.height(20.dp),
                        color = colorResource(id = R.color.kutubordrengi).copy(alpha = 0.2f)
                    )

                    // Toplam üye
                    VoteCount(
                        count = voteState.totalMembers,
                        label = "Toplam",
                        color = colorResource(id = R.color.yazirengi)
                    )
                }

                // Oy verme butonları
                if (!voteState.hasUserVoted) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onVoteClick(false) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorResource(id = R.color.pastelkirmizi)
                            ),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 4.dp)
                        ) {
                            Text(
                                "Hayır",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 12.sp
                            )
                        }
                        Button(
                            onClick = { onVoteClick(true) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorResource(id = R.color.yesil2)
                            ),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 4.dp)
                        ) {
                            Text(
                                "Evet",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 12.sp
                            )
                        }
                    }
                } else {
                    Text(
                        text = "Oyunuzu kullandınız",
                        style = MaterialTheme.typography.bodySmall,
                        color = colorResource(id = R.color.yazirengi),
                        fontSize = 11.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    } else {
        // Gizlenmiş durumdaki mini buton
        Card(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .clickable { isExpanded = true },
            colors = CardDefaults.cardColors(
                containerColor = colorResource(id = R.color.kutubordrengi).copy(alpha = 0.1f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ballot),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Oylama",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    color = colorResource(id = R.color.kutubordrengi)
                )
            }
        }
    }
}

@Composable
private fun VoteCount(
    count: Int,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "$count",
            style = MaterialTheme.typography.bodyMedium,
            color = color,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = color,
            fontSize = 10.sp
        )
    }
}

private fun formatRemainingTime(remainingTime: Long): String {
    val hours = remainingTime / (1000 * 60 * 60)
    val minutes = (remainingTime % (1000 * 60 * 60)) / (1000 * 60)
    return String.format("%02d:%02d", hours, minutes)
}

// Mesaj gönderme fonksiyonunu güncelleyelim
@RequiresApi(Build.VERSION_CODES.O)
private fun sendMessage(
    messageText: String,
    groupedId: String,
    groupsAddViewModel: GroupsAddViewModel,
    context: Context,
    onMessageSent: () -> Unit
) {
    if (messageText.isNotBlank()) {
        val currentUserId = groupsAddViewModel.getCurrentUserId() ?: return
        val currentUserName = groupsAddViewModel.getCurrentUserName() ?: "Misafir"
        
        groupsAddViewModel.createMessagesFirebase(
            groupId = groupedId,
            senderId = currentUserId,
            senderName = currentUserName,
            message = messageText,
            isCurrentUser = true,
            context = context
        )
        
        onMessageSent()
    }
}

