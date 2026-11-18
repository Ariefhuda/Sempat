package com.sempatapps.navigation

import com.sempatapps.domain.model.HomeRole
import kotlinx.serialization.Serializable

@Serializable
object OnboardingRoute

@Serializable
object RegisterRoute

@Serializable
object RegisterDonaturRoute

@Serializable
object RegisterPenerimaRoute

@Serializable
data class HomeRoute(val role: HomeRole)
