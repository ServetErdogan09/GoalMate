package com.example.goalmate.prenstatntion.login

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.goalmate.viewmodel.RegisterViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.goalmate.data.AuthState
import com.example.goalmate.data.RegistrationStep

import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions


import com.example.yeniproje.R
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.ui.graphics.Color


@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: RegisterViewModel = hiltViewModel(),
    initialLoginMode: Boolean = true
) {
    var isLoginMode by remember {
        mutableStateOf(initialLoginMode)
    }
    val currentStep by viewModel.currentStep.collectAsState(initial = RegistrationStep.EMAIL_PASSWORD)



    BackHandler {
        when {
            !isLoginMode && currentStep != RegistrationStep.EMAIL_PASSWORD -> {
                viewModel.moveToPreviousStep()
            }
            else -> {
                navController.popBackStack()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = colorResource(R.color.arkaplan)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp , start = 8.dp),
            horizontalArrangement = Arrangement.Start
        ) {
            IconButton(
                onClick = {
                    if (!isLoginMode && currentStep != RegistrationStep.EMAIL_PASSWORD) {
                        viewModel.moveToPreviousStep()
                    } else {
                        navController.popBackStack()
                    }
                }
            ) {

                Icon(
                    painter = painterResource(R.drawable.backlogin),
                    contentDescription = "back",
                    modifier = Modifier.padding(top = 17.dp)
                )
            }
        }

        if (!isLoginMode) {
            StepIndicator(currentStep = currentStep)
        }

        when {
            isLoginMode -> LoginContent(viewModel, navController)
            else -> when (currentStep) {
                RegistrationStep.EMAIL_PASSWORD -> EmailPasswordStep(viewModel)
                RegistrationStep.PERSONAL_INFO -> PersonalInfoStep(viewModel)
                RegistrationStep.BIRTH_DATE -> BirthDateStep(viewModel)
            }
        }

        // Mode switch button
        TextButton(
            onClick = { isLoginMode = !isLoginMode },
            modifier = Modifier.padding(top = 10.dp)
        ) {
            Text(if (isLoginMode) "Hesabın yok mu? Hesap oluştur" else "Zaten bir hesabın var mı? Oturum Aç")
        }

        when (val state = viewModel.authState.collectAsState().value) {
            is AuthState.Success -> {
                LaunchedEffect(Unit) {
                    navController.navigate("HomeScreen") {
                        popUpTo("LoginScreen") { inclusive = true }
                    }
                }
            }
            is AuthState.VerificationRequired -> {
                LaunchedEffect(Unit) {
                    navController.navigate("verification") {
                        popUpTo("LoginScreen") { inclusive = true }
                    }
                }
            }
            is AuthState.Loading -> {
                // Loading göstergesi sadece buton içinde gösterilecek
            }
            else -> {}
        }
    }
}

@Composable
fun StepIndicator(currentStep: RegistrationStep) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        for (i in 1..3) {
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .size(12.dp)
                    .background(
                        when {
                            i == currentStep.ordinal + 1 -> MaterialTheme.colorScheme.primary
                            i < currentStep.ordinal + 1 -> MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        },
                        CircleShape
                    )
            )
        }
    }
}

