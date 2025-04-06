package com.example.pylearn
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.google.firebase.database.*
import java.io.ByteArrayOutputStream
import android.util.Base64
fun convertBitmapToByteArray(bitmap: Bitmap): ByteArray {
    val byteArrayOutputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
    return byteArrayOutputStream.toByteArray()
}
fun saveAvatarToDatabase(userId: String, bitmap: Bitmap, onComplete: (Boolean) -> Unit) {
    val byteArray = convertBitmapToByteArray(bitmap)
    val base64String = Base64.encodeToString(byteArray, Base64.DEFAULT)
    val database = FirebaseDatabase.getInstance().getReference("Auth")
    database.child(userId).child("Avatar").setValue(base64String).addOnCompleteListener {
        if (it.isSuccessful) {
            onComplete(true)
        } else {
            onComplete(false)
        }
    }
}
fun loadAvatarFromDatabase(userId: String, onComplete: (Bitmap?) -> Unit) {
    val database = FirebaseDatabase.getInstance().getReference("Auth")
    database.child(userId).child("Avatar").get().addOnSuccessListener { snapshot ->
        val base64String = snapshot.value as? String
        if (base64String != null) {
            val byteArray = Base64.decode(base64String, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
            onComplete(bitmap)
        } else {
            onComplete(null)
        }
    }.addOnFailureListener {
        onComplete(null)
    }
}
@Composable
fun ProfileScreen(navController: NavController, userId: String) {
    var userInfo by remember { mutableStateOf<Map<String, String>?>(null) }
    var avatarBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var avatarUri by remember { mutableStateOf<Uri?>(null) }
    val database = FirebaseDatabase.getInstance().getReference("Auth")
    LaunchedEffect(userId) {
        database.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                for (child in snapshot.children) {
                    val user = child.value as? Map<String, Any>
                    if (user != null && user["IDAuth"].toString() == userId) {
                        userInfo = mapOf(
                            "FirstName" to user["FirstName"].toString(),
                            "LastName" to user["LastName"].toString(),
                            "Patronomyc" to user["Patronomyc"].toString(),
                            "Email" to user["Email"].toString(),
                            "Login" to user["Login"].toString(),
                            "Password" to user["Password"].toString()
                        )
                        loadAvatarFromDatabase(userId) { bitmap ->
                            avatarBitmap = bitmap
                        }
                        return@addOnSuccessListener
                    }
                }
            }
        }.addOnFailureListener {
            Log.e("ProfileScreen", "Ошибка загрузки данных: ${it.message}")
        }
    }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val bitmap = BitmapFactory.decodeStream(navController.context.contentResolver.openInputStream(it))
            saveAvatarToDatabase(userId, bitmap) { success ->
                if (success) {
                    avatarBitmap = bitmap
                }
            }
        }
    }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.probprof),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 70.dp)
                .verticalScroll(rememberScrollState()),

            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(16.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.avatar),
                    contentDescription = "Рамка аватара",
                    modifier = Modifier.fillMaxSize()
                )
                avatarBitmap?.let {
                    Image(
                        painter = rememberImagePainter(it),
                        contentDescription = "Аватар",
                        modifier = Modifier
                            .size(80.dp)
                            .align(Alignment.Center)
                            .clip(RoundedCornerShape(16.dp))
                    )
                } ?: run {
                    Image(
                        painter = painterResource(id = R.drawable.account_user),
                        contentDescription = "Аватар",
                        modifier = Modifier
                            .size(80.dp)
                            .align(Alignment.Center)
                            .clip(RoundedCornerShape(16.dp))
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text("Данные пользователя",color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(0.9f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF346837))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    userInfo?.let { data ->
                        Text("Имя:", fontWeight = FontWeight.Bold)
                        Text(data["FirstName"] ?: "Неизвестно")
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        Text("Фамилия:", fontWeight = FontWeight.Bold)
                        Text(data["LastName"] ?: "Неизвестно")
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        Text("Отчество:", fontWeight = FontWeight.Bold)
                        Text(data["Patronomyc"] ?: "Неизвестно")
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        Text("Электронная почта:", fontWeight = FontWeight.Bold)
                        Text(data["Email"] ?: "Неизвестно")
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        Text("Логин:", fontWeight = FontWeight.Bold)
                        Text(data["Login"] ?: "Неизвестно")
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        Text("Пароль:", fontWeight = FontWeight.Bold)
                        Text("********")
                    } ?: run {
                        CircularProgressIndicator()
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Button(
                onClick = { launcher.launch("image/*") },
                modifier = Modifier.fillMaxWidth(0.8f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF346837))
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.camera_add),
                    contentDescription = "Фото",
                    modifier = Modifier.size(20.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Изменить фотографию",
                    color = Color.White,
                    style = MaterialTheme.typography.labelLarge
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Навигация по приложению",color = Color.White)
            Spacer(modifier = Modifier.height(4.dp))
            Button(
                onClick = {  navController.navigate("StudyScreen/${userId}") },
                modifier = Modifier.fillMaxWidth(0.8f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF346837))
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.study_icon),
                    contentDescription = "Обучение",
                    modifier = Modifier.size(20.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Обучение",
                    color = Color.White,
                    style = MaterialTheme.typography.labelLarge
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {  },
                modifier = Modifier.fillMaxWidth(0.8f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF346837))
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.icon_settings),
                    contentDescription = "Настройки и управление",
                    modifier = Modifier.size(20.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Настройки и управление",
                    color = Color.White,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}
