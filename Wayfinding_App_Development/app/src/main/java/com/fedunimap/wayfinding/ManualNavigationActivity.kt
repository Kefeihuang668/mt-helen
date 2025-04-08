package com.fedunimap.wayfinding

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class ManualNavigationActivity : ComponentActivity() {
    private lateinit var mapView: MapView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 创建 MapView 实例
        mapView = MapView(this)
        mapView.onCreate(savedInstanceState)

        setContent {
            Column(modifier = Modifier.fillMaxSize()) {
                AndroidView(factory = {
                    mapView.getMapAsync { googleMap ->
                        Log.d("MAP_DEBUG", "Google Map is ready ✅")

                        val fedUni = LatLng(-37.631, 143.426)
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(fedUni, 17f))
                        googleMap.addMarker(MarkerOptions().position(fedUni).title("FedUni Mount Helen"))
                    }
                    mapView
                }, modifier = Modifier.fillMaxSize())
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        mapView.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        mapView.onDestroy()
        super.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }
}
