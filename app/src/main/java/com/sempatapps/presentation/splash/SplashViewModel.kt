package com.sempatapps.presentation.splash

import androidx.lifecycle.ViewModel
import com.sempatapps.data.local.SessionStore
import com.sempatapps.domain.model.HomeRole
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val sessionStore: SessionStore,
) : ViewModel() {
    /** Peran pengguna yang sudah login di HP ini, atau null kalau belum pernah daftar/masuk. */
    suspend fun peranTersimpan(): HomeRole? = sessionStore.ambil()?.peran
}
