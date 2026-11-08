
package com.sempatapps.presentation.home

import androidx.compose.runtime.Composable
import com.sempatapps.data.repository.DummyHomeRepository
import com.sempatapps.domain.model.HomeRole

@Composable
fun HomeApp(
    role: HomeRole,
    repository: DummyHomeRepository
) {
    HomePenerima()
}
