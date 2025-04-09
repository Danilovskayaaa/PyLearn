package com.example.pylearn

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


data class Answer(
    val Answer: String = "",
    val IsCorrect: Boolean = false
)

data class TestQuestion(
    val IDTest: Int = 0,
    val TestTask: String = "",
    val Category: String = "",
    val TestQuestion: String = "",
    val Answers: List<Answer> = listOf()
)

@Composable
fun TestScreen(
    navController: NavController,
    userId: String
) {
    val database = FirebaseDatabase.getInstance().getReference("Tests")
    val sharedPref = LocalContext.current.getSharedPreferences("user_test_preferences", Context.MODE_PRIVATE)
    var testQuestions by remember { mutableStateOf<List<TestQuestion>>(emptyList()) }
    var categories by remember { mutableStateOf<List<String>>(emptyList()) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var selectedQuestion by remember { mutableStateOf<TestQuestion?>(null) }
    var completedQuestions by remember { mutableStateOf<Map<Int, Boolean>>(emptyMap()) }
    var answeredQuestions by remember { mutableStateOf<Map<Int, Boolean>>(emptyMap()) }
    var correctAnswersPerCategory by remember { mutableStateOf<Map<String, Int>>(mutableMapOf()) }
    var totalQuestionsPerCategory by remember { mutableStateOf<Map<String, Int>>(mutableMapOf()) }
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val tempList = mutableListOf<TestQuestion>()
                val tempCategories = mutableSetOf<String>()
                val tempTotalQuestionsPerCategory = mutableMapOf<String, Int>()
                val tempAnsweredQuestions = mutableMapOf<Int, Boolean>()
                val tempCorrectAnswersPerCategory = mutableMapOf<String, Int>()
                for (questionSnapshot in snapshot.children) {
                    val question = questionSnapshot.getValue(TestQuestion::class.java)
                    question?.let {
                        tempList.add(it)
                        tempCategories.add(it.Category)
                        tempTotalQuestionsPerCategory[it.Category] = tempTotalQuestionsPerCategory.getOrDefault(it.Category, 0) + 1
                        val isAnswered = isAlreadyAnswered(sharedPref, userId, it.IDTest)
                        if (isAnswered) {
                            tempAnsweredQuestions[it.IDTest] = true
                            val isCorrect = isCorrectAnswer(sharedPref, userId, it.IDTest)
                            if (isCorrect) {
                                tempCorrectAnswersPerCategory[it.Category] = tempCorrectAnswersPerCategory.getOrDefault(it.Category, 0) + 1
                            }
                        }
                    }
                }
                testQuestions = tempList
                categories = tempCategories.toList()
                totalQuestionsPerCategory = tempTotalQuestionsPerCategory
                answeredQuestions = tempAnsweredQuestions
                correctAnswersPerCategory = tempCorrectAnswersPerCategory
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.backtest),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 100.dp, start = 16.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            when {
                selectedQuestion != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(bottom = 100.dp)
                    ) {
                        Text(
                            text = selectedQuestion!!.TestQuestion,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            color = Color(0xFF346837),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        val shuffledAnswers = remember { selectedQuestion!!.Answers.shuffled() }
                        shuffledAnswers.forEach { answer ->
                            val isAnswered = answeredQuestions[selectedQuestion!!.IDTest] != null
                            val isCorrectAnswer = sharedPref.getBoolean(
                                "correct_answer_${userId}_${selectedQuestion!!.IDTest}",
                                false
                            )

                            Button(
                                onClick = {

                                    if (!isAnswered) {
                                        val answerIsCorrect = answer.IsCorrect
                                        if (answerIsCorrect) {
                                            Toast.makeText(context, "Правильный ответ!", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "Неправильный ответ.", Toast.LENGTH_SHORT).show()
                                        }
                                        answeredQuestions = answeredQuestions + (selectedQuestion!!.IDTest to true)
                                        completedQuestions = completedQuestions + (selectedQuestion!!.IDTest to true)

                                        correctAnswersPerCategory = correctAnswersPerCategory.toMutableMap().apply {
                                            val currentCorrect = getOrDefault(selectedCategory, 0)
                                            this[selectedCategory!!] = currentCorrect + (if (answerIsCorrect) 1 else 0)
                                        }


                                        saveAnswerState(sharedPref, userId, selectedQuestion!!.IDTest, answerIsCorrect)
                                        updateStatistics(userId.toInt(), answerIsCorrect)
                                        selectedQuestion = null
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF346837)
                                )
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        answer.Answer,
                                        color = Color.White,
                                        modifier = Modifier.weight(1f)
                                    )

                                    if (isAnswered) {

                                        val icon = when {
                                            answer.IsCorrect -> Icons.Filled.CheckCircle
                                            else -> Icons.Filled.Close
                                        }

                                        val iconTint = when {
                                            answer.IsCorrect -> Color.Green
                                            else -> Color.Red
                                        }

                                        Icon(
                                            imageVector = icon,
                                            contentDescription = "Answer Status",
                                            tint = iconTint
                                        )
                                    }
                                }
                            }
                        }







                    }
                }
                selectedCategory != null -> {
                    Text(
                        text = "Вопросы: $selectedCategory",
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = Color(0xFF346837),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(bottom = 100.dp)
                    ) {
                        items(testQuestions.filter { it.Category == selectedCategory }) { question ->
                            val isAnswered = answeredQuestions[question.IDTest] != null
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                                    .clickable { selectedQuestion = question }
                                    .border(1.dp, Color.Gray),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        question.TestQuestion,
                                        color = Color(0xFF346837),
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (isAnswered) {
                                        val isCorrectAnswer = sharedPref.getBoolean("correct_answer_${userId}_${question.IDTest}", false)
                                        val isWrongAnswer = sharedPref.getBoolean("wrong_answer_${userId}_${question.IDTest}", false)

                                        Icon(
                                            imageVector = if (isCorrectAnswer) Icons.Filled.CheckCircle

                                            else Icons.Filled.Close,
                                            contentDescription = "Answered",
                                            tint = if (isCorrectAnswer) Color.Green else Color.Red
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                else -> {
                    Text(
                        text = "Выберите категорию",
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = Color(0xFF346837),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(bottom = 100.dp)
                    ) {
                        items(categories) { category ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                                    .clickable { selectedCategory = category }
                                    .border(1.dp, Color.Gray),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        category,
                                        color = Color(0xFF346837),
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f)
                                    )

                                    val correctAnswersInCategory = correctAnswersPerCategory[category] ?: 0
                                    val totalInCategory = totalQuestionsPerCategory[category] ?: 0
                                    val scorePercentage = if (totalInCategory > 0) (correctAnswersInCategory.toFloat() / totalInCategory * 100).toInt() else 0

                                    Text(
                                        text = "$correctAnswersInCategory / $totalInCategory ($scorePercentage%)",
                                        fontSize = 16.sp,
                                        color = if (completedQuestions[category.hashCode()] == true) Color.Green else Color.Gray,
                                        modifier = Modifier.padding(start = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        ) {
            Button(
                onClick = {
                    when {
                        selectedQuestion != null -> selectedQuestion = null
                        selectedCategory != null -> selectedCategory = null
                        else -> navController.navigate("StudyScreen/$userId")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF346837))
            ) {
                Text(
                    text = when {
                        selectedCategory != null -> "Назад"
                        selectedQuestion != null -> "Назад"
                        else -> "Назад в обучение"
                    },
                    color = Color.White
                )
            }

        }
    }
}



fun saveAnswerState(sharedPref: SharedPreferences, userId: String, testId: Int, isCorrect: Boolean) {
    val editor = sharedPref.edit()
    editor.putBoolean("answered_${userId}_$testId", true)

    if (isCorrect) {
        editor.putBoolean("correct_answer_${userId}_$testId", true)
        editor.putBoolean("wrong_answer_${userId}_$testId", false)
    } else {
        editor.putBoolean("correct_answer_${userId}_$testId", false)
        editor.putBoolean("wrong_answer_${userId}_$testId", true)
    }

    editor.apply()
}

fun isCorrectAnswer(sharedPref: SharedPreferences, userId: String, testId: Int): Boolean {
    return sharedPref.getBoolean("correct_answer_${userId}_$testId", false)
}

fun isAlreadyAnswered(sharedPref: SharedPreferences, userId: String, testId: Int): Boolean {
    return sharedPref.getBoolean("answered_${userId}_$testId", false)
}

fun isWrongAnswer(sharedPref: SharedPreferences, userId: String, testId: Int): Boolean {
    return sharedPref.getBoolean("wrong_answer_${userId}_$testId", false)
}

fun updateStatistics(userId: Int, isCorrect: Boolean) {
    val database = FirebaseDatabase.getInstance().reference
    val statsRef = database.child("Statistics")
    val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    statsRef.get()
        .addOnSuccessListener { snapshot ->
            var found = false
            snapshot.children.forEach { record ->
                val idAuth = record.child("IDAuth").getValue(Int::class.java)
                if (idAuth != null && idAuth == userId) {
                    found = true
                    val correctAnswers = record.child("CompletedTests").getValue(Int::class.java) ?: 0
                    val wrongAnswers = record.child("WrongAnswers").getValue(Int::class.java) ?: 0
                    val updatedCorrectAnswers = if (isCorrect) correctAnswers + 1 else correctAnswers
                    val updatedWrongAnswers = if (!isCorrect) wrongAnswers + 1 else wrongAnswers

                    val updates = mapOf(
                        "CompletedTests" to updatedCorrectAnswers,
                        "WrongAnswers" to updatedWrongAnswers,
                        "LastActivity" to currentDate
                    )

                    statsRef.child(record.key!!).updateChildren(updates)
                }
            }

            if (!found) {
                Log.e("Firebase", "Статистика для пользователя с IDAuth $userId не найдена")
            }
        }
        .addOnFailureListener { exception ->
            Log.e("Firebase", "Ошибка при получении статистики", exception)
        }
}