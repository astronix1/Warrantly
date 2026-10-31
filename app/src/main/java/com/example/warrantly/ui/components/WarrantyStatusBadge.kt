package com.example.warrantly.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.warrantly.data.model.WarrantyStatus
import com.example.warrantly.ui.theme.WarrantlyColors

@Composable
fun WarrantyStatusBadge(
    status: WarrantyStatus,
    modifier: Modifier = Modifier
) {
    val containerColor = WarrantlyColors.statusContainerColor(status)
    val contentColor = WarrantlyColors.statusOnContainerColor(status)
    val (icon, label) = statusIconAndLabel(status)

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = containerColor,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(14.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                color = contentColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
            )
        }
    }
}

private fun statusIconAndLabel(status: WarrantyStatus): Pair<ImageVector, String> {
    return when (status) {
        WarrantyStatus.ACTIVE -> Pair(Icons.Outlined.CheckCircle, "Active")
        WarrantyStatus.EXPIRING_SOON -> Pair(Icons.Outlined.Warning, "Expiring Soon")
        WarrantyStatus.EXPIRED -> Pair(Icons.Outlined.Cancel, "Expired")
        WarrantyStatus.UNKNOWN -> Pair(Icons.Outlined.HelpOutline, "No Warranty")
    }
}
