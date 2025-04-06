package com.example.pylearn
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.database.FirebaseDatabase
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
@Composable
fun StudyScreen(navController: NavController, userId: String) {
    var statistics by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    val database = FirebaseDatabase.getInstance().getReference("Statistics")
    LaunchedEffect(userId) {
        Log.d("StudyScreen", "userId: $userId")
        database.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val statsList = mutableListOf<Map<String, Any>>()
                for (child in snapshot.children) {
                    val stat = child.value as? Map<String, Any>
                    val idAuth = stat?.get("IDAuth")?.toString() ?: "N/A"
                    Log.d("StudyScreen", "IDAuth from Firebase: $idAuth")
                    if (stat != null && idAuth == userId) {
                        statsList.add(stat)
                    }
                }
                statistics = statsList
                Log.d("StudyScreen", "Filtered statistics: $statistics")
            }
        }.addOnFailureListener {
            Log.e("StudyScreen", "Ошибка загрузки статистики: ${it.message}")
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.probstud),
            contentDescription = "Background Image",
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.Center),
            contentScale = ContentScale.Crop
        )
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(top = 100.dp)
        ) {
            item {
                Image(
                    painter = painterResource(id = R.drawable.studpic),
                    contentDescription = "Изображение обучения",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(bottom = 16.dp),
                    contentScale = ContentScale.Fit
                )
            }
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = { navController.navigate("theory_screen/$userId") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .padding(bottom = 8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF346837)),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text("Теория", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { navController.navigate("interactive_tasks_screen/$userId") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .padding(bottom = 8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF346837)),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text("Интерактивные задания", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { navController.navigate("testing_screen/$userId") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .padding(bottom = 8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF346837)),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text("Тестирование", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
            item {
                if (statistics.isEmpty()) {
                    Text(
                        "Нет данных для отображения.",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                } else {
                    Column(modifier = Modifier.padding(8.dp)) {
                        statistics.forEach { stat ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF346837)),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Завершённые задания",
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = "${stat["CompletedTasks"]}",
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black,
                                            fontSize = 12.sp
                                        )
                                    }
                                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Завершённые тесты",
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = "${stat["CompletedTests"]}",
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black,
                                            fontSize = 12.sp
                                        )
                                    }
                                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Завершённая теория",
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = "${stat["CompletedTheory"]}",
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black,
                                            fontSize = 12.sp
                                        )
                                    }
                                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Правильные ответы",
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = "${stat["CorrectAnswers"]}",
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black,
                                            fontSize = 12.sp
                                        )
                                    }
                                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Общее количество попыток",
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = "${stat["TotalAttempts"]}",
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black,
                                            fontSize = 12.sp
                                        )
                                    }
                                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Неверные ответы",
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = "${stat["WrongAnswers"]}",
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black,
                                            fontSize = 12.sp
                                        )
                                    }
                                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Последняя активность",
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = "${stat["LastActivity"]}",
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            item {
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = { navController.navigate("ProfileScreen/$userId") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .padding(bottom = 16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF346837)),
                    shape = RoundedCornerShape(50)
                ) {
                    Text("Вернуться в профиль", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}