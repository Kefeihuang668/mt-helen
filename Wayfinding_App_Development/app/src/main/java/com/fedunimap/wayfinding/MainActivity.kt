package com.fedunimap.wayfinding

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MainActivity : ComponentActivity() {
    private lateinit var mapView: MapView
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1001
            )
        }

        mapView = MapView(this)
        mapView.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setContent {
            MainUIScreen(mapView, fusedLocationClient)
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }
}

@Composable
fun MainUIScreen(mapView: MapView, fusedLocationClient: FusedLocationProviderClient) {
    val context = LocalContext.current

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF1F4FB)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp)
        ) {
            Text(
                text = "Smart Navigation",
                style = MaterialTheme.typography.headlineSmall,
                color = Color(0xFF002B5B)
            )

            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = "",
                onValueChange = {},
                placeholder = { Text("Search locations", color = Color.Gray) },
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Color(0xFFE9ECF5),
                    focusedContainerColor = Color(0xFFE9ECF5),
                    focusedIndicatorColor = Color(0xFF002B5B),
                    unfocusedIndicatorColor = Color(0xFFCCCCCC),
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            AndroidView(
                factory = {
                    mapView.getMapAsync { googleMap ->
                        val fedUni = LatLng(-37.62623191992383, 143.8917856963707)
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(fedUni, 15f))
                        googleMap.addMarker(
                            MarkerOptions().position(fedUni).title("Federation University")
                        )

                        if (ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            googleMap.isMyLocationEnabled = true
                        }

                        googleMap.uiSettings.isZoomGesturesEnabled = true
                    }
                    mapView
                },
                modifier = Modifier.weight(1f).fillMaxWidth()
            )
        }

        FloatingButtons(mapView, fusedLocationClient)
    }
}

@SuppressLint("MissingPermission")
@Composable
fun FloatingButtons(mapView: MapView, fusedLocationClient: FusedLocationProviderClient) {
    val context = LocalContext.current
    val fedUniLatLng = LatLng(-37.62623191992383, 143.8917856963707)

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.End
    ) {
        FloatingActionButton(
            onClick = {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        val currentLatLng = LatLng(location.latitude, location.longitude)
                        mapView.getMapAsync { googleMap ->
                            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 17f))
                        }
                    }
                }
            },
            containerColor = Color(0xFF002B5B),
            contentColor = Color.White
        ) {
            Text("📍")
        }

        Spacer(modifier = Modifier.height(12.dp))

        FloatingActionButton(
            onClick = {
                mapView.getMapAsync { googleMap ->
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(fedUniLatLng, 15f))
                }
            },
            containerColor = Color(0xFF002B5B),
            contentColor = Color.White
        ) {
            Text("🎓")
        }

        Spacer(modifier = Modifier.height(12.dp))

        FloatingActionButton(
            onClick = {
                val intent = Intent(context, ManualNavigationActivity::class.java)
                context.startActivity(intent)
            },
            containerColor = Color(0xFF002B5B),
            contentColor = Color.White
        ) {
            Text("➡")
        }
    }
}
