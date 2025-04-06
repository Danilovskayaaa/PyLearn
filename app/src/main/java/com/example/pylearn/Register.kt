package com.example.pylearn
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DatabaseReference
val DarkGreenn = Color(0xFF1A3E1D)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(navController: NavController) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var patronymic by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var login by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    val database = FirebaseDatabase.getInstance().getReference("Auth")
    val onRegistrationSuccess: (String) -> Unit = { userId ->
        navController.navigate("ProfileScreen/$userId")
    }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo",
                modifier = Modifier.size(200.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Регистрация",
                style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
            )
            Spacer(modifier = Modifier.height(16.dp))
            TextField(
                value = firstName,
                onValueChange = { firstName = it },
                placeholder = { Text("Имя", color = Color.White) },
                singleLine = true,
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = Color.White.copy(alpha = 0.2f),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                ),
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .clip(RoundedCornerShape(50.dp))
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = lastName,
                onValueChange = { lastName = it },
                placeholder = { Text("Фамилия", color = Color.White) },
                singleLine = true,
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = Color.White.copy(alpha = 0.2f),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                ),
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .clip(RoundedCornerShape(50.dp))
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = patronymic,
                onValueChange = { patronymic = it },
                placeholder = { Text("Отчество", color = Color.White) },
                singleLine = true,
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = Color.White.copy(alpha = 0.2f),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                ),
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .clip(RoundedCornerShape(50.dp))
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = email,
                onValueChange = { email = it },
                placeholder = { Text("Электронная почта", color = Color.White) },
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = Color.White.copy(alpha = 0.2f),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                ),
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .clip(RoundedCornerShape(50.dp))
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = login,
                onValueChange = { login = it },
                placeholder = { Text("Логин", color = Color.White) },
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = Color.White.copy(alpha = 0.2f),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                ),
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .clip(RoundedCornerShape(50.dp))
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = password,
                onValueChange = { password = it },
                placeholder = { Text("Пароль", color = Color.White) },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    registerUser(firstName, lastName, patronymic, email, login, password, database, onRegistrationSuccess, ::setError)
                }),
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = Color.White.copy(alpha = 0.2f),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                ),
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .clip(RoundedCornerShape(50.dp))
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    registerUser(firstName, lastName, patronymic, email, login, password, database, onRegistrationSuccess, ::setAuthError)
                },
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .clip(RoundedCornerShape(50.dp)),
                colors = ButtonDefaults.buttonColors(containerColor = DarkGreen)
            ) {
                Text("Зарегистрироваться", color = Color.White)
            }
            Spacer(modifier = Modifier.height(8.dp))
            if (errorMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(errorMessage, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
fun registerUser(
    firstName: String,
    lastName: String,
    patronymic: String,
    email: String,
    login: String,
    password: String,
    database: DatabaseReference,
    onSuccess: (String) -> Unit,
    onError: (String) -> Unit
) {
    val usersRef = database
    usersRef.get().addOnSuccessListener { snapshot ->
        val idsInDatabase = if (snapshot.exists()) {
            val usersMap = snapshot.value as? Map<String, Any> ?: emptyMap()
            usersMap.keys.mapNotNull { it.toLongOrNull() }
        } else {
            emptyList<Long>()
        }
        fun generateUniqueId(): String {
            val newIdAuth = (1_000_000_000..9_999_999_999).random().toString()
            return if (newIdAuth in idsInDatabase.map { it.toString() }) {
                generateUniqueId()
            } else {
                newIdAuth
            }
        }
        val newIdAuth = generateUniqueId()
        val newUser = mapOf(
            "Email" to email,
            "FirstName" to firstName,
            "IDAuth" to newIdAuth,
            "LastName" to lastName,
            "Login" to login,
            "Password" to password,
            "Patronymic" to patronymic
        )
        usersRef.child(newIdAuth).setValue(newUser)
            .addOnSuccessListener { onSuccess(newIdAuth) }
            .addOnFailureListener { onError("Ошибка регистрации") }
    }.addOnFailureListener {
        onError("Ошибка при получении данных пользователей")
    }
}
fun setAuthError(message: String) {
    Log.e("AuthError", message)
}


