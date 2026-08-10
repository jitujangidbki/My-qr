package com.example.generator

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.EnumMap

object QrBitmapGenerator {

    suspend fun generateQrBitmap(
        content: String,
        options: QrOptions,
        targetSizePx: Int = 1024
    ): Bitmap = withContext(Dispatchers.Default) {
        if (content.isBlank()) {
            return@withContext createBlankBitmap(targetSizePx, options.bgColor, options.isBgTransparent)
        }

        val zxingEcc = if (options.logoBitmap != null) {
            ErrorCorrectionLevel.H // High EC is mandatory for logos so scanner can recover covered modules
        } else {
            when (options.eccLevel) {
                EccLevel.L -> ErrorCorrectionLevel.L
                EccLevel.M -> ErrorCorrectionLevel.M
                EccLevel.Q -> ErrorCorrectionLevel.Q
                EccLevel.H -> ErrorCorrectionLevel.H
            }
        }

        // Set MARGIN hint to 0 so ZXing bitMatrix aligns 1:1 without extra padding in the matrix
        val hints = EnumMap<EncodeHintType, Any>(EncodeHintType::class.java).apply {
            put(EncodeHintType.ERROR_CORRECTION, zxingEcc)
            put(EncodeHintType.MARGIN, 0)
            put(EncodeHintType.CHARACTER_SET, "UTF-8")
        }

        val qrWriter = QRCodeWriter()
        val bitMatrix = try {
            qrWriter.encode(content, BarcodeFormat.QR_CODE, 0, 0, hints)
        } catch (e: Exception) {
            return@withContext createBlankBitmap(targetSizePx, options.bgColor, options.isBgTransparent)
        }

        val matrixWidth = bitMatrix.width
        val matrixHeight = bitMatrix.height

        val paddingModules = options.margin.coerceAtLeast(4)
        val totalWidthModules = matrixWidth + 2 * paddingModules
        val totalHeightModules = matrixHeight + 2 * paddingModules

        val bitmap = Bitmap.createBitmap(targetSizePx, targetSizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Clear background
        if (!options.isBgTransparent) {
            canvas.drawColor(options.bgColor)
        } else {
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        }

        val cellSize = targetSizePx.toFloat() / totalWidthModules.toFloat()

        val fgPaint = Paint().apply {
            color = options.fgColor
            style = Paint.Style.FILL
            isAntiAlias = false
        }

        val eyePaint = Paint().apply {
            color = options.fgColor
            style = Paint.Style.FILL
            isAntiAlias = false
        }

        // Helper to check if (col, row) in bitMatrix is part of one of the 3 finder eyes (7x7)
        fun isFinderEye(col: Int, row: Int): Boolean {
            val isTopLeft = col in 0..6 && row in 0..6
            val isTopRight = col in (matrixWidth - 7) until matrixWidth && row in 0..6
            val isBottomLeft = col in 0..6 && row in (matrixHeight - 7) until matrixHeight
            return isTopLeft || isTopRight || isBottomLeft
        }

        // Draw data modules (non-finder)
        for (row in 0 until matrixHeight) {
            for (col in 0 until matrixWidth) {
                if (bitMatrix.get(col, row)) {
                    if (!isFinderEye(col, row)) {
                        val left = (col + paddingModules) * cellSize
                        val top = (row + paddingModules) * cellSize
                        val right = left + cellSize
                        val bottom = top + cellSize

                        when (options.dotStyle) {
                            DotStyle.SQUARE -> {
                                canvas.drawRect(left, top, right, bottom, fgPaint)
                            }
                            DotStyle.ROUNDED -> {
                                val radius = cellSize * 0.35f
                                canvas.drawRoundRect(
                                    RectF(left, top, right, bottom),
                                    radius,
                                    radius,
                                    fgPaint
                                )
                            }
                            DotStyle.CIRCLE -> {
                                val cx = left + cellSize / 2f
                                val cy = top + cellSize / 2f
                                val radius = cellSize * 0.45f
                                canvas.drawCircle(cx, cy, radius, fgPaint)
                            }
                            DotStyle.SOFT_SQUARE -> {
                                val radius = cellSize * 0.2f
                                canvas.drawRoundRect(
                                    RectF(left, top, right, bottom),
                                    radius,
                                    radius,
                                    fgPaint
                                )
                            }
                        }
                    }
                }
            }
        }

        // Draw Finder Pattern Eyes (Top-Left, Top-Right, Bottom-Left)
        drawEyePattern(canvas, paddingModules, paddingModules, cellSize, options.eyeStyle, eyePaint, options.bgColor, options.isBgTransparent)
        drawEyePattern(canvas, paddingModules + matrixWidth - 7, paddingModules, cellSize, options.eyeStyle, eyePaint, options.bgColor, options.isBgTransparent)
        drawEyePattern(canvas, paddingModules, paddingModules + matrixHeight - 7, cellSize, options.eyeStyle, eyePaint, options.bgColor, options.isBgTransparent)

        // Overlay Logo if available
        options.logoBitmap?.let { rawLogo ->
            val logoScaleFraction = (options.logoScalePercent.coerceIn(10, 30) / 100f)
            val maxLogoSize = targetSizePx * logoScaleFraction

            val logoWidth: Float
            val logoHeight: Float
            val aspect = rawLogo.width.toFloat() / rawLogo.height.toFloat()
            if (aspect >= 1f) {
                logoWidth = maxLogoSize
                logoHeight = maxLogoSize / aspect
            } else {
                logoHeight = maxLogoSize
                logoWidth = maxLogoSize * aspect
            }

            val centerX = targetSizePx / 2f
            val centerY = targetSizePx / 2f

            val logoLeft = centerX - logoWidth / 2f
            val logoTop = centerY - logoHeight / 2f
            val logoRight = logoLeft + logoWidth
            val logoBottom = logoTop + logoHeight

            val paddingPx = options.logoPaddingDp * (targetSizePx / 512f)

            // Draw Logo Background Badge
            if (options.hasLogoBg) {
                val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = options.logoBgColor
                    style = Paint.Style.FILL
                }
                val bgRect = RectF(
                    logoLeft - paddingPx,
                    logoTop - paddingPx,
                    logoRight + paddingPx,
                    logoBottom + paddingPx
                )

                when (options.logoShape) {
                    LogoShape.SQUARE -> canvas.drawRect(bgRect, bgPaint)
                    LogoShape.ROUNDED -> {
                        val rx = bgRect.width() * 0.2f
                        canvas.drawRoundRect(bgRect, rx, rx, bgPaint)
                    }
                    LogoShape.CIRCLE -> {
                        val bgRadius = (bgRect.width() / 2f).coerceAtLeast(bgRect.height() / 2f)
                        canvas.drawCircle(centerX, centerY, bgRadius, bgPaint)
                    }
                }
            }

            // Draw Clipped Logo
            val logoRect = RectF(logoLeft, logoTop, logoRight, logoBottom)
            val logoPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
                alpha = (options.logoOpacity * 255).toInt().coerceIn(0, 255)
            }

            val logoCanvasBitmap = Bitmap.createBitmap(
                logoWidth.toInt().coerceAtLeast(1),
                logoHeight.toInt().coerceAtLeast(1),
                Bitmap.Config.ARGB_8888
            )
            val logoCanvas = Canvas(logoCanvasBitmap)
            val tempPath = Path()

            when (options.logoShape) {
                LogoShape.SQUARE -> tempPath.addRect(0f, 0f, logoWidth, logoHeight, Path.Direction.CW)
                LogoShape.ROUNDED -> {
                    val rx = logoWidth * 0.2f
                    tempPath.addRoundRect(RectF(0f, 0f, logoWidth, logoHeight), rx, rx, Path.Direction.CW)
                }
                LogoShape.CIRCLE -> {
                    val r = minOf(logoWidth, logoHeight) / 2f
                    tempPath.addCircle(logoWidth / 2f, logoHeight / 2f, r, Path.Direction.CW)
                }
            }

            logoCanvas.clipPath(tempPath)
            logoCanvas.drawBitmap(
                rawLogo,
                null,
                RectF(0f, 0f, logoWidth, logoHeight),
                Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
            )

            canvas.drawBitmap(logoCanvasBitmap, logoLeft, logoTop, logoPaint)
        }

