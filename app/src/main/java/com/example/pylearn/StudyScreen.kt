package com.example.pylearn
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material3.HorizontalDivider

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
                .padding(top = 70.dp)
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
                        onClick = { navController.navigate("TheoryScreen/$userId") },
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
                        onClick = { navController.navigate("TaskScreen/$userId") },
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
                        onClick = { navController.navigate("TestScreen/$userId") },
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
                Text(
                    text = "Статистика",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier

                        .fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
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

                                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                                        Text(
                                            "Завершённые задания",
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            "${stat["CompletedTasks"]}",
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontSize = 12.sp
                                        )
                                    }
                                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                                        Text(
                                            "Завершённые тесты",
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            "${stat["CompletedTests"]}",
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontSize = 12.sp
                                        )
                                    }
                                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                                        Text(
                                            "Завершённая теория",
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            "${stat["CompletedTheory"]}",
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontSize = 12.sp
                                        )
                                    }
                                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                                        Text(
                                            "Правильные ответы",
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            "${stat["CorrectAnswers"]}",
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontSize = 12.sp
                                        )
                                    }
                                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                                        Text(
                                            "Общее количество попыток",
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            "${stat["TotalAttempts"]}",
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontSize = 12.sp
                                        )
                                    }
                                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                                        Text(
                                            "Неверные ответы",
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            "${stat["WrongAnswers"]}",
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontSize = 12.sp
                                        )
                                    }
                                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                                        Text(
                                            "Последняя активность",
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            "${stat["LastActivity"]}",
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontSize = 12.sp
                                        )
                                    }
                                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                                    Text(
                                        text = "Гистограмма статистики",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier

                                            .fillMaxWidth(),
                                        textAlign = TextAlign.Center
                                    )
                                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                                    val statsMap: Map<String, Float> = mapOf(
                                        "📋" to (stat["CompletedTasks"]?.toString()?.toFloatOrNull() ?: 0f),
                                        "📝" to (stat["CompletedTests"]?.toString()?.toFloatOrNull() ?: 0f),
                                        "📚" to (stat["CompletedTheory"]?.toString()?.toFloatOrNull() ?: 0f),
                                        "✅" to (stat["CorrectAnswers"]?.toString()?.toFloatOrNull() ?: 0f),
                                        "❌" to (stat["WrongAnswers"]?.toString()?.toFloatOrNull() ?: 0f),
                                        "🔄" to (stat["TotalAttempts"]?.toString()?.toFloatOrNull() ?: 0f)
                                    )
                                    val maxStat = statsMap.maxOfOrNull { it.value } ?: 1f
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(180.dp)
                                    ) {
                                        statsMap.toList().forEachIndexed { index, (label, value) ->
                                            val barHeight = (150 * (value / maxStat)).dp.coerceAtLeast(8.dp)
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.BottomStart)
                                                    .offset(x = (index * 60).dp)
                                                    .height(barHeight)
                                                    .width(40.dp)
                                                    .background(Color(0xFF4CAF50), shape = RoundedCornerShape(8.dp))
                                            )
                                            Text(
                                                text = label,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                textAlign = TextAlign.Center,
                                                color = Color.Black,
                                                modifier = Modifier
                                                    .align(Alignment.BottomStart)
                                                    .offset(x = (index * 60 + 10).dp)
                                                    .padding(top = 8.dp)
                                            )
                                            Text(
                                                text = value.toInt().toString(),
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                modifier = Modifier
                                                    .align(Alignment.BottomStart)
                                                    .offset(x = (index * 60 + 10).dp)
                                                    .padding(bottom = (barHeight.value + 4).dp)
                                            )
                                        }
                                    }
                                }
                                }
                                }
                            }
                        }
            }
            item {
                Spacer(modifier = Modifier.height(10.dp))
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