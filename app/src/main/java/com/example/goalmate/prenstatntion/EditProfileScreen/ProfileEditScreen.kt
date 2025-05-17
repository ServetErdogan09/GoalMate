package com.example.goalmate.prenstatntion.EditProfileScreen


import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.goalmate.R
import com.example.goalmate.prenstatntion.homescreen.getProfilePainter
import com.example.goalmate.viewmodel.GroupsAddViewModel
import com.example.goalmate.viewmodel.RegisterViewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch

@Composable
fun ProfileEditScreen(
    registerViewModel: RegisterViewModel,
    addViewModel: GroupsAddViewModel,
    navController: NavController
) {
    val userName by registerViewModel.userName.collectAsState()
    val userProfile by registerViewModel.profileImage.collectAsState()

    var editedUserName by remember { mutableStateOf(userName) }
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf<String?>(null) }
    
    // Snackbar state'i
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()

    val context = LocalContext.current

    Scaffold(
        scaffoldState = scaffoldState,
        modifier = Modifier.fillMaxSize(),
        backgroundColor = colorResource(R.color.arkaplan),
        topBar = {
            TopAppBar(
                backgroundColor = colorResource(R.color.arkaplan),
                elevation = 0.dp,
                title = {
                    Text(
                        text = "Profil Düzenle",
                        color = colorResource(R.color.yazirengi),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_back),
                            contentDescription = "Geri",
                            tint = colorResource(R.color.yazirengi)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Profil resmi
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .padding(bottom = 32.dp)
            ) {
                val painter = when {
                    userProfile.startsWith("http") || userProfile.startsWith("content") -> {
                        rememberAsyncImagePainter(
                            model = userProfile,
                            error = painterResource(R.drawable.bildl)
                        )
                    }
                    else -> {
                        painterResource(id = getProfilePainter(userProfile, R.drawable.personel))
                    }
                }

                Image(
                    painter = painter,
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .border(2.dp, colorResource(R.color.kutubordrengi), CircleShape)
                )
            }

            // Kullanıcı bilgileri başlığı
            Text(
                text = "Kullanıcı Bilgileri",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.yazirengi),
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(bottom = 12.dp)
            )

            // Kullanıcı adı düzenleme
            OutlinedTextField(
                value = editedUserName,
                onValueChange = { editedUserName = it },
                label = { Text("Kullanıcı Adı") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = colorResource(R.color.kutubordrengi),
                    unfocusedBorderColor = colorResource(R.color.yazirengi),
                    backgroundColor = colorResource(R.color.beyaz)
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Şifre değişikliği başlığı
            Text(
                text = "Şifre Değiştir",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.yazirengi),
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(bottom = 12.dp)
            )

            // Mevcut şifre
            OutlinedTextField(
                value = currentPassword,
                onValueChange = { currentPassword = it },
                label = { Text("Mevcut Şifre") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = colorResource(R.color.kutubordrengi),
                    unfocusedBorderColor = colorResource(R.color.yazirengi),
                    backgroundColor = colorResource(R.color.beyaz)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Yeni şifre
            OutlinedTextField(
                value = newPassword,
                onValueChange = { 
                    newPassword = it
                    if (confirmPassword.isNotEmpty()) {
                        passwordError = if (it != confirmPassword) "Şifreler eşleşmiyor" else null
                    }
                },
                label = { Text("Yeni Şifre") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = colorResource(R.color.kutubordrengi),
                    unfocusedBorderColor = colorResource(R.color.yazirengi),
                    backgroundColor = colorResource(R.color.beyaz)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Yeni şifre tekrar
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { 
                    confirmPassword = it
                    passwordError = if (it != newPassword) "Şifreler eşleşmiyor" else null
                },
                label = { Text("Yeni Şifre Tekrar") },
                visualTransformation = PasswordVisualTransformation(),
                isError = passwordError != null,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = colorResource(R.color.kutubordrengi),
                    unfocusedBorderColor = colorResource(R.color.yazirengi),
                    backgroundColor = colorResource(R.color.beyaz)
                )
            )

            // Şifre hata mesajı
            if (passwordError != null) {
                Text(
                    text = passwordError!!,
                    color = MaterialTheme.colors.error,
                    style = MaterialTheme.typography.caption,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Kaydet butonu
            Button(
                onClick = {
                    // İsim değişikliği kontrolü
                    if (editedUserName != userName && editedUserName.isNotBlank()) {
                        registerViewModel.updateUserName(editedUserName, context) { success ->
                            if (success) {
                                scope.launch {
                                    scaffoldState.snackbarHostState.showSnackbar("Kullanıcı adı başarıyla güncellendi")
                                }
                            }
                        }
                    }

                    // Şifre değişikliği kontrolü
                    if (currentPassword.isNotBlank() && newPassword.isNotBlank() && confirmPassword.isNotBlank()) {
                        if (newPassword == confirmPassword) {
                            registerViewModel.updateUserPassword(currentPassword, newPassword) { error ->
                                if (error == null) {
                                    scope.launch {
                                        scaffoldState.snackbarHostState.showSnackbar("Şifre başarıyla güncellendi")
                                        // Şifre alanlarını temizle
                                        currentPassword = ""
                                        newPassword = ""
                                        confirmPassword = ""
                                        passwordError = null
                                    }
                                } else {
                                    scope.launch {
                                        scaffoldState.snackbarHostState.showSnackbar(error)
                                    }
                                }
                            }
                        } else {
                            scope.launch {
                                scaffoldState.snackbarHostState.showSnackbar("Yeni şifreler eşleşmiyor")
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp)
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = colorResource(R.color.kutubordrengi)),
                enabled = (editedUserName != userName && editedUserName.isNotBlank()) || 
                         (currentPassword.isNotBlank() && newPassword.isNotBlank() && confirmPassword.isNotBlank() && passwordError == null)
            ) {
                Text(
                    "Değişiklikleri Kaydet",
                    color = colorResource(R.color.beyaz),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}