@Composable
fun LoginContent(viewModel: RegisterViewModel, navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val authState by viewModel.authState.collectAsState()

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.logonew),
            contentDescription = "Login Image",
            modifier = Modifier
                .size(250.dp)
                .padding(bottom = 25.dp),
        )


        OutlinedTextField(
            value = email,
            onValueChange = { 
                email = it
                viewModel.clearError()
            },
            label = { Text("E-posta") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = MaterialTheme.shapes.medium,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { 
                password = it
                viewModel.clearError()
            },
            label = { Text("Şifre") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = MaterialTheme.shapes.medium,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Hata mesajı gösterimi burada kalacak
        when (authState) {
            is AuthState.Error -> {
                Text(
                    text = (authState as AuthState.Error).message,
                    color = Color.Red,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    textAlign = TextAlign.Center
                )
            }
            else -> {}
        }

        Button(
            onClick = { 
                viewModel.clearError()
                if (email.isBlank() || password.isBlank()) {
                    viewModel.showError("Lütfen email ve şifre alanlarını doldurunuz")
                } else {
                    viewModel.login(email, password)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(50.dp),
            shape = MaterialTheme.shapes.medium,
            colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.yazirengi))
        ) {
            if (authState is AuthState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White
                )
            } else {
                Text("Giriş Yap", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Composable
fun EmailPasswordStep(viewModel: RegisterViewModel) {
    val registrationData by viewModel.registrationData.collectAsState()
    var isValid by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = colorResource(R.color.arkaplan)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.logonew),
            contentDescription = "Email Signup Image",
            modifier = Modifier
                .size(250.dp)
                .padding(bottom = 20.dp),
            contentScale = ContentScale.Fit
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(colorResource(R.color.arkaplan)),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    "Hesap Oluştur",
                    style = MaterialTheme.typography.titleLarge,
                    color = colorResource(R.color.yazirengi),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = registrationData.email,
                    onValueChange = {
                        viewModel.clearError()
                        viewModel.updateEmail(it)
                    },
                    label = { Text("E-posta") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.small,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = registrationData.password,
                    onValueChange = {
                        viewModel.clearError()
                        viewModel.updatePassword(it)
                    },
                    label = { Text("Şifre") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.small,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
            }
        }

        Button(
            onClick = { 
                if (isValid) viewModel.moveToNextStep() 
            },
            enabled = isValid,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(R.color.yazirengi),
                disabledContainerColor = colorResource(R.color.yazirengi).copy(alpha = 0.5f)
            )
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Devam Et")
                if (isValid) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }

        LaunchedEffect(registrationData.email, registrationData.password) {
            isValid = registrationData.email.contains("@gmail.com") && registrationData.password.length >= 8
        }
    }
}

@Composable
fun PersonalInfoStep(viewModel: RegisterViewModel) {
    val registrationData by viewModel.registrationData.collectAsState()
    var isValid by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth()
            .background(color = colorResource(R.color.arkaplan)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.logonew),
            contentDescription = "Personal Info Image",
            modifier = Modifier
                .size(250.dp)
                .padding(bottom = 24.dp),
            contentScale = ContentScale.Fit
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(colorResource(R.color.arkaplan)),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    "Kişisel Bilgiler",
                    style = MaterialTheme.typography.titleLarge,
                    color = colorResource(R.color.yazirengi),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = registrationData.name,
                    onValueChange = {
                        viewModel.clearError()
                        viewModel.updatePersonalInfo(
                            it, 
                            registrationData.surname,
                            registrationData.gender
                        )
                    },
                    label = { Text("Ad") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.small
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = registrationData.surname,
                    onValueChange = {
                        viewModel.clearError()
                        viewModel.updatePersonalInfo(
                            registrationData.name,
                            it,
                            registrationData.gender
                        )
                    },
                    label = { Text("Soyad") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.small
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "Cinsiyet",
                    style = MaterialTheme.typography.bodyLarge,
                    color = colorResource(R.color.yazirengi),
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = registrationData.gender == "Erkek",
                            onClick = {
                                viewModel.updatePersonalInfo(
                                    registrationData.name,
                                    registrationData.surname,
                                    "Erkek"
                                )
                            },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = MaterialTheme.colorScheme.primary
                            )
                        )
                        Text("Erkek", style = MaterialTheme.typography.bodyMedium)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = registrationData.gender == "Kadın",
                            onClick = {
                                viewModel.updatePersonalInfo(
                                    registrationData.name,
                                    registrationData.surname,
                                    "Kadın"
                                )
                            },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = MaterialTheme.colorScheme.primary
                            )
                        )
                        Text("Kadın", style = MaterialTheme.typography.bodyMedium)
                    }


                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = registrationData.gender == "Diğer",
                            onClick = {
                                viewModel.updatePersonalInfo(
                                    registrationData.name,
                                    registrationData.surname,
                                    "Diğer"
                                )
                            },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = MaterialTheme.colorScheme.primary
                            )
                        )
                        Text("Diğer", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }

    Button(
        onClick = { 
            if (isValid) viewModel.moveToNextStep() 
        },
        enabled = isValid,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = colorResource(R.color.yazirengi),
            disabledContainerColor = colorResource(R.color.yazirengi).copy(alpha = 0.5f)
        )
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Devam Et")
            if (isValid) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }

    LaunchedEffect(registrationData.name, registrationData.surname, registrationData.gender) {
        isValid = registrationData.name.isNotBlank() && 
                 registrationData.surname.isNotBlank() && 
                 registrationData.gender.isNotBlank()
    }
}

