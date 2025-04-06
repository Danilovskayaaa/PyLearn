@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.pylearn
import android.util.Log
import androidx.compose.foundation.Image
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
import androidx.navigation.NavHostController
import com.google.firebase.database.*
val DarkGreen = Color(0xFF1A3E1D)
@Composable
fun AuthScreen(navController: NavHostController) {
    LoginScreen(navController) { userId ->

        navController.navigate("ProfileScreen/$userId")
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavHostController, onLoginSuccess: (String) -> Unit) {
    var login by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    val database = FirebaseDatabase.getInstance().getReference("Auth")
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
                text = "Авторизация",
                style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
            )
            Spacer(modifier = Modifier.height(16.dp))
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
                    authenticateUser(login, password, database, onLoginSuccess, ::setError)
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
                    authenticateUser(login, password, database, onLoginSuccess, ::setError)
                },
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .clip(RoundedCornerShape(50.dp)),
                colors = ButtonDefaults.buttonColors(containerColor = DarkGreen)
            ) {
                Text("Войти", color = Color.White)
            }
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = { navController.navigate("RegisterScreen") }) {
                Text("Зарегистрироваться", color = Color.White)
            }
            if (errorMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(errorMessage, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
fun authenticateUser(
    login: String,
    password: String,
    database: DatabaseReference,
    onSuccess: (String) -> Unit,
    onError: (String) -> Unit
) {
    database.get().addOnSuccessListener { snapshot ->
        if (snapshot.exists()) {
            for (child in snapshot.children) {
                val user = child.value as? Map<String, Any>
                if (user != null && user["Login"].toString() == login && user["Password"].toString() == password) {
                    onSuccess(user["IDAuth"].toString())
                    return@addOnSuccessListener
                }
            }
            onError("Неверный логин или пароль")
        } else {
            onError("Данные не найдены")
        }
    }.addOnFailureListener {
        onError("Ошибка подключения к базе данных")
    }
}
fun setError(message: String) {
    Log.e("AuthError", message)
}