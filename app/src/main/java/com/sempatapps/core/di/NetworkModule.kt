package com.sempatapps.core.di

import com.sempatapps.BuildConfig
import com.sempatapps.data.repository.ApiAuthRepository
import com.sempatapps.data.repository.ApiHomeRepository
import com.sempatapps.data.repository.AuthRepository
import com.sempatapps.data.repository.HomeRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun httpClient(): HttpClient = HttpClient(OkHttp) {
        defaultRequest { url(BuildConfig.API_URL) }
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 15_000
            connectTimeoutMillis = 10_000
        }
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun authRepository(impl: ApiAuthRepository): AuthRepository

    @Binds
    abstract fun homeRepository(impl: ApiHomeRepository): HomeRepository
}
