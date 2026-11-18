package com.sempatapps.server.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

fun connectDatabase(url: String, user: String, password: String): Database {
    val dataSource = HikariDataSource(
        HikariConfig().apply {
            jdbcUrl = url
            username = user
            this.password = password
            maximumPoolSize = 5
        },
    )
    val db = Database.connect(dataSource)
    // Untuk sekarang tabel dibuat otomatis waktu server nyala.
    // Kalau nanti struktur tabel sering berubah, ganti pakai migrasi (mis. Flyway).
    transaction(db) {
        SchemaUtils.create(Users, Lokasi, KodeOtp)
    }
    return db
}
