package com.sempatapps.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.sempatapps.data.repository.DummyHomeRepository
import com.sempatapps.domain.model.HomeRole
import com.sempatapps.presentation.auth.login.LoginScreen
import com.sempatapps.presentation.auth.register.BuatPasswordScreen
import com.sempatapps.presentation.auth.register.FotoKtpScreen
import com.sempatapps.presentation.auth.register.HasilKtpScreen
import com.sempatapps.presentation.auth.register.KodeVerifikasiScreen
import com.sempatapps.presentation.auth.register.LokasiScreen
import com.sempatapps.presentation.auth.register.PilihAlamatScreen
import com.sempatapps.presentation.auth.register.RegisterScreen
import com.sempatapps.presentation.auth.register.RegisterViewModel
import com.sempatapps.presentation.home.HomeApp
import com.sempatapps.presentation.onboarding.OnboardingScreen
import com.sempatapps.presentation.splash.SplashScreen
import com.sempatapps.presentation.splash.SplashViewModel
import kotlinx.coroutines.launch

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = SplashRoute,
    ) {
        composable<SplashRoute> {
            val viewModel: SplashViewModel = hiltViewModel()
            val scope = rememberCoroutineScope()
            SplashScreen(
                onFinished = {
                    scope.launch {
                        // Sudah pernah daftar/masuk di HP ini → langsung ke beranda.
                        val peran = viewModel.peranTersimpan()
                        if (peran != null) {
                            navController.goToHome(peran)
                        } else {
                            navController.navigate(OnboardingRoute) {
                                popUpTo(SplashRoute) { inclusive = true }
                            }
                        }
                    }
                },
            )
        }
        composable<OnboardingRoute> {
            OnboardingScreen(
                onLoginClick = { navController.navigate(LoginRoute) },
                onRegisterClick = { navController.navigate(RegisterGraph) },
            )
        }

        composable<LoginRoute> {
            LoginScreen(
                onBackClick = { navController.popBackStack() },
                onDaftarClick = {
                    navController.navigate(RegisterGraph) {
                        popUpTo(LoginRoute) { inclusive = true }
                    }
                },
                onMulai = { peran -> navController.goToHome(peran) },
            )
        }

        navigation<RegisterGraph>(startDestination = RegisterRoute) {
            composable<RegisterRoute> { entry ->
                val viewModel = entry.registerViewModel(navController)
                RegisterScreen(
                    onBackClick = { navController.popBackStack() },
                    onContinue = { role ->
                        viewModel.pilihPeran(role)
                        navController.navigate(FotoKtpRoute)
                    },
                )
            }
            composable<FotoKtpRoute> { entry ->
                FotoKtpScreen(
                    viewModel = entry.registerViewModel(navController),
                    onBackClick = { navController.popBackStack() },
                    onKtpTerbaca = { navController.navigate(HasilKtpRoute) },
                    onIsiManual = { navController.navigate(HasilKtpRoute) },
                )
            }
            composable<HasilKtpRoute> { entry ->
                HasilKtpScreen(
                    viewModel = entry.registerViewModel(navController),
                    onBackClick = { navController.popBackStack() },
                    onLanjut = { navController.navigate(LokasiRoute) },
                )
            }
            composable<LokasiRoute> { entry ->
                LokasiScreen(
                    viewModel = entry.registerViewModel(navController),
                    onBackClick = { navController.popBackStack() },
                    onPilihDiPeta = { navController.navigate(PilihAlamatRoute) },
                    onKodeTerkirim = { navController.navigate(KodeVerifikasiRoute) },
                )
            }
            composable<PilihAlamatRoute> { entry ->
                PilihAlamatScreen(
                    viewModel = entry.registerViewModel(navController),
                    onBackClick = { navController.popBackStack() },
                    onKonfirmasi = { navController.popBackStack() },
                )
            }
            composable<KodeVerifikasiRoute> { entry ->
                KodeVerifikasiScreen(
                    viewModel = entry.registerViewModel(navController),
                    onBackClick = { navController.popBackStack() },
                    onKodeBenar = { navController.navigate(BuatPasswordRoute) },
                )
            }
            composable<BuatPasswordRoute> { entry ->
                val viewModel = entry.registerViewModel(navController)
                BuatPasswordScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() },
                    onMulai = { navController.goToHome(viewModel.state.value.data.peran) },
                )
            }
        }

        composable<HomeRoute> { entry ->
            val route = entry.toRoute<HomeRoute>()
            if (route.role == HomeRole.RECIPIENT) {
                PenerimaApp(
                    onKeluar = {
                        navController.navigate(OnboardingRoute) {
                            popUpTo(navController.graph.id) { inclusive = true }
                        }
                    },
                )
            } else {
                val repository = remember { DummyHomeRepository() }
                HomeApp(role = route.role, repository = repository)
            }
        }
    }
}

/**
 * Semua layar daftar memakai RegisterViewModel yang sama (milik RegisterGraph),
 * jadi isian tetap ada waktu pindah langkah dan otomatis dibuang setelah daftar selesai/batal.
 */
@Composable
private fun NavBackStackEntry.registerViewModel(navController: NavController): RegisterViewModel {
    val graphEntry = remember(this) { navController.getBackStackEntry<RegisterGraph>() }
    return hiltViewModel(graphEntry)
}

/** Masuk Home dan kosongkan back stack, jadi tombol back di Home langsung keluar aplikasi. */
private fun NavController.goToHome(role: HomeRole) {
    navigate(HomeRoute(role)) {
        popUpTo(graph.id) { inclusive = true }
    }
}
