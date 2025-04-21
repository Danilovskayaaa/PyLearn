package com.example.pylearn
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
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
import androidx.navigation.NavController
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.*
import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
fun getUserSharedPreferences(context: Context, userId: String): SharedPreferences {
    return context.getSharedPreferences("TheoryPrefs_$userId", Context.MODE_PRIVATE)
}
fun isTheoryCompleted(context: Context, userId: String, theoryId: String): Boolean {
    val preferences = context.getSharedPreferences("TheoryPrefs_$userId", Context.MODE_PRIVATE)
    return preferences.getBoolean("completed_theory_$theoryId", false)
}
fun saveTheoryCompletion(context: Context, userId: String, theoryId: String, isCompleted: Boolean) {
    val preferences = context.getSharedPreferences("TheoryPrefs_$userId", Context.MODE_PRIVATE)
    if (isTheoryCompleted(context, userId, theoryId)) return // Не обновляем, если уже завершено
    val editor = preferences.edit()
    editor.putBoolean("completed_theory_$theoryId", isCompleted)
    editor.apply()
}
data class Theory(
    val Category: String = "",
    val IDTheory: Int = 0,
    val Theory: String = "",
    val Title: String = "",
    var isCompleted: Boolean = false
)
class TheoryViewModel : androidx.lifecycle.ViewModel() {
    private val database = FirebaseDatabase.getInstance().getReference("Theory")
    var theoryList by mutableStateOf<List<Theory>>(emptyList())
        private set
    var categories by mutableStateOf<List<String>>(emptyList())
        private set
    var titles by mutableStateOf<List<String>>(emptyList())
        private set
    var selectedCategory by mutableStateOf<String?>(null)
        private set
    var selectedTheory by mutableStateOf<Theory?>(null)
        private set
    init {
        loadTheory()
    }
    fun loadTheory() {
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val tempList = mutableListOf<Theory>()
                for (theorySnapshot in snapshot.children) {
                    val theory = theorySnapshot.getValue(Theory::class.java)
                    theory?.let { tempList.add(it) }
                }
                theoryList = tempList
                categories = tempList.map { it.Category }.distinct()
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }
    fun selectCategory(category: String) {
        selectedCategory = category
        titles = theoryList.filter { it.Category == category }.map { it.Title }
        selectedTheory = null
    }
    fun selectTheory(title: String) {
        selectedTheory = theoryList.firstOrNull { it.Title == title }
        selectedTheory?.isCompleted = true
    }
    fun resetSelection() {
        selectedCategory = null
        titles = emptyList()
        selectedTheory = null
    }
}
fun getCompletionStatsForCategory(context: Context, userId: String, category: String, theoryList: List<Theory>): Pair<Int, Int> {
    val totalLectures = theoryList.count { it.Category == category }
    val completedLectures = theoryList.count { it.Category == category && isTheoryCompleted(context, userId, it.IDTheory.toString()) }
    return Pair(completedLectures, totalLectures)
}
fun incrementCompletedTheory(userId: Int) {
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
                    Log.d("Firebase", "Данные найдены для пользователя с IDAuth = $userId")
                    val idStat = record.child("IDStat").getValue(Int::class.java) ?: return@forEach
                    val completedTheory = record.child("CompletedTheory").getValue(Int::class.java) ?: 0
                    val updatedTheory = completedTheory + 1
                    val updates = mapOf(
                        "CompletedTheory" to updatedTheory,
                        "LastActivity" to currentDate
                    )
                    statsRef.child(record.key!!).updateChildren(updates)
                        .addOnSuccessListener {
                            Log.d("Firebase", "Успешно обновлены данные для IDStat: $idStat")
                        }
                        .addOnFailureListener { exception ->
                            Log.e("Firebase", "Ошибка при обновлении данных для IDStat: $idStat", exception)
                        }
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
@Composable
fun TheoryScreen(
    navController: NavController,
    userId: String,
    viewModel: TheoryViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val categories = viewModel.categories
    val selectedCategory = viewModel.selectedCategory
    val titles = viewModel.titles
    val selectedTheory = viewModel.selectedTheory
    val context = LocalContext.current
    var isTheoryRead by remember { mutableStateOf(false) }
    LaunchedEffect(isTheoryRead) {
        if (isTheoryRead) {
            incrementCompletedTheory(userId.toInt())
            isTheoryRead = false
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.backtheory),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 100.dp, start = 16.dp, end = 16.dp)
        ) {
            when {
                selectedTheory != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = selectedTheory!!.Title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            color = Color(0xFF346837),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = selectedTheory!!.Theory,
                            fontSize = 16.sp,
                            color = Color(0xFF1A2D1B)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                if (!isTheoryCompleted(context, userId, selectedTheory!!.IDTheory.toString())) {
                                    saveTheoryCompletion(context, userId, selectedTheory!!.IDTheory.toString(), true)
                                    viewModel.selectCategory(selectedCategory ?: "")
                                    isTheoryRead = true
                                } else {
                                    Toast.makeText(context, "Лекция уже пройдена", Toast.LENGTH_SHORT).show()
                                    viewModel.selectCategory(selectedCategory ?: "")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF346837))
                        ) {
                            Text("Назад")
                        }

                    }
                }
                selectedCategory != null -> {
                    Text(
                        text = "Лекции: $selectedCategory",
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = Color(0xFF346837),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    val allLecturesCompleted = viewModel.theoryList
                        .filter { it.Category == selectedCategory }
                        .all { isTheoryCompleted(context, userId, it.IDTheory.toString()) }
                    LazyColumn {
                        items(titles) { title ->
                            val theory = viewModel.theoryList.firstOrNull { it.Title == title }
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                                    .clickable { theory?.let { viewModel.selectTheory(it.Title) } },
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(4.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        title,
                                        color = Color(0xFF346837),
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (theory != null && isTheoryCompleted(context, userId, theory.IDTheory.toString())) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Completed",
                                            tint = Color(0xFF346837)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.resetSelection() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF346837))
                    ) {
                        Text("Назад")
                    }
                    if (allLecturesCompleted) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Completed Category",
                            tint = Color(0xFF346837),
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
                else -> {
                    Text(
                        text = "Категории",
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = Color(0xFF346837),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    LazyColumn {
                        items(categories) { category ->
                            val (completedLectures, totalLectures) = getCompletionStatsForCategory(context, userId, category, viewModel.theoryList)
                            val completionPercentage = if (totalLectures > 0) (completedLectures * 100) / totalLectures else 0
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                                    .clickable { viewModel.selectCategory(category) },
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(4.dp),
                                shape = RoundedCornerShape(12.dp)
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
                                    if (totalLectures > 0) {
                                        Text(
                                            text = "${completedLectures}/${totalLectures} (${completionPercentage}%)",
                                            color = Color.Gray,
                                            fontSize = 14.sp
                                        )
                                    }
                                    if (completedLectures == totalLectures && totalLectures > 0) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Completed Category",
                                            tint = Color(0xFF346837)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { navController.navigate("StudyScreen/$userId") },
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF346837))
                    ) {
                        Text("Вернуться на экран обучения")
                    }
                }
            }
        }
    }
}