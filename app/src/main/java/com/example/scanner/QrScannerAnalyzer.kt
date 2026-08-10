package com.example.scanner

import android.graphics.ImageFormat
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.NotFoundException
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer
import java.nio.ByteBuffer

class QrScannerAnalyzer(
    private val onQrCodeDetected: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val reader = MultiFormatReader()
    private var isProcessing = false

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage == null || isProcessing) {
            imageProxy.close()
            return
        }

        if (imageProxy.format == ImageFormat.YUV_420_888 ||
            imageProxy.format == ImageFormat.YUV_422_888 ||
            imageProxy.format == ImageFormat.YUV_444_888
        ) {
            isProcessing = true
            val buffer = mediaImage.planes[0].buffer
            val data = buffer.toByteArray()
            val width = imageProxy.width
            val height = imageProxy.height

            val source = PlanarYUVLuminanceSource(
                data,
                width,
                height,
                0,
                0,
                width,
                height,
                false
            )

            val binaryBitmap = BinaryBitmap(HybridBinarizer(source))

            try {
                val result = reader.decode(binaryBitmap)
                result?.text?.let { text ->
                    if (text.isNotBlank()) {
                        onQrCodeDetected(text)
                    }
                }
            } catch (_: NotFoundException) {
                // No QR found in this frame
            } catch (_: Exception) {
                // Ignore decoding errors
            } finally {
                isProcessing = false
                imageProxy.close()
            }
        } else {
            imageProxy.close()
        }
    }

    private fun ByteBuffer.toByteArray(): ByteArray {
        rewind()
        val data = ByteArray(remaining())
        get(data)
        return data
    }
}
