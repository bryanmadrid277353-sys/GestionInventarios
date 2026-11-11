package com.example.gestioninventarios.util

import java.util.Locale

object CodigoInterno {

    /**
     * Calcula el dígito verificador EAN-13 usando los pesos 1 y 3 alternados.
     */
    fun digitoVerificador(first12Digits: String): Int {
        require(first12Digits.length == 12 && first12Digits.all { it.isDigit() }) {
            "Se requieren exactamente 12 dígitos numéricos para calcular el dígito verificador EAN-13"
        }
        var sum = 0
        for (i in 0 until 12) {
            val digit = first12Digits[i].toString().toInt()
            sum += if (i % 2 == 0) digit else digit * 3
        }
        return (10 - (sum % 10)) % 10
    }

    /**
     * Verifica si una cadena es un código EAN-13 válido (13 dígitos numéricos y dígito verificador correcto).
     */
    fun esEan13Valido(barcode: String): Boolean {
        val trimmed = barcode.trim()
        if (trimmed.length != 13 || !trimmed.all { it.isDigit() }) return false
        val first12 = trimmed.substring(0, 12)
        val expectedCheckDigit = digitoVerificador(first12)
        val actualCheckDigit = trimmed[12].toString().toInt()
        return expectedCheckDigit == actualCheckDigit
    }

    /**
     * Construye el prefijo de 5 dígitos: "2" + código categoría (2 dígitos) + código proveedor (2 dígitos)
     */
    fun prefijo(catCodigo: Int, provCodigo: Int): String {
        val catClamped = catCodigo.coerceIn(0, 99)
        val provClamped = provCodigo.coerceIn(0, 99)
        return String.format(Locale.US, "2%02d%02d", catClamped, provClamped)
    }

    /**
     * Genera un código EAN-13 completo de 13 dígitos a partir del código de categoría, proveedor y consecutivo.
     */
    fun generar(catCodigo: Int, provCodigo: Int, consecutivo: Int): String {
        val pref = prefijo(catCodigo, provCodigo)
        val consFormatted = String.format(Locale.US, "%07d", consecutivo.coerceIn(1, 9999999))
        val first12 = pref + consFormatted
        val checkDigit = digitoVerificador(first12)
        return first12 + checkDigit
    }

    /**
     * Formatea un código de barras EAN-13 interno para visualización bonita: "2 01 01 0000001 3"
     */
    fun formatear(barcode: String): String {
        val trimmed = barcode.trim()
        if (trimmed.length == 13 && trimmed.all { it.isDigit() }) {
            val p1 = trimmed.substring(0, 1)      // "2"
            val p2 = trimmed.substring(1, 3)      // cat "01"
            val p3 = trimmed.substring(3, 5)      // prov "01"
            val p4 = trimmed.substring(5, 12)     // consecutivo "0000001"
            val p5 = trimmed.substring(12, 13)    // dv "3"
            return "$p1 $p2 $p3 $p4 $p5"
        }
        return trimmed
    }
}
