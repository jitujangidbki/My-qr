package com.example.ui.customize

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.generator.DotStyle
import com.example.generator.EccLevel
import com.example.generator.EyeStyle
import com.example.generator.LogoShape
import com.example.ui.navigation.Screen
import com.example.viewmodel.MainViewModel

@Composable
fun CustomizeScreen(
    viewModel: MainViewModel,
    onNavigate: (Screen) -> Unit
) {
    val qrOptions by viewModel.qrOptions.collectAsState()
    val generatedBitmap by viewModel.generatedBitmap.collectAsState()

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.setLogoFromUri(uri)
        }
    }

    val presetFgColors = listOf(
        Color.Black,
        Color(0xFF4F46E5), // Indigo
        Color(0xFF0F172A), // Slate
        Color(0xFF0284C7), // Sky
        Color(0xFF059669), // Emerald
        Color(0xFFDC2626), // Red
        Color(0xFF7C3AED)  // Purple
    )

    val presetBgColors = listOf(
        Color.White,
        Color(0xFFF8FAFC),
        Color(0xFFFEF3C7),
        Color(0xFFE0E7FF),
        Color(0xFFDCFCE7),
        Color(0xFFFEE2E2)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Customize QR Code",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Live Preview Box
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (qrOptions.isBgTransparent) Color.LightGray.copy(alpha = 0.3f) else Color(qrOptions.bgColor))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (generatedBitmap != null) {
                        Image(
                            bitmap = generatedBitmap!!.asImageBitmap(),
                            contentDescription = "Customized QR",
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Scannability Grade Badge
                val isHighRiskLogo = qrOptions.logoBitmap != null && qrOptions.logoScalePercent > 22 && qrOptions.eccLevel in listOf(EccLevel.L, EccLevel.M)
                val isLowContrast = qrOptions.fgColor == qrOptions.bgColor

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isHighRiskLogo || isLowContrast) Color(0xFFFEF2F2) else Color(0xFFECFDF5)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isHighRiskLogo || isLowContrast) Icons.Default.Warning else Icons.Default.Security,
                            contentDescription = null,
                            tint = if (isHighRiskLogo || isLowContrast) Color(0xFFDC2626) else Color(0xFF059669),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isLowContrast) "Error: Colors too similar!"
                            else if (isHighRiskLogo) "Warning: High logo coverage. Increase ECC to High!"
                            else "100% Highly Scannable",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isHighRiskLogo || isLowContrast) Color(0xFFDC2626) else Color(0xFF059669),
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Panel 1: Foreground & Background Colors
        CustomSectionCard(title = "Colors") {
            Text("Foreground Color", style = MaterialTheme.typography.labelMedium)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(presetFgColors) { color ->
                    val selected = qrOptions.fgColor == android.graphics.Color.argb(
                        (color.alpha * 255).toInt(),
                        (color.red * 255).toInt(),
                        (color.green * 255).toInt(),
                        (color.blue * 255).toInt()
                    )
                    ColorSwatch(color = color, isSelected = selected) {
                        viewModel.updateOptions { opts ->
                            opts.copy(fgColor = android.graphics.Color.argb(
                                (color.alpha * 255).toInt(),
                                (color.red * 255).toInt(),
                                (color.green * 255).toInt(),
                                (color.blue * 255).toInt()
                            ))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text("Background Color", style = MaterialTheme.typography.labelMedium)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(presetBgColors) { color ->
                    val selected = !qrOptions.isBgTransparent && qrOptions.bgColor == android.graphics.Color.argb(
                        (color.alpha * 255).toInt(),
                        (color.red * 255).toInt(),
                        (color.green * 255).toInt(),
                        (color.blue * 255).toInt()
                    )
                    ColorSwatch(color = color, isSelected = selected) {
                        viewModel.updateOptions { opts ->
                            opts.copy(
                                isBgTransparent = false,
                                bgColor = android.graphics.Color.argb(
                                    (color.alpha * 255).toInt(),
                                    (color.red * 255).toInt(),
                                    (color.green * 255).toInt(),
                                    (color.blue * 255).toInt()
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Transparent Background", style = MaterialTheme.typography.bodyMedium)
                Switch(
                    checked = qrOptions.isBgTransparent,
                    onCheckedChange = { transparent ->
                        viewModel.updateOptions { it.copy(isBgTransparent = transparent) }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Panel 2: Dot Shapes & Eye Styles
        CustomSectionCard(title = "Shapes & Style") {
            Text("Dot Shape", style = MaterialTheme.typography.labelMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DotStyle.values().forEach { style ->
                    FilterChip(
                        selected = qrOptions.dotStyle == style,
                        onClick = { viewModel.updateOptions { it.copy(dotStyle = style) } },
                        label = { Text(style.displayName) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text("Corner Eye Style", style = MaterialTheme.typography.labelMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                EyeStyle.values().forEach { style ->
                    FilterChip(
                        selected = qrOptions.eyeStyle == style,
                        onClick = { viewModel.updateOptions { it.copy(eyeStyle = style) } },
                        label = { Text(style.displayName) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Panel 3: Margin & Error Correction Level
        CustomSectionCard(title = "Margin & Error Correction") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Quiet Zone (Margin)", style = MaterialTheme.typography.bodyMedium)
                Text("${qrOptions.margin} modules", style = MaterialTheme.typography.labelSmall)
            }
            Slider(
                value = qrOptions.margin.toFloat(),
                onValueChange = { m -> viewModel.updateOptions { it.copy(margin = m.toInt()) } },
                valueRange = 0f..6f,
                steps = 5
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text("Error Correction Level (ECC)", style = MaterialTheme.typography.labelMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                EccLevel.values().forEach { ecc ->
                    FilterChip(
                        selected = qrOptions.eccLevel == ecc,
                        onClick = { viewModel.updateOptions { it.copy(eccLevel = ecc) } },
                        label = { Text(ecc.name) }
                    )
                }
            }
            Text(
                text = qrOptions.eccLevel.capacityPercent,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Panel 4: Center Logo Overlay
        CustomSectionCard(title = "Center Logo / Image") {
            if (qrOptions.logoBitmap == null) {
                Button(
                    onClick = { galleryLauncher.launch("image/*") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pick_logo_btn")
                ) {
                    Icon(imageVector = Icons.Default.Image, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Select Logo from Gallery")
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Logo Selected", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                    IconButton(onClick = { viewModel.setLogoFromUri(null) }) {
                        Icon(imageVector = Icons.Default.Clear, contentDescription = "Remove Logo", tint = Color.Red)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Logo Size", style = MaterialTheme.typography.bodySmall)
                    Text("${qrOptions.logoScalePercent}%", style = MaterialTheme.typography.labelSmall)
                }
                Slider(
                    value = qrOptions.logoScalePercent.toFloat(),
                    onValueChange = { size -> viewModel.updateOptions { it.copy(logoScalePercent = size.toInt()) } },
                    valueRange = 10f..30f
                )

                Text("Logo Cutout Shape", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    LogoShape.values().forEach { shape ->
                        FilterChip(
                            selected = qrOptions.logoShape == shape,
                            onClick = { viewModel.updateOptions { it.copy(logoShape = shape) } },
                            label = { Text(shape.displayName) }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Background Badge", style = MaterialTheme.typography.bodyMedium)
                    Switch(
                        checked = qrOptions.hasLogoBg,
                        onCheckedChange = { bg -> viewModel.updateOptions { it.copy(hasLogoBg = bg) } }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Bottom Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = { onNavigate(Screen.Scan) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.QrCodeScanner, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Test Scan")
            }

            Button(
                onClick = {
                    viewModel.saveCurrentQrToHistory {
                        onNavigate(Screen.Create)
                    }
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Apply & Save")
            }
        }
    }
}

@Composable
fun CustomSectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            content()
        }
    }
}

@Composable
fun ColorSwatch(
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(color)
            .border(
                width = if (isSelected) 3.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.5f),
                shape = CircleShape
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = if (color == Color.White) Color.Black else Color.White,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
