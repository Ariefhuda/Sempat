package com.sempatapps.presentation.penerima

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sempatapps.data.local.SessionStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Data akun yang dipakai bersama semua layar penerima. Isi layar lainnya masih data contoh. */
@HiltViewModel
class PenerimaViewModel @Inject constructor(
    private val sessionStore: SessionStore,
) : ViewModel() {

    /** Nama dari sesi login; kosong kalau belum termuat. */
    private val _nama = MutableStateFlow("")
    val nama = _nama.asStateFlow()

    init {
        viewModelScope.launch { _nama.value = sessionStore.ambil()?.nama.orEmpty() }
    }

    /** Hapus sesi di HP ini, lalu [selesai] dipanggil (pindah ke halaman awal). */
    fun keluar(selesai: () -> Unit) {
        viewModelScope.launch {
            sessionStore.hapus()
            selesai()
        }
    }
}
