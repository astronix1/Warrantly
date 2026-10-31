package com.example.warrantly.data.model

enum class ProductCategory(val displayName: String, val iconName: String) {
    SMARTPHONE("Smartphone", "smartphone"),
    LAPTOP("Laptop", "laptop_mac"),
    TABLET("Tablet", "tablet"),
    AUDIO("Audio", "headphones"),
    TV_DISPLAY("TV & Display", "tv"),
    HOME_APPLIANCE("Home Appliance", "kitchen"),
    CAMERA("Camera", "camera_alt"),
    WEARABLE("Wearable", "watch"),
    FURNITURE("Furniture", "chair"),
    VEHICLE("Vehicle", "directions_car"),
    OTHER("Other", "category")
}
