package com.example.goalmate.presentation.GroupsListScreen

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.viewmodel.compose.viewModel
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
    // Kategorilerin listesi ve ilgili navigasyon fonksiyonunu geçiyoruz
    val groupListState = viewModel.groupListState.collectAsState().value



    Column (
        modifier = Modifier.fillMaxSize().
        padding(6.dp).
        background(color = colorResource(R.color.arkaplan))
    ){
        Groupcategory()

        when(groupListState){
            is GroupListState.Loading->{
                CircularProgressIndicator()
            }
            is GroupListState.Error->{
                Text(text = groupListState.message)
            }
            is GroupListState.Success ->{
                GroupHabitCard(groupListState.groups, registerViewModel = registerViewModel)
            }
        }
    }

}

@Composable
fun Groupcategory() {
    val groupList = listOf("Spor", "Eğitim", "Sanat", "Teknoloji", "Seyahat", "Diğer")

    LazyRow(modifier = Modifier.padding(16.dp)) {
        items(groupList) { categoryItem ->
            TextButton(
                onClick = {
                    Log.e("Groupcategory", "Kategori tıklandı: $categoryItem")
                },
                border = BorderStroke(width = 1.dp, color = colorResource(R.color.acikgri)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .padding(end = 16.dp , top = 10.dp)
            ) {
                Text(
                    text = categoryItem,
                    color = colorResource(R.color.yazirengi)
                )
            }
        }
    }
}


@Composable
fun GroupHabitCard(groupList: List<Group> , registerViewModel: RegisterViewModel) {
    val profileImage = registerViewModel.profileImage.toString()
    LazyColumn(
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        items(groupList) { group ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                ) {
                    Text(
                        text = group.groupName,
                        style = MaterialTheme.typography.titleLarge,
                        color = colorResource(R.color.yazirengi)
                    )
                    Row(
                        modifier = Modifier.padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {

                        // Profil Resmi
                        Image(
                            painter = when {
                                profileImage.isNotEmpty() -> {
                                    when {
                                        profileImage.startsWith("http") || profileImage.startsWith("content") -> {
                                            rememberAsyncImagePainter(
                                                model = profileImage,
                                                error = painterResource(R.drawable.personel)
                                            )
                                        }
                                        else -> {
                                            painterResource(getProfilePainter(profileImage, R.drawable.personel))
                                        }
                                    }
                                }
                                else -> painterResource(R.drawable.personel)
                            },
                            contentDescription = "Profile Image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .border(2.dp, colorResource(R.color.yazirengi), CircleShape)
                        )

                        Text(
                            text = "${group.members.size}/${group.participantNumber} Katılımcı",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}