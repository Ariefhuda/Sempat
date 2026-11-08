package com.sempatapps

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.remember
import com.sempatapps.data.repository.DummyHomeRepository
import com.sempatapps.domain.model.HomeRole
import com.sempatapps.presentation.home.HomeApp
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val role = if (intent.getStringExtra("home_role") == "recipient") HomeRole.RECIPIENT else HomeRole.DONOR
        setContent {
            val repository = remember { DummyHomeRepository() }
            HomeApp(role = role, repository = repository)
        }
    }
}
