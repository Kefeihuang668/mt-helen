package com.fedunimap.wayfinding

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.*
import kotlin.math.*
import android.util.Log

// Function to calculate bearing between two points
private fun calculateBearing(start: LatLng, end: LatLng): Double {
    val lat1 = start.latitude * Math.PI / 180
    val lat2 = end.latitude * Math.PI / 180
    val lon1 = start.longitude * Math.PI / 180
    val lon2 = end.longitude * Math.PI / 180
    
    val y = sin(lon2 - lon1) * cos(lat2)
    val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(lon2 - lon1)
    var bearing = atan2(y, x)
    bearing = bearing * 180 / Math.PI
    if (bearing < 0) bearing += 360
    return bearing
}

// Function to get turn direction
private fun getTurnDirection(currentBearing: Double, nextBearing: Double): String {
    val angle = ((nextBearing - currentBearing + 360) % 360)
    return when {
        angle < 45 -> "继续直行"
        angle < 135 -> "右转"
        angle > 315 -> "继续直行"
        angle > 225 -> "左转"
        else -> "掉头"
    }
}

// Function to calculate distance between two points
private fun calculateDistance(point1: LatLng, point2: LatLng): Double {
    val R = 6371e3 // Earth's radius in meters
    val φ1 = point1.latitude * Math.PI / 180
    val φ2 = point2.latitude * Math.PI / 180
    val Δφ = (point2.latitude - point1.latitude) * Math.PI / 180
    val Δλ = (point2.longitude - point1.longitude) * Math.PI / 180
    
    val a = sin(Δφ / 2) * sin(Δφ / 2) +
            cos(φ1) * cos(φ2) *
            sin(Δλ / 2) * sin(Δλ / 2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    
    return R * c
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    var showBuildingList by remember { mutableStateOf(false) }
    var selectedStartBuilding by remember { mutableStateOf<Building?>(null) }
    var selectedEndBuilding by remember { mutableStateOf<Building?>(null) }
    var showStartBuildingSelection by remember { mutableStateOf(false) }
    var showEndBuildingSelection by remember { mutableStateOf(false) }
    var showSelectionMessage by remember { mutableStateOf(false) }
    var selectionMessage by remember { mutableStateOf("") }
    var routePoints by remember { mutableStateOf<List<LatLng>>(emptyList()) }
    var navigationInfo by remember { mutableStateOf<String?>(null) }
    var isNavigating by remember { mutableStateOf(false) }
    var currentStepIndex by remember { mutableStateOf(0) }
    var navigationJob by remember { mutableStateOf<Job?>(null) }
    var nextTurnInfo by remember { mutableStateOf("") }

    // Federation University Mount Helen Campus location
    val fedUni = LatLng(-37.6297, 143.8890)
    
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(fedUni, 17f)
    }

    // Check location permission
    val hasLocationPermission = remember {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Function to calculate route
    fun calculateRoute() {
        if (selectedStartBuilding != null && selectedEndBuilding != null) {
            Log.d("MapScreen", "Calculating route from ${selectedStartBuilding?.name} to ${selectedEndBuilding?.name}")
            scope.launch {
                try {
                    Log.d("MapScreen", "Using API Key: ${BuildConfig.MAPS_API_KEY.take(5)}...")
                    val points = RouteCalculator.getWalkingRoute(
                        selectedStartBuilding!!.location,
                        selectedEndBuilding!!.location,
                        BuildConfig.MAPS_API_KEY
                    )
                    Log.d("MapScreen", "Received ${points.size} route points")
                    routePoints = points
                    
                    // Move camera to show the entire route
                    if (points.size >= 2) {
                        val boundsBuilder = com.google.android.gms.maps.model.LatLngBounds.builder()
                        points.forEach { boundsBuilder.include(it) }
                        val bounds = boundsBuilder.build()
                        val cameraUpdate = com.google.android.gms.maps.CameraUpdateFactory.newLatLngBounds(
                            bounds,
                            100 // padding in pixels
                        )
                        cameraPositionState.move(cameraUpdate)
                    }
                    
                    // Calculate approximate walking time (assuming 5km/h walking speed)
                    var distance = 0.0
                    for (i in 0 until points.size - 1) {
                        val lat1 = points[i].latitude
                        val lon1 = points[i].longitude
                        val lat2 = points[i + 1].latitude
                        val lon2 = points[i + 1].longitude
                        
                        // Haversine formula
                        val R = 6371e3 // Earth's radius in meters
                        val φ1 = lat1 * Math.PI / 180
                        val φ2 = lat2 * Math.PI / 180
                        val Δφ = (lat2 - lat1) * Math.PI / 180
                        val Δλ = (lon2 - lon1) * Math.PI / 180
                        
                        val a = Math.sin(Δφ / 2) * Math.sin(Δφ / 2) +
                                Math.cos(φ1) * Math.cos(φ2) *
                                Math.sin(Δλ / 2) * Math.sin(Δλ / 2)
                        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
                        
                        distance += R * c
                    }
                    
                    val walkingTimeMinutes = (distance / 1000.0 / 5.0 * 60.0).toInt()
                    navigationInfo = "总距离: ${String.format("%.1f", distance / 1000.0)}公里\n" +
                            "预计步行时间: $walkingTimeMinutes 分钟"
                    Log.d("MapScreen", "Route calculated: $navigationInfo")
                } catch (e: Exception) {
                    Log.e("MapScreen", "Error calculating route", e)
                    navigationInfo = "无法计算路线"
                    routePoints = listOf(selectedStartBuilding!!.location, selectedEndBuilding!!.location)
                }
            }
        } else {
            Log.d("MapScreen", "Cannot calculate route: start or end building not selected")
            routePoints = emptyList()
            navigationInfo = null
        }
    }

    // Function to start navigation
    fun startNavigation() {
        if (routePoints.isEmpty()) {
            Log.d("MapScreen", "Cannot start navigation: no route points available")
            return
        }
        
        Log.d("MapScreen", "Starting navigation with ${routePoints.size} points")
        isNavigating = true
        currentStepIndex = 0
        
        // 立即移动相机到起点位置
        cameraPositionState.position = CameraPosition.fromLatLngZoom(routePoints[0], 18f)
        
        navigationJob?.cancel()
        navigationJob = scope.launch {
            while (isActive && currentStepIndex < routePoints.size - 1) {
                withContext(Dispatchers.Main) {
                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                        location?.let {
                            Log.d("MapScreen", "Current location: ${it.latitude}, ${it.longitude}")
                            userLocation = LatLng(it.latitude, it.longitude)
                            
                            // 计算到下一个点的距离
                            val nextPoint = routePoints[currentStepIndex + 1]
                            val currentPoint = routePoints[currentStepIndex]
                            
                            // 计算方向
                            val currentBearing = calculateBearing(currentPoint, nextPoint)
                            val nextBearing = if (currentStepIndex < routePoints.size - 2) {
                                calculateBearing(nextPoint, routePoints[currentStepIndex + 2])
                            } else {
                                currentBearing
                            }
                            
                            // 更新转向信息
                            nextTurnInfo = "下一步: ${getTurnDirection(currentBearing, nextBearing)}"
                            Log.d("MapScreen", nextTurnInfo)
                            
                            // 如果足够接近下一个点，则移动到下一步
                            val distanceToNext = calculateDistance(
                                LatLng(it.latitude, it.longitude),
                                nextPoint
                            )
                            Log.d("MapScreen", "Distance to next point: $distanceToNext meters")
                            if (distanceToNext < 10) { // 10米阈值
                                currentStepIndex++
                                Log.d("MapScreen", "Moving to next point: $currentStepIndex")
                            }
                            
                            // 更新相机位置，保持在用户位置上方
                            cameraPositionState.position = CameraPosition.Builder()
                                .target(LatLng(it.latitude, it.longitude))
                                .zoom(18f)
                                .bearing(currentBearing.toFloat()) // 相机方向跟随路线方向
                                .tilt(45f) // 添加倾斜角度以获得更好的导航视角
                                .build()
                        }
                    }
                }
                delay(1000) // 每秒更新一次
            }
            
            // 导航完成
            if (currentStepIndex >= routePoints.size - 1) {
                Log.d("MapScreen", "Navigation completed")
                isNavigating = false
                nextTurnInfo = "已到达目的地"
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("FedUni Wayfinding") },
                actions = {
                    IconButton(onClick = { showBuildingList = !showBuildingList }) {
                        Icon(Icons.Default.List, contentDescription = "Building List")
                    }
                }
            )
        },
        floatingActionButton = {
            Column {
                FloatingActionButton(
                    onClick = {
                        if (hasLocationPermission) {
                            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                                location?.let {
                                    userLocation = LatLng(it.latitude, it.longitude)
                                    cameraPositionState.position = CameraPosition.fromLatLngZoom(
                                        LatLng(it.latitude, it.longitude),
                                        17f
                                    )
                                }
                            }
                        }
                    },
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = "My Location")
                }
                
                if (!isNavigating) {
                    FloatingActionButton(
                        onClick = { 
                            showStartBuildingSelection = true
                            showEndBuildingSelection = false
                            selectionMessage = "请选择起点"
                            showSelectionMessage = true
                        }
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = "Set Start Location")
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    FloatingActionButton(
                        onClick = { 
                            showEndBuildingSelection = true
                            showStartBuildingSelection = false
                            selectionMessage = "请选择终点"
                            showSelectionMessage = true
                        }
                    ) {
                        Icon(Icons.Default.Place, contentDescription = "Set Destination")
                    }

                    if (selectedStartBuilding != null && selectedEndBuilding != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        FloatingActionButton(
                            onClick = { startNavigation() }
                        ) {
                            Icon(Icons.Default.Navigation, contentDescription = "Start Navigation")
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        FloatingActionButton(
                            onClick = { 
                                selectedStartBuilding = null
                                selectedEndBuilding = null
                                routePoints = emptyList()
                                navigationInfo = null
                            }
                        ) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear Route")
                        }
                    }
                } else {
                    FloatingActionButton(
                        onClick = { 
                            isNavigating = false
                            navigationJob?.cancel()
                            nextTurnInfo = ""
                        }
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Stop Navigation")
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    isMyLocationEnabled = hasLocationPermission
                )
            ) {
                // Display all building markers
                BuildingData.buildings.forEach { building ->
                    val isStart = building == selectedStartBuilding
                    val isEnd = building == selectedEndBuilding
                    
                    Marker(
                        state = MarkerState(position = building.location),
                        title = building.name,
                        snippet = building.description,
                        icon = when {
                            isStart -> BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                            isEnd -> BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                            else -> BitmapDescriptorFactory.defaultMarker()
                        },
                        onClick = {
                            when {
                                showStartBuildingSelection -> {
                                    selectedStartBuilding = building
                                    showStartBuildingSelection = false
                                    showSelectionMessage = false
                                    if (selectedEndBuilding != null) {
                                        calculateRoute()
                                    }
                                }
                                showEndBuildingSelection -> {
                                    selectedEndBuilding = building
                                    showEndBuildingSelection = false
                                    showSelectionMessage = false
                                    if (selectedStartBuilding != null) {
                                        calculateRoute()
                                    }
                                }
                            }
                            true
                        }
                    )
                }

                // Draw route
                if (routePoints.isNotEmpty()) {
                    Log.d("MapScreen", "Drawing route with ${routePoints.size} points")
                    Polyline(
                        points = routePoints,
                        color = Color.Blue,
                        width = 12f,
                        zIndex = 1f
                    )
                }
            }

            // Building selection dialog
            if (showBuildingList) {
                AlertDialog(
                    onDismissRequest = { showBuildingList = false },
                    title = { Text("校园建筑") },
                    text = {
                        Column {
                            BuildingData.buildings.forEach { building ->
                                Text(
                                    text = building.name,
                                    modifier = Modifier
                                        .clickable {
                                            cameraPositionState.position = CameraPosition.fromLatLngZoom(
                                                building.location,
                                                18f
                                            )
                                            showBuildingList = false
                                        }
                                        .padding(vertical = 8.dp)
                                )
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showBuildingList = false }) {
                            Text("关闭")
                        }
                    }
                )
            }

            // Display navigation info
            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text("起点: ${selectedStartBuilding?.name ?: "请选择起点"}")
                    Text("终点: ${selectedEndBuilding?.name ?: "请选择终点"}")
                    if (navigationInfo != null) {
                        Divider(modifier = Modifier.padding(vertical = 4.dp))
                        Text(navigationInfo!!)
                    }
                    if (nextTurnInfo.isNotEmpty()) {
                        Divider(modifier = Modifier.padding(vertical = 4.dp))
                        Text(nextTurnInfo)
                    }
                }
            }

            // Selection message
            if (showSelectionMessage) {
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                ) {
                    Text(selectionMessage)
                }
            }
        }
    }
} 