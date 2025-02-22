package com.example.goalmate.prenstatntion.registerscreen

import android.content.Context
import android.os.Handler
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.yeniproje.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun RegisterScreen() {
    var name by remember { mutableStateOf("") }
    var surname by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var selectedGender by remember { mutableStateOf("Erkek") }
    var errorMessage by remember { mutableStateOf("") }
    var day by remember { mutableStateOf("") }
    var month by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Kayıt Ol", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Black)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Ad") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = surname,
            onValueChange = { surname = it },
            label = { Text("Soyad") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("E-posta") },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Şifre") },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Password),
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(30.dp))

        Text("Cinsiyet", modifier = Modifier.align(Alignment.Start), fontStyle = FontStyle.Normal)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            RadioButton(
                selected = selectedGender == "Erkek",
                onClick = { selectedGender = "Erkek" },
                colors = RadioButtonDefaults.colors(colorResource(R.color.turkuaz))
            )
            Text(text = "Erkek", modifier = Modifier.padding(start = 4.dp))

            Spacer(modifier = Modifier.width(16.dp))

            RadioButton(
                selected = selectedGender == "Kadın",
                onClick = { selectedGender = "Kadın" },
                colors = RadioButtonDefaults.colors(colorResource(R.color.turkuaz))
            )
            Text(text = "Kadın", modifier = Modifier.padding(start = 4.dp))

            Spacer(modifier = Modifier.width(16.dp))

            RadioButton(
                selected = selectedGender == "Diğer",
                onClick = { selectedGender = "Diğer" },
                colors = RadioButtonDefaults.colors(colorResource(R.color.turkuaz))
            )
            Text(text = "Diğer", modifier = Modifier.padding(start = 4.dp))
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text("Doğum Tarihi", modifier = Modifier.align(Alignment.Start))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            OutlinedTextField(
                value = day,
                onValueChange = {
                    if (it.isEmpty() || (it.length in 1..2 && it.toIntOrNull() in 1..31)) {
                        day = it
                    }
                    if (!isValidDate(day, month, year)) {
                        errorMessage = "Geçersiz doğum tarihi!"
                    } else {
                        errorMessage = ""
                    }
                },
                label = { Text("Gün") },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(0.2f),
                maxLines = 2
            )

            Spacer(modifier = Modifier.width(8.dp))

            OutlinedTextField(
                value = month,
                onValueChange = {
                    if (it.isEmpty() || (it.length in 1..2 && it.toIntOrNull() in 1..12)) {
                        month = it
                    }
                    if (!isValidDate(day, month, year)) {
                        errorMessage = "Geçersiz doğum tarihi!"
                    } else {
                        errorMessage = ""
                    }
                },
                label = { Text("Ay") },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(0.2f),
                maxLines = 2
            )

            Spacer(modifier = Modifier.width(8.dp))

            OutlinedTextField(
                value = year,
                onValueChange = {
                    if (it.isNotEmpty() && it.length in 1..4) {
                        year = it
                    }
                    if (!isValidDate(day, month, year)) {
                        errorMessage = "Geçersiz doğum tarihi!"
                    } else {
                        errorMessage = ""
                    }
                },
                label = { Text("Yıl") },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(0.4f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (errorMessage.isNotEmpty()) {
            Text(text = errorMessage, color = Color.Red)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { registerUser(name, surname, email, password, "$day/$month/$year", context, selectedGender, day, month, year) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.turkuaz))
        ) {
            Text(text = "Kayıt Ol")
        }
    }
}

fun isValidDate(day: String, month: String, year: String): Boolean {
    if (day.isEmpty() || month.isEmpty() || year.isEmpty()) return false

    val dayInt = day.toIntOrNull()
    val monthInt = month.toIntOrNull()
    val yearInt = year.toIntOrNull()

    if (dayInt == null || monthInt == null || yearInt == null) return false

    if (yearInt < 1990 || yearInt > 2016) return false

    if (monthInt !in 1..12 || dayInt !in 1..31) return false

    if (monthInt == 4 || monthInt == 6 || monthInt == 9 || monthInt == 11) {
        if (dayInt > 30) return false
    }

    if (monthInt == 2) {
        val isLeapYear = yearInt % 4 == 0 && (yearInt % 100 != 0 || yearInt % 400 == 0)
        if ((isLeapYear && dayInt > 29) || (!isLeapYear && dayInt > 28)) return false
    }

    return true
}

