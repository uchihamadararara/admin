package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*

@Composable
fun StatusPill(
    text: String,
    backgroundColor: Color,
    textColor: Color,
    borderColor: Color? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(backgroundColor)
            .then(
                if (borderColor != null) Modifier.border(1.dp, borderColor, CircleShape)
                else Modifier
            )
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.6.sp
        )
    }
}

@Composable
fun AccessTypePill(type: AccessType) {
    when (type) {
        AccessType.FREE -> StatusPill(
            text = "FREE",
            backgroundColor = GeoEmeraldContainer,
            textColor = GeoEmeraldText,
            borderColor = GeoEmerald.copy(alpha = 0.4f)
        )
        AccessType.PREMIUM -> StatusPill(
            text = "PREMIUM",
            backgroundColor = GeoTertiaryContainer,
            textColor = GeoOnTertiaryContainer,
            borderColor = GeoTertiary.copy(alpha = 0.3f)
        )
    }
}

@Composable
fun ContentTypePill(type: ContentType, experienceType: LiveExperienceType? = null) {
    when (type) {
        ContentType.STATIC -> StatusPill(
            text = "STATIC",
            backgroundColor = GeoSurfaceVariant,
            textColor = TextSecondary,
            borderColor = GeoCardBorder
        )
        ContentType.LIVE -> {
            val label = when (experienceType) {
                LiveExperienceType.NORMAL -> "LIVE • NORMAL"
                LiveExperienceType.TRANSITION -> "LIVE • TRANSITION"
                else -> "LIVE"
            }
            StatusPill(
                text = label,
                backgroundColor = GeoSecondaryContainer,
                textColor = GeoOnSecondaryContainer,
                borderColor = GeoSecondary.copy(alpha = 0.3f)
            )
        }
    }
}

@Composable
fun WallpaperStatusPill(status: WallpaperStatus) {
    when (status) {
        WallpaperStatus.PUBLISHED -> StatusPill(
            text = "PUBLISHED",
            backgroundColor = GeoEmeraldContainer,
            textColor = GeoEmeraldText,
            borderColor = GeoEmerald.copy(alpha = 0.3f)
        )
        WallpaperStatus.DRAFT -> StatusPill(
            text = "DRAFT",
            backgroundColor = GeoSurfaceVariant,
            textColor = TextSecondary,
            borderColor = GeoCardBorder
        )
        WallpaperStatus.INACTIVE -> StatusPill(
            text = "INACTIVE",
            backgroundColor = GeoGoldContainer,
            textColor = GeoGoldText,
            borderColor = GeoGold.copy(alpha = 0.3f)
        )
        WallpaperStatus.ARCHIVED -> StatusPill(
            text = "ARCHIVED",
            backgroundColor = GeoOutlineVariant,
            textColor = TextMuted
        )
    }
}

@Composable
fun RolePill(role: AdminRole) {
    val (bg, txt) = when (role) {
        AdminRole.SUPER_ADMIN -> GeoPrimaryContainer to GeoOnPrimaryContainer
        AdminRole.ADMIN -> GeoSecondaryContainer to GeoOnSecondaryContainer
        AdminRole.CONTENT_MANAGER -> GeoEmeraldContainer to GeoEmeraldText
        AdminRole.MODERATOR -> GeoTertiaryContainer to GeoOnTertiaryContainer
        AdminRole.SUPPORT -> GeoSurfaceVariant to TextSecondary
    }
    StatusPill(
        text = role.displayName.uppercase(),
        backgroundColor = bg,
        textColor = txt,
        borderColor = txt.copy(alpha = 0.2f)
    )
}

