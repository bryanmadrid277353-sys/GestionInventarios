package com.example.gestioninventarios.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Ean13ValidatorTest {

    @Test
    fun isValidEan13_codigoValido_retornaTrue() {
        // Códigos EAN-13 válidos con su dígito verificador correcto
        assertTrue(Ean13Validator.isValidEan13("750100000001")) // 12 dígitos
        assertTrue(Ean13Validator.isValidEan13("4006381333931")) // 13 dígitos
    }

    @Test
    fun isValidEan13_digitoVerificadorErroneo_retornaFalse() {
        // Código de 13 dígitos con el último dígito modificado erróneamente
        assertFalse(Ean13Validator.isValidEan13("4006381333939"))
    }

    @Test
    fun isValidEan13_codigoVacio_retornaFalse() {
        assertFalse(Ean13Validator.isValidEan13(""))
        assertFalse(Ean13Validator.isValidEan13("   "))
    }
}