private fun registerUser(
    name: String,
    surname: String,
    email: String,
    password: String,
    birthDate: String,
    context: Context,
    selectedGender: String,
    day: String,
    month: String,
    year: String
) {
    if (name.isNotEmpty() && surname.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() && isValidDate(day, month, year)) {
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = FirebaseAuth.getInstance().currentUser
                    user?.sendEmailVerification()?.addOnCompleteListener { verificationTask ->
                        if (verificationTask.isSuccessful) {
                            Toast.makeText(context, "Doğrulama e-postası gönderildi!", Toast.LENGTH_LONG).show()
                            val userInfo = hashMapOf(
                                "name" to name,
                                "surname" to surname,
                                "gender" to selectedGender,
                                "birthDate" to birthDate
                            )

                            startVerificationCheck(context, user, userInfo)

                        } else {
                            Toast.makeText(context, "Doğrulama e-postası gönderilemedi: ${verificationTask.exception?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                } else {
                    Toast.makeText(context, "Kayıt Başarısız: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    } else {
        Toast.makeText(context, "Tüm alanları doğru şekilde doldurun!", Toast.LENGTH_LONG).show()
    }
}

fun saveUserDataAfterVerification(uid: String?, userInfo: HashMap<String, String>, context: Context) {
    val user = FirebaseAuth.getInstance().currentUser
    if (user?.isEmailVerified == true) {
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid!!)
            .set(userInfo)
            .addOnSuccessListener {
                Toast.makeText(context, "Kayıt Başarılı!", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Kayıt Başarısız: ${it.message}", Toast.LENGTH_LONG).show()
            }
    } else {
        Toast.makeText(context, "Lütfen e-posta adresinizi doğrulayın.", Toast.LENGTH_LONG).show()
    }
}

@Composable
fun MainScreen() {
    Box(modifier = Modifier.fillMaxSize()) {
        WavyBackground()
        RegisterScreen()
    }
}

@Composable
fun WavyBackground() {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.35f)
    ) {
        val path = androidx.compose.ui.graphics.Path().apply {
            moveTo(0f, size.height * 0.7f)
            cubicTo(
                size.width * 0.25f, size.height * 0.5f,
                size.width * 0.75f, size.height,
                size.width, size.height * 0.8f
            )
            lineTo(size.width, 0f)
            lineTo(0f, 0f)
            close()
        }

        drawPath(
            path = path,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF4CAF50), Color(0xFF4CAF50))
            )
        )
    }
}

fun startVerificationCheck(context: Context, user: FirebaseUser, userInfo: HashMap<String, String>, onay: Boolean = false) {
    val handler = Handler()
    var runnable: Runnable? = null

    runnable = object : Runnable {
        override fun run() {
            if (onay) {
                checkEmailVerificationStatus(context, user, userInfo, handler)
                handler.postDelayed(this, 5000)
            } else {
                runnable?.let { handler.removeCallbacks(it) }
            }
        }
    }
}

fun checkEmailVerificationStatus(context: Context, user: FirebaseUser, userInfo: HashMap<String, String>, handler: Handler) {
    user.reload()
    if (user.isEmailVerified) {
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(user.uid)
            .set(userInfo)
            .addOnSuccessListener {
                Toast.makeText(context, "Kayıt Başarılı!", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Kayıt Başarısız: ${it.message}", Toast.LENGTH_LONG).show()
            }
        startVerificationCheck(context, user, userInfo, onay = true)
    } else {
        Toast.makeText(context, "E-posta doğrulama yapılmadı, lütfen doğrulama yapın.", Toast.LENGTH_SHORT).show()
    }
}

fun LoginUser(email: String, password: String) {
    FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
        if (task.isSuccessful) {
            // kullanıcı başarılı bir şekilde giriş yaptı
        } else {
            // kullanıcı giriş yapamadı
        }
    }
}


