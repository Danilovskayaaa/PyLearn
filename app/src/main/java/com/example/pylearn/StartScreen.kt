package com.example.pylearn

import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController

class StartScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Получаем экземпляр SharedPreferences
        val sharedPreferences: SharedPreferences = getSharedPreferences("your_prefs_name", MODE_PRIVATE)

        setContent {
            val navController = rememberNavController()
            AppNavGraph(navController, sharedPreferences) // Передаем sharedPreferences сюда
        }
    }
}