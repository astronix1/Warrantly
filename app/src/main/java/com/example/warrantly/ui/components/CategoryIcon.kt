package com.example.warrantly.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Chair
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.Headphones
import androidx.compose.material.icons.outlined.Kitchen
import androidx.compose.material.icons.outlined.LaptopMac
import androidx.compose.material.icons.outlined.Smartphone
import androidx.compose.material.icons.outlined.TabletAndroid
import androidx.compose.material.icons.outlined.Tv
import androidx.compose.material.icons.outlined.Watch
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.warrantly.data.model.ProductCategory

@Composable
fun CategoryIcon(
    category: ProductCategory,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
) {
    val icon = when (category) {
        ProductCategory.SMARTPHONE -> Icons.Outlined.Smartphone
        ProductCategory.LAPTOP -> Icons.Outlined.LaptopMac
        ProductCategory.TABLET -> Icons.Outlined.TabletAndroid
        ProductCategory.AUDIO -> Icons.Outlined.Headphones
        ProductCategory.TV_DISPLAY -> Icons.Outlined.Tv
        ProductCategory.HOME_APPLIANCE -> Icons.Outlined.Kitchen
        ProductCategory.CAMERA -> Icons.Outlined.CameraAlt
        ProductCategory.WEARABLE -> Icons.Outlined.Watch
        ProductCategory.FURNITURE -> Icons.Outlined.Chair
        ProductCategory.VEHICLE -> Icons.Outlined.DirectionsCar
        ProductCategory.OTHER -> Icons.Outlined.Category
    }

    Icon(
        imageVector = icon,
        contentDescription = category.displayName,
        modifier = modifier,
        tint = tint,
    )
}
