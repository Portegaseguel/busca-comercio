package cl.unab.busca_comercio

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EmailValidatorTest {

    fun isValidEmail(email: String): Boolean {
        return email.contains("@") && email.contains(".")
    }

    @Test
    fun validEmail_returnsTrue() {
        val result = isValidEmail("paulina@gmail.com")
        assertTrue(result)
    }

    @Test
    fun invalidEmail_returnsFalse() {
        val result = isValidEmail("correo_invalido")
        assertFalse(result)
    }
}
