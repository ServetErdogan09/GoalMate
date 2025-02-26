package com.example.goalmate.prenstatntion.ProfilScreen

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.goalmate.R
import com.example.goalmate.viewmodel.RegisterViewModel

@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedAvatarId by remember { mutableStateOf<Int?>(null) }
    val context = LocalContext.current
    val userName by viewModel.userName.collectAsState()
    
    val man = listOf(R.drawable.erkek1, R.drawable.erkek12, R.drawable.erkek10, R.drawable.erkek11, R.drawable.erkek13)
    val woman = listOf(R.drawable.kiz1, R.drawable.kiz10, R.drawable.kiz11, R.drawable.kiz12, R.drawable.kiz13)

    // Profil fotoğrafı seçici
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            selectedAvatarId = null
            viewModel.updateProfileImage(it.toString(), context)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        
        // Profil fotoğrafı
        Box(
            modifier = Modifier.size(150.dp)
        ) {
            Image(
                painter = when {
                    selectedImageUri != null -> rememberAsyncImagePainter(selectedImageUri)
                    selectedAvatarId != null -> painterResource(selectedAvatarId!!)
                    else -> painterResource(R.drawable.personel)
                },
                contentDescription = "Profil Fotoğrafı",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .border(
                        width = 2.dp,
                        color = colorResource(R.color.yazirengi),
                        shape = CircleShape
                    )
                    .clickable { launcher.launch("image/*") },
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = userName,
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Avatar seçim bölümü
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Erkek Avatarları",
                style = MaterialTheme.typography.titleMedium,
                color = colorResource(R.color.yazirengi),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            AvatarRow(
                avatars = man,
                onAvatarSelected = { resourceId ->
                    selectedAvatarId = resourceId
                    selectedImageUri = null
                    viewModel.updateProfileImage(resourceId.toString(), context)
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Kadın Avatarları",
                style = MaterialTheme.typography.titleMedium,
                color = colorResource(R.color.yazirengi),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            AvatarRow(
                avatars = woman,
                onAvatarSelected = { resourceId ->
                    selectedAvatarId = resourceId
                    selectedImageUri = null
                    viewModel.updateProfileImage(resourceId.toString(), context)
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Butonlar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = { 
                        if (selectedAvatarId != null || selectedImageUri != null) {
                            viewModel.updateProfileImage(
                                if (selectedImageUri != null) selectedImageUri.toString() 
                                else selectedAvatarId.toString(),
                                context
                            )
                            navController.navigate("HomeScreen") {
                                popUpTo("ProfileScreen") { inclusive = true }
                            }
                        }
                    },
                    enabled = selectedAvatarId != null || selectedImageUri != null,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(R.color.yazirengi)
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Profili Tamamla")
                }

                Spacer(modifier = Modifier.width(8.dp))

                OutlinedButton(
                    onClick = { 
                        navController.navigate("HomeScreen") {
                            popUpTo("ProfileScreen") { inclusive = true }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = colorResource(R.color.yazirengi)
                    )
                ) {
                    Text("Daha Sonra")
                }
            }
        }
    }
}

@Composable
fun AvatarRow(
    avatars: List<Int>,
    onAvatarSelected: (Int) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(avatars.size) { index ->
            Image(
                painter = painterResource(id = avatars[index]),
                contentDescription = "Avatar",
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .border(
                        width = 2.dp,
                        color = colorResource(R.color.yazirengi),
                        shape = CircleShape
                    )
                    .clickable {
                        onAvatarSelected(avatars[index])
                    }
            )
        }
    }
}