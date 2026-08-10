package com.example.generator

import android.graphics.Bitmap
import android.graphics.Color

enum class DotStyle(val displayName: String) {
    SQUARE("Square"),
    ROUNDED("Rounded"),
    CIRCLE("Circle"),
    SOFT_SQUARE("Soft")
}

enum class EyeStyle(val displayName: String) {
    SQUARE("Classic Square"),
    ROUNDED("Smooth Rounded"),
    CIRCLE("Circle Eye")
}

enum class EccLevel(val label: String, val capacityPercent: String) {
    L("Low (7%)", "~7% recovery"),
    M("Medium (15%)", "~15% recovery"),
    Q("Quartile (25%)", "~25% recovery"),
    H("High (30%)", "~30% recovery (Best for Logos)")
}

enum class LogoShape(val displayName: String) {
    SQUARE("Square"),
    ROUNDED("Rounded"),
    CIRCLE("Circular")
}

data class QrOptions(
    val fgColor: Int = Color.BLACK,
    val bgColor: Int = Color.WHITE,
    val isBgTransparent: Boolean = false,
    val dotStyle: DotStyle = DotStyle.SQUARE,
    val eyeStyle: EyeStyle = EyeStyle.SQUARE,
    val margin: Int = 2,
    val eccLevel: EccLevel = EccLevel.M,
    val logoUri: String? = null,
    val logoBitmap: Bitmap? = null,
    val logoScalePercent: Int = 20, // 10% to 30%
    val logoShape: LogoShape = LogoShape.ROUNDED,
    val hasLogoBg: Boolean = true,
    val logoBgColor: Int = Color.WHITE,
    val logoPaddingDp: Int = 6,
    val logoOpacity: Float = 1.0f
)