@Composable
fun BirthDateStep(viewModel: RegisterViewModel) {
    val registrationData by viewModel.registrationData.collectAsState()
    var isValid by remember { mutableStateOf(false) }
    val authState by viewModel.authState.collectAsState()

    Column(
        modifier = Modifier.fillMaxWidth()
            .background(color = colorResource(R.color.arkaplan)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.logonew),
            contentDescription = "Birthday Image",
            modifier = Modifier
                .size(250.dp)
                .padding(bottom = 24.dp),
            contentScale = ContentScale.Fit
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(colorResource(R.color.arkaplan)),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    "Doğum Tarihi",
                    style = MaterialTheme.typography.titleLarge,
                    color = colorResource(R.color.yazirengi),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedTextField(
                        value = registrationData.birthDay,
                        onValueChange = {
                            if (it.length <= 2) {
                                viewModel.clearError()
                                viewModel.updateBirthDate(
                                    it,
                                    registrationData.birthMonth,
                                    registrationData.birthYear
                                )
                            }
                        },
                        label = { Text("Gün") },
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = MaterialTheme.shapes.small
                    )

                    OutlinedTextField(
                        value = registrationData.birthMonth,
                        onValueChange = {
                            if (it.length <= 2) {
                                viewModel.clearError()
                                viewModel.updateBirthDate(
                                    registrationData.birthDay,
                                    it,
                                    registrationData.birthYear
                                )
                            }
                        },
                        label = { Text("Ay") },
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = MaterialTheme.shapes.small
                    )

                    OutlinedTextField(
                        value = registrationData.birthYear,
                        onValueChange = {
                            if (it.length <= 4) {
                                viewModel.clearError()
                                viewModel.updateBirthDate(
                                    registrationData.birthDay,
                                    registrationData.birthMonth,
                                    it
                                )
                            }
                        },
                        label = { Text("Yıl") },
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = MaterialTheme.shapes.small
                    )
                }
            }
        }

        // Hata mesajı gösterimi
        when (authState) {
            is AuthState.Error -> {
                Text(
                    text = (authState as AuthState.Error).message,
                    color = Color.Red,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    textAlign = TextAlign.Center
                )
            }
            else -> {}
        }

        Button(
            onClick = { 
                viewModel.clearError()
                if (isValid) {
                    viewModel.createUserWithEmailOnly()
                } else {
                    when {
                        registrationData.birthDay.isEmpty() || registrationData.birthMonth.isEmpty() || registrationData.birthYear.isEmpty() -> {
                            viewModel.showError("Lütfen doğum tarihinizi eksiksiz giriniz")
                        }
                        registrationData.birthDay.length != 2 || registrationData.birthMonth.length != 2 || registrationData.birthYear.length != 4 -> {
                            viewModel.showError("Lütfen doğum tarihinizi doğru formatta giriniz (GG/AA/YYYY)")
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(R.color.yazirengi),
                disabledContainerColor = colorResource(R.color.yazirengi).copy(alpha = 0.5f)
            )
        ) {
            if (authState is AuthState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White
                )
            } else {
                Text("Kayıt Ol")
            }
        }
    }

    LaunchedEffect(registrationData.birthDay, registrationData.birthMonth, registrationData.birthYear) {
        isValid = registrationData.birthDay.length == 2 && 
                 registrationData.birthMonth.length == 2 && 
                 registrationData.birthYear.length == 4
    }
}

