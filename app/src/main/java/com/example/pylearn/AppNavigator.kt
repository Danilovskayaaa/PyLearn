package com.example.pylearn

import androidx.compose.runtime.Composable
import androidx.navigation.*
import androidx.navigation.compose.*
@Composable
fun AppNavGraph(navController: NavHostController) {
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
        composable("StudyScreen/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")
            if (userId != null) {
                StudyScreen(navController, userId)
            }
        }
    }
}
