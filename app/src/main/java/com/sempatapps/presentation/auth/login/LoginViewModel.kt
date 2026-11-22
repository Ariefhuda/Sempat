package com.sempatapps.presentation.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sempatapps.data.remote.ApiException
import com.sempatapps.data.repository.AuthRepository
import com.sempatapps.domain.model.HomeRole
import com.sempatapps.domain.model.noHpValid
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginState(
    val noHp: String = "",
    val password: String = "",
    /** true setelah tombol Masuk ditekan sekali: baru setelah itu kolom yang salah ditandai merah. */
    val cekIsian: Boolean = false,
    val sedangProses: Boolean = false,
    val pesanError: String? = null,
    /** Terisi setelah berhasil masuk: tampilkan pop-up, lalu ke beranda sesuai peran ini. */
    val berhasilSebagai: HomeRole? = null,
) {
    val noHpSalah get() = cekIsian && !noHpValid(noHp)
    val passwordKosong get() = cekIsian && password.isEmpty()
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state = _state.asStateFlow()

    fun ubahNoHp(noHp: String) = _state.update { it.copy(noHp = noHp.filter { c -> c.isDigit() }.take(15), pesanError = null) }

    fun ubahPassword(password: String) = _state.update { it.copy(password = password, pesanError = null) }

    fun masuk() {
        val s = _state.value
        if (s.sedangProses) return
        _state.update { it.copy(cekIsian = true) }
        if (!noHpValid(s.noHp) || s.password.isEmpty()) return

        _state.update { it.copy(sedangProses = true, pesanError = null) }
        viewModelScope.launch {
            try {
                val peran = authRepository.masuk(s.noHp, s.password)
                _state.update { it.copy(berhasilSebagai = peran) }
            } catch (e: ApiException) {
                _state.update { it.copy(pesanError = e.message) }
            } finally {
                _state.update { it.copy(sedangProses = false) }
            }
        }
    }
}
