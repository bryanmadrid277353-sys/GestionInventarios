package com.example.gestioninventarios.util

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix

object BarcodeGenerator {
    fun generateBarcodeBitmap(
        code: String,
        width: Int = 600,
        height: Int = 200
    ): Bitmap? {
        if (code.isBlank()) return null
        return try {
            val formatToUse = if (CodigoInterno.esEan13Valido(code)) {
                BarcodeFormat.EAN_13
            } else {
                BarcodeFormat.CODE_128
            }

            val writer = MultiFormatWriter()
            val bitMatrix: BitMatrix = writer.encode(code, formatToUse, width, height)
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE)
                }
            }
            bitmap
        } catch (e: Exception) {
            null
        }
    }
}
