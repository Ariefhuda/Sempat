package com.sempatapps.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sempatapps.data.repository.HomeRepository
import com.sempatapps.domain.model.HomeRole
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    val repository: HomeRepository,
) : ViewModel() {
    fun refresh(role: HomeRole) {
        viewModelScope.launch {
            repository.refresh(role)
        }
    }
}
