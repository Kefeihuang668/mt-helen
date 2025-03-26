package com.fedunimap.wayfinding

import com.google.android.gms.maps.model.LatLng

data class Building(
    val id: String,
    val name: String,
    val description: String,
    val location: LatLng
)

object BuildingData {
    val buildings = listOf(
        Building(
            "1",
            "Building T",
            "Technology Building",
            LatLng(-37.631401, 143.889021)
        ),
        Building(
            "2",
            "Building F",
            "Federation Building",
            LatLng(-37.631046, 143.890201)
        ),
        Building(
            "3",
            "Building S",
            "Science Building",
            LatLng(-37.630657, 143.888843)
        ),
        Building(
            "4",
            "Building C",
            "Caro Convention Centre",
            LatLng(-37.631788, 143.890394)
        ),
        Building(
            "5",
            "Building H",
            "Health Sciences Building",
            LatLng(-37.632157, 143.889278)
        ),
        Building(
            "6",
            "Building P",
            "Physical Sciences Building",
            LatLng(-37.630213, 143.889664)
        ),
        Building(
            "7",
            "Library",
            "Main Library",
            LatLng(-37.631890, 143.888478)
        ),
        Building(
            "8",
            "Student Centre",
            "Student Services and Support",
            LatLng(-37.632456, 143.888199)
        )
    )
} 