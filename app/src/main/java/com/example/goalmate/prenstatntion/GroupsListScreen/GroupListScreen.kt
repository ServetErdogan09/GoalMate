package com.example.goalmate.presentation.GroupsListScreen

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Tab
import androidx.compose.material3.TabRow
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.goalmate.R
import com.example.goalmate.data.localdata.Group
import com.example.goalmate.extrensions.GroupListState
import com.example.goalmate.prenstatntion.homescreen.getProfilePainter
import com.example.goalmate.viewmodel.GroupsAddViewModel
import com.example.goalmate.viewmodel.RegisterViewModel


@Composable
fun GroupListScreen(
    navController: NavController,
    viewModel: GroupsAddViewModel = viewModel(),
    registerViewModel: RegisterViewModel = viewModel()
) {
    var selectedTab by remember { mutableStateOf(0) }
    val groupListState = viewModel.groupListState.collectAsState().value
    val myGroups by viewModel.myGroups.collectAsState()
    Log.e("myGroups","myGroups . $myGroups")

    LaunchedEffect(Unit) {
        // kullanıcının olduğu gurupları çek
        viewModel.getUserGroups()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = colorResource(R.color.arkaplan))
            .padding(top = 25.dp, bottom = 110.dp)
    ) {
        Spacer(modifier = Modifier.padding(top = 15.dp))
        // Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = colorResource(R.color.beyaz),
            contentColor = colorResource(R.color.kutubordrengi)
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = {
                    selectedTab = 0

                          },
                text = { Text("Tüm Gruplar") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Katıldığım Gruplar") }
            )
        }

        when (selectedTab) {
            0 -> {
                // Tüm Gruplar Tab'ı
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
                        .background(colorResource(R.color.beyaz))
                ) {
                    Spacer(modifier = Modifier.height(15.dp))
                    Groupcategory(viewModel)
                    Spacer(modifier = Modifier.height(10.dp))

                    when(groupListState) {
                        is GroupListState.Loading -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    color = colorResource(R.color.kutubordrengi),
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                        }
                        is GroupListState.Error -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = groupListState.message,
                                    color = colorResource(R.color.pastelkirmizi),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                        is GroupListState.Success -> {
                            if (groupListState.groups.isEmpty()) {
                                EmptyGroupState(selectedTab)
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(groupListState.groups) { group ->
                                        GroupCard(group = group, groupsAddViewModel = viewModel, navController)
                                    }

                                    item {
                                        if (groupListState.groups.isNotEmpty()) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(8.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(32.dp),
                                                    color = colorResource(R.color.kutubordrengi)
                                                )
                                            }

                                            LaunchedEffect(Unit) {
                                                viewModel.loadMoreGroups()
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            1 -> {

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
                        .background(colorResource(R.color.beyaz))
                        .padding(20.dp)
                ) {
                    if (myGroups.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            EmptyGroupState(selectedTab)
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(myGroups) { group ->
                                GroupCard(
                                    group = group,
                                    groupsAddViewModel = viewModel,
                                    navController = navController
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Groupcategory(viewModel: GroupsAddViewModel) {
    val groupList = listOf("Tümü", "Özel", "Açık", "Spor", "Eğitim", "Sanat", "Teknoloji", "Seyahat", "Diğer")
    var selectedCategory by remember { mutableStateOf<String?>("Tümü") }

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(groupList) { categoryItem ->
            CategoryChip(
                category = categoryItem,
                isSelected = selectedCategory == categoryItem,
                onSelected = { 
                    selectedCategory = categoryItem
                    viewModel.setCategory(categoryItem)
                }
            )
        }
    }
}

@Composable
fun CategoryChip(
    category: String,
    isSelected: Boolean,
    onSelected: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable { onSelected() },
        shape = RoundedCornerShape(50),
        color = if (isSelected) colorResource(R.color.kutubordrengi) else Color.Transparent,
        border = BorderStroke(
            width = 1.dp,
            color = if (isSelected) colorResource(R.color.kutubordrengi) else colorResource(R.color.acik_gri)
        )
    ) {
        Text(
            text = category,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = if (isSelected) Color.White else colorResource(R.color.yazirengi),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun GroupCard(group: Group, groupsAddViewModel: GroupsAddViewModel, navController: NavController) {
    val profileImages = groupsAddViewModel.profileImages.collectAsState().value
    val creatorProfileImage = profileImages[group.createdBy] ?: ""

    LaunchedEffect(group.createdBy) {
        groupsAddViewModel.getProfile(group.createdBy)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .clickable { navController.navigate("GroupDetailScreen/${group.groupId}") },
        border = BorderStroke(width = 0.2.dp , color = colorResource(R.color.yazirengi)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colorResource(R.color.gri)),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Üst kısımdaki kategori etiketi
            Card(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp),
                shape = RoundedCornerShape(50),
                colors = CardDefaults.cardColors(
                    containerColor = getCategoryColor(group.category).copy(alpha = 0.1f)
                )
            ) {
                Text(
                    text = group.category,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    color = getCategoryColor(group.category),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Üst Kısım - Başlık ve Açıklama
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 80.dp)
                ) {
                    Text(
                        text = group.groupName,
                        style = MaterialTheme.typography.titleLarge,
                        color = colorResource(R.color.yazirengi),
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = group.description.ifEmpty { "Bu grup hakkında herhangi bir açıklama bulunmamaktadır." },
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorResource(R.color.yazirengi).copy(alpha = 0.7f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Alt Kısım - Grup Lideri ve Durum Bilgileri
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Sol taraf - Grup Lideri
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(
                                color = colorResource(R.color.yazirengi).copy(alpha = 0.1f),
                                shape = RoundedCornerShape(20.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Image(
                            painter = when {
                                creatorProfileImage.isNotEmpty() -> {
                                    when {
                                        creatorProfileImage.startsWith("http") || creatorProfileImage.startsWith("content") -> {
                                            rememberAsyncImagePainter(model = creatorProfileImage)
                                        }
                                        else -> painterResource(getProfilePainter(creatorProfileImage, R.drawable.personel))
                                    }
                                }
                                else -> painterResource(R.drawable.personel)
                            },
                            contentDescription = "Profile Image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                        )
                        
                        Spacer(modifier = Modifier.width(6.dp))
                        
                        Text(
                            text = "Grup Lideri",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colorResource(R.color.yazirengi),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Sağ taraf - Durum Bilgileri
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Katılımcı sayısı
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = colorResource(R.color.kutubordrengi).copy(alpha = 0.1f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_personal_info),
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = colorResource(R.color.kutubordrengi)
                                )
                                Text(
                                    text = "${group.members.size}/${group.participantNumber}",
                                    color = colorResource(R.color.kutubordrengi),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }

                        // Gizlilik durumu
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (group.isPrivate) 
                                    colorResource(R.color.kirmizi).copy(alpha = 0.1f)
                                else 
                                    colorResource(R.color.yesil).copy(alpha = 0.1f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    painter = painterResource(
                                        if (group.isPrivate) R.drawable.close
                                        else R.drawable.open
                                    ),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = if (group.isPrivate) 
                                        colorResource(R.color.pastelkirmizi)
                                    else 
                                        colorResource(R.color.yesil)
                                )
                                Text(
                                    text = if (group.isPrivate) "Özel" else "Açık",
                                    color = if (group.isPrivate) 
                                        colorResource(R.color.pastelkirmizi)
                                    else 
                                        colorResource(R.color.yesil),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Kategori renklerini belirlemek için yardımcı fonksiyon
@Composable
private fun getCategoryColor(category: String): Color {
    return when (category.lowercase()) {
        "spor" -> Color(0xFF4CAF50)
        "eğitim" -> Color(0xFF2196F3)
        "sanat" -> Color(0xFFE91E63)
        "teknoloji" -> Color(0xFF9C27B0)
        "seyahat" -> Color(0xFFFF9800)
        else -> Color(0xFF607D8B)
    }
}

@Composable
fun EmptyGroupState(selectedTab : Int) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.group),
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = colorResource(R.color.yazirengi).copy(alpha = 0.5f)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Henüz hiç grup yok",
                style = MaterialTheme.typography.titleMedium,
                color = colorResource(R.color.yazirengi)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = if (selectedTab == 0) "Yeni bir grup oluşturarak başlayabilirsin" else "Hayde Guruplara katılarak alışkanlıkları yap",
                style = MaterialTheme.typography.bodyMedium,
                color = colorResource(R.color.yazirengi).copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}