package com.example.pylearn
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.navigation.NavController
import androidx.work.*
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
@Composable
fun SettingsScreen(navController: NavController, userId: String) {
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("user_settings", Context.MODE_PRIVATE)
    var isNotificationEnabled by remember { mutableStateOf(loadNotificationSetting(sharedPreferences, userId)) }
    var inputText by remember { mutableStateOf(TextFieldValue("")) }
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.setsc),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 100.dp, start = 16.dp, end = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Включить уведомления",
                    modifier = Modifier.padding(end = 8.dp),
                    style = TextStyle(color = Color.White, fontSize = 18.sp)
                )
                Switch(
                    checked = isNotificationEnabled,
                    onCheckedChange = {
                        isNotificationEnabled = it
                        saveNotificationSetting(sharedPreferences, userId, it)
                        if (it) {
                            startNotificationWorker(context)
                        } else {
                            stopNotificationWorker(context)
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF346837),
                        uncheckedThumbColor = Color(0xFF002D00),
                        checkedTrackColor = Color(0xFF346837).copy(alpha = 0.4f),
                        uncheckedTrackColor = Color(0xFF78A979)
                    ),
                    modifier = Modifier.padding(8.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Техническая поддержка",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier

                    .fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(10.dp))
            TextField(
                value = inputText,
                onValueChange = { inputText = it },
                label = { Text("Введите сообщение", color = Color.Black) },
                textStyle = TextStyle(color = Color.Black, fontSize = 16.sp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .heightIn(min = 240.dp),
                maxLines = 40,
                minLines = 20
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    val message = inputText.text
                    if (message.isEmpty()) {
                        Toast.makeText(context, "Пожалуйста, введите сообщение.", Toast.LENGTH_SHORT).show()
                    } else {
                        sendEmail(message, context)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF346837)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .height(56.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.helperpic),
                    contentDescription = "Поддержка",
                    modifier = Modifier.size(20.dp),
                    tint = Color.White
                )
                Text(text = "Отправить сообщение", color = Color.White, fontSize = 18.sp)
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
                    navController.navigate("ProfileScreen/$userId")
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF346837))
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.undopic),
                    contentDescription = "Назад",
                    modifier = Modifier.size(20.dp),
                    tint = Color.White
                )
                Text(
                    text = "Перейти в профиль",
                    color = Color.White
                )
            }
        }
    }
}
fun loadNotificationSetting(sharedPreferences: SharedPreferences, userId: String): Boolean {
    return sharedPreferences.getBoolean("notifications_$userId", false)
}
fun saveNotificationSetting(sharedPreferences: SharedPreferences, userId: String, isEnabled: Boolean) {
    with(sharedPreferences.edit()) {
        putBoolean("notifications_$userId", isEnabled)
        apply()
    }
}
fun startNotificationWorker(context: Context) {
    val workRequest = PeriodicWorkRequestBuilder<NotificationWorker>(1, TimeUnit.HOURS)
        .setInitialDelay(0, TimeUnit.HOURS)
        .build()
    WorkManager.getInstance(context).enqueue(workRequest)
}
fun stopNotificationWorker(context: Context) {
    WorkManager.getInstance(context).cancelAllWork()
}
fun sendEmail(message: String, context: Context) {
    val recipient = "pylearnsupport@gmail.com"
    val subject = "Техническая поддержка"
    val emailIntent = Intent(Intent.ACTION_SEND).apply {
        type = "message/rfc822"
        putExtra(Intent.EXTRA_EMAIL, arrayOf(recipient))
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, message)
    }
    try {
        context.startActivity(Intent.createChooser(emailIntent, "Выберите почтовое приложение"))
    } catch (e: android.content.ActivityNotFoundException) {
        Toast.makeText(context, "Установите почтовое приложение.", Toast.LENGTH_SHORT).show()
    }
}
class NotificationWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    override fun doWork(): Result {
        showNotification()
        return Result.success()
    }
    private fun showNotification() {
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "notification_channel_id",
                "Notification Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }
        val notification = NotificationCompat.Builder(applicationContext, "notification_channel_id")
            .setContentTitle("Пора позаниматься")
            .setContentText("Не забывайте, что время для занятий пришло!")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .build()
        notificationManager.notify(1, notification)
    }
}