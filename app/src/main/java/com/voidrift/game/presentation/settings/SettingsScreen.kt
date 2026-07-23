package com.voidrift.game.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.voidrift.game.ui.theme.DividerColor
import com.voidrift.game.ui.theme.NeonCyan
import com.voidrift.game.ui.theme.NeonPink
import com.voidrift.game.ui.theme.TextPrimary
import com.voidrift.game.ui.theme.TextSecondary
import com.voidrift.game.ui.theme.TorchAmber
import com.voidrift.game.ui.theme.VoidBackground
import com.voidrift.game.ui.theme.VoidCorrupt
import com.voidrift.game.ui.theme.VoidSurfaceVariant

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(VoidBackground, VoidCorrupt, VoidBackground)))
    ) {
        Column(Modifier.fillMaxSize()) {
            // ── Header ────────────────────────────────────────────────────
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = NeonCyan)
                }
                Text(
                    "SETTINGS",
                    color         = NeonPink,
                    fontSize      = 22.sp,
                    fontWeight    = FontWeight.ExtraBold,
                    letterSpacing = 4.sp
                )
            }

            Spacer(Modifier.height(8.dp))

            SettingsToggle(label = "Sound Effects", checked = state.sfxEnabled, tint = NeonCyan,
                onToggle = { viewModel.toggleSfx() })

            SettingsDivider()

            SettingsToggle(label = "Music", checked = state.musicEnabled, tint = TorchAmber,
                onToggle = { viewModel.toggleMusic() })

            SettingsDivider()

            SettingsToggle(label = "Screen Shake", checked = state.screenShakeEnabled, tint = NeonPink,
                onToggle = { viewModel.toggleShake() })

            SettingsDivider()

            // SFX Volume slider
            Column(Modifier.padding(horizontal = 24.dp, vertical = 12.dp)) {
                Text("SFX VOLUME", color = TextSecondary, fontSize = 12.sp,
                    fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                Spacer(Modifier.height(8.dp))
                Slider(
                    value         = state.sfxVolume,
                    onValueChange = { viewModel.setSfxVolume(it) },
                    valueRange    = 0f..1f,
                    colors = SliderDefaults.colors(
                        thumbColor         = NeonCyan,
                        activeTrackColor   = NeonCyan,
                        inactiveTrackColor = VoidSurfaceVariant
                    )
                )
            }

            SettingsDivider()

            Spacer(Modifier.weight(1f))
            Text(
                "VOIDRIFT v1.0.0  —  Material 3 Dark Theme Only",
                color     = TextSecondary.copy(alpha = 0.5f),
                fontSize  = 11.sp,
                modifier  = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 24.dp)
            )
        }
    }
}

@Composable
private fun SettingsToggle(
    label: String, checked: Boolean, tint: Color, onToggle: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = TextPrimary, fontSize = 15.sp, modifier = Modifier.weight(1f))
        Switch(
            checked         = checked,
            onCheckedChange = { onToggle() },
            colors = SwitchDefaults.colors(
                checkedThumbColor   = tint,
                checkedTrackColor   = tint.copy(alpha = 0.3f),
                uncheckedThumbColor = TextSecondary,
                uncheckedTrackColor = VoidSurfaceVariant
            )
        )
    }
}

@Composable
private fun SettingsDivider() {
    Divider(color = DividerColor, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
}
