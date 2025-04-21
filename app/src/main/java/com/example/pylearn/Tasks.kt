package com.example.pylearn
import android.content.SharedPreferences
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
data class Task(
    val IDTest: Int = 0,
    val category: String = "",
    val description: String = "",
    val expectedOutput: String = "",
    val testInput: String = "",
    val title: String = "",
    var isCorrect: Boolean = false
)
fun updateStatisticsCode(userId: Int, isCorrect: Boolean) {
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
                    val correctAnswers = record.child("CompletedTasks").getValue(Int::class.java) ?: 0
                    val wrongAnswers = record.child("WrongAnswers").getValue(Int::class.java) ?: 0
                    val updatedCorrectAnswers = if (isCorrect) correctAnswers + 1 else correctAnswers
                    val updatedWrongAnswers = if (!isCorrect) wrongAnswers + 1 else wrongAnswers
                    val updates = mapOf(
                        "CompletedTasks" to updatedCorrectAnswers,
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
fun getCompletionStatsForCategory(sharedPreferences: SharedPreferences, userId: Int, category: String, tasks: List<Task>): Pair<Int, Int> {
    val totalTasks = tasks.count { it.category == category }
    val completedTasks = tasks.count { it.category == category && it.isCorrect }

    return Pair(completedTasks, totalTasks)
}
data class CodeExecutionResult(
    val output: String = "",
    var error: String = ""
)
class TaskViewModel(private val sharedPreferences: SharedPreferences,userId: Int) : ViewModel() {
    private val database = FirebaseDatabase.getInstance().getReference("Tasks")
    private val _categoryCompletion = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val categoryCompletion: StateFlow<Map<String, Boolean>> = _categoryCompletion
    private val _selectedTask = MutableStateFlow(Task())
    val selectedTask = _selectedTask.asStateFlow()
    private val _executionResult = MutableStateFlow(CodeExecutionResult())
    val executionResult = _executionResult.asStateFlow()
    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks = _tasks.asStateFlow()
    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory = _selectedCategory.asStateFlow()
    init {
        loadTasks(userId)
    }
    private fun saveTaskStatus(userId: Int, taskId: Int, isCorrect: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean("user_${userId}_task_${taskId}_isCorrect", isCorrect)
        editor.apply()
    }
    private val _codeMap = mutableStateMapOf<Int, String>()
    val codeMap: Map<Int, String> = _codeMap
    fun updateCode(idTest: Int, newCode: String) {
        _codeMap[idTest] = newCode
    }
    fun loadTaskStatus(userId: Int, taskId: Int): Boolean? {
        return if (sharedPreferences.contains("user_${userId}_task_${taskId}_isCorrect")) {
            sharedPreferences.getBoolean("user_${userId}_task_${taskId}_isCorrect", false)
        } else {
            null
        }
    }
    fun isTaskCompleted(userId: Int, taskId: Int): Boolean {
        return loadTaskStatus(userId, taskId) == true
    }
    fun loadTasks(userId: Int) {
        database.get().addOnSuccessListener { snapshot ->
            val list = snapshot.children.mapNotNull { it.getValue(Task::class.java) }
            list.forEach { task ->
                val taskStatus = loadTaskStatus(userId, task.IDTest)
            }
            _tasks.value = list
            _tasks.value.groupBy { it.category }.keys.forEach { category ->
                checkCategoryCompletion(category)
            }
        }
    }
    fun selectTask(task: Task) {
        _selectedTask.value = task
        _executionResult.value = CodeExecutionResult()
        _selectedCategory.value?.let {
            checkCategoryCompletion(it)
        }
    }
    fun selectCategory(category: String?) {
        _selectedCategory.value = category
        _selectedTask.value = Task()
        category?.let {
            checkCategoryCompletion(it)
        }
    }
    private fun checkCategoryCompletion(category: String) {
        val allTasksInCategory = _tasks.value.filter { it.category == category }
        val allCorrect = allTasksInCategory.all { it.isCorrect }
        _categoryCompletion.value = _categoryCompletion.value.toMutableMap().apply {
            put(category, allCorrect)
        }
    }
    fun checkPythonCode(code: String, input: String, userId: Int) {
        val client = OkHttpClient()

        if (!isValidPythonCode(code)) {
            _executionResult.value = CodeExecutionResult(error = "Неверная структура кода. Пожалуйста, напишите код для выполнения задачи.")
            return
        }
        val json = """
        {
            "clientId": "17c390a305bafda1bcfc5fa7b14d0876",
            "clientSecret": "827dd3169edd2d15fb3b04a84b5c83cbe2d3d9de027c543732b76844a337c876",
            "script": "${code.replace("\"", "\\\"").replace("\n", "\\n")}",
            "language": "python3",
            "versionIndex": "3",
            "stdin": "${input.replace("\"", "\\\"").replace("\n", "\\n")}"
        }
    """.trimIndent()
        val requestBody = json.toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("https://api.jdoodle.com/v1/execute")
            .post(requestBody)
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                _executionResult.value = CodeExecutionResult(error = e.localizedMessage ?: "Неизвестная ошибка")
            }
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    response.body?.string()?.let { body ->
                        val output = Regex("\"output\":\"(.*?)\"").find(body)?.groupValues?.get(1)?.replace("\\n", "\n") ?: ""
                        val error = Regex("\"error\":\"(.*?)\"").find(body)?.groupValues?.getOrNull(1)?.replace("\\n", "\n") ?: ""
                        if (output.isNotEmpty()) {
                            val isCorrect = output.trim() == selectedTask.value.expectedOutput.trim()
                            _executionResult.value = CodeExecutionResult(output = output + if (isCorrect) "\n\nРезультат верный!" else "\n\nРезультат неверный!")
                            selectedTask.value.isCorrect = isCorrect
                            saveTaskStatus(userId, selectedTask.value.IDTest, isCorrect)
                            updateStatisticsCode(userId, isCorrect)
                            checkCategoryCompletion(selectedTask.value.category)
                        } else if (error.isNotEmpty()) {
                            _executionResult.value = CodeExecutionResult(error = error)
                        }
                    }
                } else {
                    response.body?.string()?.let { body ->
                        _executionResult.value = CodeExecutionResult(error = "Ошибка ответа от сервера: ${response.code} - ${response.message}\nТело ответа: $body")
                    }
                }
            }
        })
    }
    fun isValidPythonCode(code: String): Boolean {
        val functionRegex = Regex("def\\s+\\w+\\s*\$")
        val loopRegex = Regex("for\\s+\\w+\\s+in\\s+\\w+")
        val conditionalRegex = Regex("if\\s+\\w+\\s*(==|!=|<|<=|>|>=)\\s+\\w+")
        val arithmeticRegex = Regex("[\\+\\-\\*/]")
        val variableRegex = Regex("[a-zA-Z_][a-zA-Z0-9_]*\\s*=")
        return functionRegex.containsMatchIn(code) ||
                loopRegex.containsMatchIn(code) ||
                conditionalRegex.containsMatchIn(code) ||
                arithmeticRegex.containsMatchIn(code) ||
                variableRegex.containsMatchIn(code)
    }
}
class TaskViewModelFactory(
    private val sharedPreferences: SharedPreferences,
    private val userId: Int
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TaskViewModel(sharedPreferences, userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
@Composable
fun TaskScreen(navController: NavController, userId: String, sharedPreferences: SharedPreferences) {
    val viewModel: TaskViewModel = viewModel(factory = TaskViewModelFactory(sharedPreferences,userId.toInt()))
    val tasks by viewModel.tasks.collectAsState()
    val selectedTask by viewModel.selectedTask.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val executionResult by viewModel.executionResult.collectAsState()
    val categoryCompletion by viewModel.categoryCompletion.collectAsState()
    var code by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.interscren),
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
                selectedCategory == null && selectedTask.title.isEmpty() -> {
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
                        items(tasks.map { it.category }.distinct()) { category ->
                            val (completedTasks, totalTasks) = getCompletionStatsForCategory(
                                sharedPreferences,
                                userId.toInt(),
                                category,
                                tasks
                            )
                            val completionPercentage =
                                if (totalTasks > 0) (completedTasks * 100) / totalTasks else 0
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
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "$category ${completedTasks}/${totalTasks} (${completionPercentage}%)",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp,
                                        color = Color(0xFF346837),
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (completedTasks == totalTasks && totalTasks > 0) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Completed",
                                            tint = Color.Green
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                selectedCategory != null && selectedTask.title.isEmpty() -> {
                    Text(
                        text = "Задачи по категории: $selectedCategory",
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
                        items(tasks.filter { it.category == selectedCategory }) { task ->
                            val taskStatus = viewModel.loadTaskStatus(userId.toInt(), task.IDTest)

                            val isTaskCompleted = taskStatus == true
                            val isTaskIncorrect = taskStatus == false
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                                    .clickable { viewModel.selectTask(task) },
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(4.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = task.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = Color(0xFF346837),
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (isTaskCompleted) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Completed",
                                            tint = Color.Green
                                        )
                                    } else if (isTaskIncorrect) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Incorrect",
                                            tint = Color.Red
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                selectedCategory != null && selectedTask.title.isNotEmpty() -> {
                    Text(
                        text = selectedTask.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = Color(0xFF346837)
                    )
                    Text(
                        text = selectedTask.description,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    val code = viewModel.codeMap[selectedTask.IDTest] ?: ""
                    BasicTextField(
                        value = code,
                        onValueChange = { newCode ->
                            viewModel.updateCode(selectedTask.IDTest, newCode)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .background(Color.White, shape = RoundedCornerShape(12.dp))
                            .shadow(4.dp, shape = RoundedCornerShape(12.dp))
                            .padding(16.dp),
                        textStyle = TextStyle.Default.copy(
                            color = Color.Black,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Normal
                        ),
                        cursorBrush = SolidColor(Color.Blue),
                        decorationBox = { innerTextField ->
                            if (code.isEmpty()) {
                                Text("Введите код...", color = Color.Gray)
                            }
                            innerTextField()
                        }
                    )
                    Button(
                        onClick = {
                            if (viewModel.isTaskCompleted(userId.toInt(), selectedTask.IDTest)) {
                                Log.d("TaskCheck", "Task already completed: ${selectedTask.IDTest}")
                                executionResult.error = "Задание уже выполнено."
                                return@Button
                            }
                            if (code.isNotBlank()) {
                                isLoading = true
                                Log.d("TaskCheck", "Checking code for task: ${selectedTask.IDTest}")
                                viewModel.checkPythonCode(code, selectedTask.testInput, userId.toInt())
                            } else {
                                executionResult.error = "Код не может быть пустым."
                                Log.d("TaskCheck", "Error: Code is blank")
                            }
                        },
                        modifier = Modifier.padding(top = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF346837)
                        ),
                    ) {
                        Text("Проверить код")
                    }
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.padding(top = 8.dp))
                    }
                    Text("Результат:", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
                    if (executionResult.output.isNotEmpty()) {
                        Text(text = executionResult.output)
                    } else if (executionResult.error.isNotEmpty()) {
                        Text(text = executionResult.error, color = Color.Red)
                    }
                    LaunchedEffect(executionResult) {
                        isLoading = false
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
                        selectedTask.title.isNotEmpty() -> viewModel.selectTask(Task())
                        selectedCategory != null -> viewModel.selectCategory(null)
                        else -> navController.navigate("StudyScreen/$userId")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF346837))
            ) {
                Text(
                    text = when {
                        selectedTask.title.isNotEmpty() -> "Назад"
                        selectedCategory != null -> "Назад"
                        else -> "Назад в обучение"
                    },
                    color = Color.White
                )
            }
        }
    }
}