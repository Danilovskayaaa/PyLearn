package com.example.pylearn

import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

@Composable
fun AppNavGraph(navController: NavHostController, sharedPreferences: SharedPreferences) {
    NavHost(navController = navController, startDestination = "AuthScreen") {
        composable("AuthScreen") {
            AuthScreen(navController)
        }
        composable("RegisterScreen") {
            RegisterScreen(navController)
        }
        composable("ProfileScreen/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")
            if (userId != null) {
                ProfileScreen(navController, userId)
            }
        }
        composable("TheoryScreen/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")
            if (userId != null) {
                TheoryScreen(navController, userId)
            }
        }
        composable("TestScreen/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")
            if (userId != null) {
                TestScreen(navController, userId)
            }
        }
        composable("TaskScreen/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")
            if (userId != null) {
                TaskScreen(navController, userId, sharedPreferences) // Передаём sharedPreferences только здесь
            }
        }
        composable("StudyScreen/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")
            if (userId != null) {
                StudyScreen(navController, userId)
            }
        }
    }
}