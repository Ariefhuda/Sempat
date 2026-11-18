package com.sempatapps.core.designsystem.component

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.sempatapps.R
import com.sempatapps.core.designsystem.BodyBold
import com.sempatapps.core.designsystem.PaleTeal
import com.sempatapps.core.designsystem.SecondaryDark
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView

/** Titik awal peta kalau belum ada lokasi apa pun: Medan (sama dengan contoh di Figma). */
val LokasiDefault = GeoPoint(3.5952, 98.6722)

/**
 * Peta OpenStreetMap (osmdroid) untuk Compose.
 *
 * @param pusat titik tengah peta. Kalau nilainya berubah (mis. hasil pencarian), peta ikut pindah.
 * @param bisaDigeser false untuk peta kecil yang cuma pratinjau.
 * @param onPusatBerubah dipanggil tiap peta selesai digeser/di-zoom, berisi titik tengah terbaru.
 */
@SuppressLint("ClickableViewAccessibility")
@Composable
fun PetaOsm(
    pusat: GeoPoint,
    modifier: Modifier = Modifier,
    zoom: Double = 17.0,
    bisaDigeser: Boolean = true,
    onPusatBerubah: (GeoPoint) -> Unit = {},
) {
    val context = LocalContext.current
    val onPusatBerubahTerbaru by rememberUpdatedState(onPusatBerubah)
    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(bisaDigeser)
            zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
            controller.setZoom(zoom)
            controller.setCenter(pusat)
            if (!bisaDigeser) setOnTouchListener { _, _ -> true }
            addMapListener(object : MapListener {
                override fun onScroll(event: ScrollEvent?): Boolean {
                    onPusatBerubahTerbaru(mapCenter as GeoPoint)
                    return false
                }

                override fun onZoom(event: ZoomEvent?): Boolean = false
            })
        }
    }
    val pusatTerakhir = remember { arrayOf(pusat) }

    // MapView perlu tahu kapan layar aktif/tidak supaya tidak boros baterai.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onDetach()
        }
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier,
        update = {
            if (pusat != pusatTerakhir[0]) {
                pusatTerakhir[0] = pusat
                if (bisaDigeser) it.controller.animateTo(pusat) else it.controller.setCenter(pusat)
            }
        },
    )
}

/** Penanda "Pilih Lokasi" di tengah peta. Ujung bawah pin = titik yang dipilih. */
@Composable
fun PinPilihLokasi(modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
        modifier = modifier.width(83.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .width(83.dp)
                .height(20.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(SecondaryDark),
        ) {
            Text("Pilih Lokasi", style = BodyBold.copy(fontSize = 14.sp, lineHeight = 16.sp), color = PaleTeal)
        }
        Image(painterResource(R.drawable.ic_location_pin), contentDescription = null, modifier = Modifier.size(44.dp))
    }
}

/**
 * Geser [PinPilihLokasi] supaya ujung pin pas di tengah. Tinggi pin 66dp (label 20 + jarak 2 + ikon 44),
 * ujung pin ada di 62dp dari atas, jadi naik 62 - 33 = 29dp dari posisi tengah.
 */
val PinOffsetY = (-29).dp
