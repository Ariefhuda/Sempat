package com.sempatapps.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.sempatapps.presentation.auth.register.RegisterScreen
import com.sempatapps.presentation.onboarding.OnboardingScreen
import androidx.navigation.compose.composable

@Composable

fun AppNavGraph(){
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = OnboardingRoute
    ){
        composable<OnboardingRoute> {
            OnboardingScreen(
                onLoginClick = {},
                onRegisterClick = { navController.navigate(RegisterRoute) },
            )
        }
        composable<RegisterRoute>{
            RegisterScreen(
                onBackClick = {navController.popBackStack()},
            )
        }
    }
}