        return@withContext bitmap
    }

    private fun drawEyePattern(
        canvas: Canvas,
        startCol: Int,
        startRow: Int,
        cellSize: Float,
        style: EyeStyle,
        eyePaint: Paint,
        bgColor: Int,
        isBgTransparent: Boolean
    ) {
        val outerLeft = startCol * cellSize
        val outerTop = startRow * cellSize
        val outerRight = outerLeft + 7 * cellSize
        val outerBottom = outerTop + 7 * cellSize
        val outerRect = RectF(outerLeft, outerTop, outerRight, outerBottom)

        val innerLeft = startCol * cellSize + 1 * cellSize
        val innerTop = startRow * cellSize + 1 * cellSize
        val innerRight = innerLeft + 5 * cellSize
        val innerBottom = innerTop + 5 * cellSize
        val innerRect = RectF(innerLeft, innerTop, innerRight, innerBottom)

        val centerLeft = startCol * cellSize + 2 * cellSize
        val centerTop = startRow * cellSize + 2 * cellSize
        val centerRight = centerLeft + 3 * cellSize
        val centerBottom = centerTop + 3 * cellSize
        val centerRect = RectF(centerLeft, centerTop, centerRight, centerBottom)

        val bgPaint = Paint().apply {
            color = if (isBgTransparent) Color.TRANSPARENT else bgColor
            this.style = Paint.Style.FILL
            isAntiAlias = false
            if (isBgTransparent) {
                xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
            }
        }

        when (style) {
            EyeStyle.SQUARE -> {
                canvas.drawRect(outerRect, eyePaint)
                canvas.drawRect(innerRect, bgPaint)
                canvas.drawRect(centerRect, eyePaint)
            }
            EyeStyle.ROUNDED -> {
                val outerRx = 7 * cellSize * 0.25f
                val innerRx = 5 * cellSize * 0.22f
                val centerRx = 3 * cellSize * 0.20f

                canvas.drawRoundRect(outerRect, outerRx, outerRx, eyePaint)
                canvas.drawRoundRect(innerRect, innerRx, innerRx, bgPaint)
                canvas.drawRoundRect(centerRect, centerRx, centerRx, eyePaint)
            }
            EyeStyle.CIRCLE -> {
                val outerR = 7 * cellSize / 2f
                val innerR = 5 * cellSize / 2f
                val centerR = 3 * cellSize / 2f

                val cx = outerLeft + outerR
                val cy = outerTop + outerR

                canvas.drawCircle(cx, cy, outerR, eyePaint)
                canvas.drawCircle(cx, cy, innerR, bgPaint)
                canvas.drawCircle(cx, cy, centerR, eyePaint)
            }
        }
    }

    private fun createBlankBitmap(sizePx: Int, bgColor: Int, isTransparent: Boolean): Bitmap {
        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        if (!isTransparent) {
            canvas.drawColor(bgColor)
        } else {
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        }
        return bitmap
    }
}
