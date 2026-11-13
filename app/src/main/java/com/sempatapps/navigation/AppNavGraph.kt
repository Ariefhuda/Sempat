package com.sempatapps.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sempatapps.domain.model.HomeRole
import com.sempatapps.presentation.auth.register.RegisterScreen
import com.sempatapps.presentation.onboarding.OnboardingScreen

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = OnboardingRoute,
    ) {
        composable<OnboardingRoute> {
            OnboardingScreen(
                onLoginClick = {},
                onRegisterClick = { navController.navigate(RegisterRoute) },
            )
        }
        composable<RegisterRoute> {
            RegisterScreen(
                onBackClick = { navController.popBackStack() },
                onContinue = { role ->
                    when (role) {
                        HomeRole.DONOR -> navController.navigate(RegisterDonaturRoute)
                        HomeRole.RECIPIENT -> navController.navigate(RegisterPenerimaRoute)
                    }
                },
            )
        }
        composable<RegisterDonaturRoute> {
            Text("Layar Donatur — belum dibuat")
        }
        composable<RegisterPenerimaRoute> {
            Text("Layar Penerima — belum dibuat")
        }
    }
}
