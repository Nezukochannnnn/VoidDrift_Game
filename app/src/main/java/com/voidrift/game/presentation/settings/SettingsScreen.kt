package com.voidrift.game.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.voidrift.game.R
import com.voidrift.game.ui.theme.DividerColor
import com.voidrift.game.ui.theme.NeonCyan
import com.voidrift.game.ui.theme.NeonPink
import com.voidrift.game.ui.theme.TextPrimary
import com.voidrift.game.ui.theme.TextSecondary
import com.voidrift.game.ui.theme.TorchAmber
import com.voidrift.game.ui.theme.VoidBackground
import com.voidrift.game.ui.theme.VoidCorrupt
import com.voidrift.game.ui.theme.VoidSurface
import com.voidrift.game.ui.theme.VoidSurfaceVariant

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    var showWipeDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(VoidBackground, VoidCorrupt, VoidBackground)))
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // ── Header ────────────────────────────────────────────────────
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.btn_home), tint = NeonCyan)
                }
                Text(
                    stringResource(R.string.settings_title),
                    color         = NeonPink,
                    fontSize      = 22.sp,
                    fontWeight    = FontWeight.ExtraBold,
                    letterSpacing = 4.sp
                )
            }

            Spacer(Modifier.height(8.dp))

            // ── Player Name Input ──────────────────────────────────────────
            Column(Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
                Text(
                    stringResource(R.string.lbl_player_name),
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = state.playerName,
                    onValueChange = { viewModel.updatePlayerName(it) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = VoidSurfaceVariant,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            SettingsDivider()

            // ── Toggles ───────────────────────────────────────────────────
            SettingsToggle(label = stringResource(R.string.lbl_sound_effects), checked = state.sfxEnabled, tint = NeonCyan,
                onToggle = { viewModel.toggleSfx() })

            SettingsDivider()

            SettingsToggle(label = stringResource(R.string.lbl_music), checked = state.musicEnabled, tint = TorchAmber,
                onToggle = { viewModel.toggleMusic() })

            SettingsDivider()

            SettingsToggle(label = stringResource(R.string.lbl_haptics), checked = state.hapticsEnabled, tint = NeonPink,
                onToggle = { viewModel.toggleHaptics() })

            SettingsDivider()

            // ── Graphics Quality Selector ─────────────────────────────────
            Column(Modifier.padding(horizontal = 24.dp, vertical = 12.dp)) {
                Text(
                    stringResource(R.string.lbl_graphics_quality),
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GraphicsQuality.entries.forEach { quality ->
                        val isSelected = state.graphicsQuality == quality
                        Button(
                            onClick = { viewModel.setGraphicsQuality(quality) },
                            modifier = Modifier.weight(1f).height(44.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) NeonCyan.copy(alpha = 0.25f) else VoidSurface
                            )
                        ) {
                            Text(
                                text = quality.name,
                                color = if (isSelected) NeonCyan else TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            SettingsDivider()

            // ── SFX Volume Slider ──────────────────────────────────────────
            Column(Modifier.padding(horizontal = 24.dp, vertical = 12.dp)) {
                Text(
                    stringResource(R.string.lbl_sfx_volume),
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
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

            Spacer(Modifier.height(24.dp))

            // ── Danger Button: WIPE ALL DATA ──────────────────────────────
            Button(
                onClick = { showWipeDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3D0000))
            ) {
                Text(
                    stringResource(R.string.btn_wipe_data),
                    color = Color.Red,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
            }

            Spacer(Modifier.height(32.dp))
        }

        // Confirmation Dialog for Data Wipe
        if (showWipeDialog) {
            AlertDialog(
                onDismissRequest = { showWipeDialog = false },
                title = { Text(stringResource(R.string.confirm_wipe_title), color = Color.Red) },
                text = { Text(stringResource(R.string.confirm_wipe_msg), color = TextPrimary) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.wipeAllData()
                            showWipeDialog = false
                        }
                    ) {
                        Text(stringResource(R.string.btn_confirm), color = Color.Red, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showWipeDialog = false }) {
                        Text(stringResource(R.string.btn_cancel), color = TextSecondary)
                    }
                },
                containerColor = VoidSurface
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
    HorizontalDivider(color = DividerColor, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
}
