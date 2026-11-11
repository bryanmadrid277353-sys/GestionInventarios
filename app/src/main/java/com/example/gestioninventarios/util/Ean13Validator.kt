package com.example.gestioninventarios.util

object Ean13Validator {
    fun isValidEan13(code: String): Boolean {
        val trimmed = code.trim()
        if (trimmed.isEmpty()) return false
        if (trimmed.length == 13 && trimmed.all { it.isDigit() }) {
            var sum = 0
            for (i in 0 until 12) {
                val digit = trimmed[i].toString().toInt()
                sum += if (i % 2 == 0) digit else digit * 3
            }
            val checkDigit = (10 - (sum % 10)) % 10
            return checkDigit == trimmed[12].toString().toInt()
        }
        return trimmed.length in 1..50
    }
}
