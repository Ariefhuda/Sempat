package com.sempatapps.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.sempatapps.data.repository.DummyHomeRepository
import com.sempatapps.domain.model.HomeRole
import com.sempatapps.presentation.auth.register.RegisterScreen
import com.sempatapps.presentation.home.HomeApp
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
            RegisterPlaceholder("Layar Donatur — belum dibuat") {
                navController.goToHome(HomeRole.DONOR)
            }
        }
        composable<RegisterPenerimaRoute> {
            RegisterPlaceholder("Layar Penerima — belum dibuat") {
                navController.goToHome(HomeRole.RECIPIENT)
            }
        }
        composable<HomeRoute> { entry ->
            val route = entry.toRoute<HomeRoute>()
            val repository = remember { DummyHomeRepository() }
            HomeApp(role = route.role, repository = repository)
        }
    }
}

/** Masuk Home dan buang onboarding/register dari back stack, jadi tombol back nggak balik ke register. */
private fun NavController.goToHome(role: HomeRole) {
    navigate(HomeRoute(role)) {
        popUpTo(OnboardingRoute) { inclusive = true }
    }
}

@Composable
private fun RegisterPlaceholder(title: String, onDone: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(title)
        Button(onClick = onDone) { Text("Lanjut ke Home (sementara)") }
    }
}
