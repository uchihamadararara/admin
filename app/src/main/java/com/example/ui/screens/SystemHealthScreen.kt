package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.StatusBadge
import com.example.ui.components.StatusBadgeType
import com.example.ui.theme.*

@Composable
fun SystemHealthScreen() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "OPERATIONAL HEALTH & OEM ARCHITECTURE",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Real-time backend status, secret isolation rules, and Android OEM runtime behavior",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Security & Secret Isolation Rules
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "1. SECURITY & SECRET ISOLATION MODEL",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = ChampagneGold
                    )
                    Text(
                        text = "• Cloudflare R2 secrets (R2_ACCESS_KEY_ID, R2_SECRET_ACCESS_KEY) and Supabase SERVICE_ROLE_KEY are strictly contained in Edge Functions and NEVER exposed to frontend browsers or client apps.\n" +
                                "• RLS policies enforce role checks server-side via get_current_admin_role().\n" +
                                "• Google Play RTDN events are processed via secure webhooks without exposing Play Console service account credentials.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Local Persistence & Premium Expiration Rules
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "2. LOCAL PERSISTENCE & SUBSCRIPTION RULES",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = ChampagneGold
                    )
                    Text(
                        text = "• Currently Applied Wallpaper Local Rule: The Android app retains active_live.mp4 and active_static.jpg in app-private persistent storage. If a wallpaper is deleted on the server, the user's currently applied wallpaper continues functioning offline and after reboot.\n" +
                                "• Premium Expiration Rule: When a user's subscription expires, their already applied Premium wallpaper continues to work. Only new Premium applications require an active entitlement.\n" +
                                "• Safe Replacement Workflow: The client uses pending media staging before replacing the active file.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // OEM Wallpaper Behavior Matrix
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "3. OEM RUNTIME COMPATIBILITY MATRIX",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = ChampagneGold
                    )
                    OemCard("Samsung OneUI 6+", "Standard WallpaperManager system picker + native ExoPlayer SurfaceView.", StatusBadgeType.SUCCESS)
                    OemCard("Xiaomi / HyperOS", "Standard WallpaperService intent with MIUI battery optimization whitelist guidelines.", StatusBadgeType.SUCCESS)
                    OemCard("OPPO / Realme UI", "Full support for 60fps MP4 loopers and content-driven charging triggers.", StatusBadgeType.SUCCESS)
                    OemCard("Google Pixel (AOSP)", "Clean system WallpaperService contract with zero background throttling.", StatusBadgeType.SUCCESS)
                }
            }
        }
    }
}

@Composable
private fun OemCard(brand: String, details: String, badgeType: StatusBadgeType) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = brand, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
            StatusBadge(text = "VERIFIED COMPATIBLE", type = badgeType)
        }
        Text(text = details, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
