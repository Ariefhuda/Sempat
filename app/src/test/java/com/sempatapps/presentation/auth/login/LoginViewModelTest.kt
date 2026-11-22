package com.sempatapps.presentation.auth.login

import com.sempatapps.data.remote.ApiException
import com.sempatapps.data.repository.AuthRepository
import com.sempatapps.data.repository.OtpTerkirim
import com.sempatapps.domain.model.DataDaftar
import com.sempatapps.domain.model.HomeRole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private class FakeAuth(private val hasil: () -> HomeRole) : AuthRepository {
        val panggilan = mutableListOf<Pair<String, String>>()
        override suspend fun masuk(noHp: String, password: String): HomeRole {
            panggilan += noHp to password
            return hasil()
        }
        override suspend fun kirimOtp(noHp: String) = OtpTerkirim(60, null)
        override suspend fun cekOtp(noHp: String, kode: String) = ""
        override suspend fun daftar(tokenVerifikasi: String, data: DataDaftar, password: String) = Unit
    }

    @Before
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `isian salah tidak dikirim ke server dan kolomnya ditandai`() {
        val auth = FakeAuth { HomeRole.DONOR }
        val vm = LoginViewModel(auth)
        vm.ubahNoHp("123")

        vm.masuk()

        assertTrue(auth.panggilan.isEmpty())
        assertTrue(vm.state.value.noHpSalah)
        assertTrue(vm.state.value.passwordKosong)
    }

    @Test
    fun `berhasil masuk menyimpan peran untuk pop-up`() {
        val auth = FakeAuth { HomeRole.RECIPIENT }
        val vm = LoginViewModel(auth)
        vm.ubahNoHp("812-3456-7890")
        vm.ubahPassword("rahasia123")

        vm.masuk()

        assertEquals(listOf("81234567890" to "rahasia123"), auth.panggilan)
        assertEquals(HomeRole.RECIPIENT, vm.state.value.berhasilSebagai)
        assertNull(vm.state.value.pesanError)
    }

    @Test
    fun `pesan error dari server ditampilkan`() {
        val vm = LoginViewModel(FakeAuth { throw ApiException("Nomor HP atau password salah") })
        vm.ubahNoHp("81234567890")
        vm.ubahPassword("salah")

        vm.masuk()

        assertEquals("Nomor HP atau password salah", vm.state.value.pesanError)
        assertNull(vm.state.value.berhasilSebagai)
        assertEquals(false, vm.state.value.sedangProses)
    }
}
