package com.sempatapps.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.sempatapps.domain.model.HomeRole
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

private val Context.sessionDataStore by preferencesDataStore(name = "sesi")

data class Sesi(val token: String, val peran: HomeRole, val nama: String)

/** Menyimpan sesi login di HP, supaya setelah daftar tidak perlu masuk lagi. */
@Singleton
class SessionStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    suspend fun simpan(sesi: Sesi) {
        context.sessionDataStore.edit {
            it[TOKEN] = sesi.token
            it[PERAN] = sesi.peran.name
            it[NAMA] = sesi.nama
        }
    }

    suspend fun ambil(): Sesi? {
        val prefs = context.sessionDataStore.data.first()
        val token = prefs[TOKEN] ?: return null
        val peran = prefs[PERAN]?.let { runCatching { HomeRole.valueOf(it) }.getOrNull() } ?: return null
        return Sesi(token, peran, prefs[NAMA].orEmpty())
    }

    suspend fun hapus() {
        context.sessionDataStore.edit { it.clear() }
    }

    private companion object {
        val TOKEN = stringPreferencesKey("token")
        val PERAN = stringPreferencesKey("peran")
        val NAMA = stringPreferencesKey("nama")
    }
}
