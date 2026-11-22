package com.sempatapps

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import org.osmdroid.config.Configuration
import java.io.File

@HiltAndroidApp
class SempatApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // osmdroid: server peta OpenStreetMap mewajibkan User-Agent yang jelas,
        // dan cache peta disimpan di folder app sendiri (tidak perlu izin penyimpanan).
        Configuration.getInstance().apply {
            userAgentValue = packageName
            osmdroidBasePath = File(cacheDir, "osmdroid")
            osmdroidTileCache = File(cacheDir, "osmdroid/tiles")
        }
    }
}
