package com.fedunimap.wayfinding

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.net.URLEncoder

object RouteCalculator {
    private const val TAG = "RouteCalculator"

    suspend fun getWalkingRoute(origin: LatLng, destination: LatLng, apiKey: String): List<LatLng> = withContext(Dispatchers.IO) {
        try {
            val originStr = "${origin.latitude},${origin.longitude}"
            val destStr = "${destination.latitude},${destination.longitude}"
            
            val urlString = "https://maps.googleapis.com/maps/api/directions/json?" +
                    "origin=${URLEncoder.encode(originStr, "UTF-8")}&" +
                    "destination=${URLEncoder.encode(destStr, "UTF-8")}&" +
                    "mode=walking&" +
                    "key=$apiKey"
            
            Log.d(TAG, "Requesting route with URL: ${urlString.replace(apiKey, "API_KEY")}")
            
            val response = URL(urlString).readText()
            Log.d(TAG, "Received response: ${response.take(500)}...")
            
            val jsonResponse = JSONObject(response)
            val routes = jsonResponse.getJSONArray("routes")
            
            if (routes.length() == 0) {
                Log.e(TAG, "No routes found in response")
                return@withContext listOf(origin, destination)
            }
            
            val points = mutableListOf<LatLng>()
            val legs = routes.getJSONObject(0).getJSONArray("legs")
            
            for (i in 0 until legs.length()) {
                val steps = legs.getJSONObject(i).getJSONArray("steps")
                Log.d(TAG, "Processing ${steps.length()} steps")
                
                for (j in 0 until steps.length()) {
                    val step = steps.getJSONObject(j)
                    val startLoc = step.getJSONObject("start_location")
                    val endLoc = step.getJSONObject("end_location")
                    
                    val startLatLng = LatLng(startLoc.getDouble("lat"), startLoc.getDouble("lng"))
                    val endLatLng = LatLng(endLoc.getDouble("lat"), endLoc.getDouble("lng"))
                    
                    if (points.isEmpty() || points.last() != startLatLng) {
                        points.add(startLatLng)
                    }
                    points.add(endLatLng)
                }
            }
            
            Log.d(TAG, "Generated ${points.size} route points")
            points
            
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating route", e)
            listOf(origin, destination)
        }
    }
    
    private fun decodePolyline(encoded: String): List<LatLng> {
        val poly = mutableListOf<LatLng>()
        var index = 0
        var lat = 0
        var lng = 0
        
        try {
            while (index < encoded.length) {
                var b: Int
                var shift = 0
                var result = 0
                
                do {
                    b = encoded[index++].code - 63
                    result = result or (b and 0x1f shl shift)
                    shift += 5
                } while (b >= 0x20)
                
                val dlat = if (result and 1 != 0) -(result shr 1) else result shr 1
                lat += dlat
                
                shift = 0
                result = 0
                
                do {
                    b = encoded[index++].code - 63
                    result = result or (b and 0x1f shl shift)
                    shift += 5
                } while (b >= 0x20)
                
                val dlng = if (result and 1 != 0) -(result shr 1) else result shr 1
                lng += dlng
                
                poly.add(LatLng(lat.toDouble() / 1E5, lng.toDouble() / 1E5))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error decoding polyline", e)
        }
        
        return poly
    }
} 