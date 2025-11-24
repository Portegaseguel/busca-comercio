package cl.unab.busca_comercio

import org.junit.Assert.assertEquals
import org.junit.Test

class NameFormatterTest {

    fun formatName(raw: String): String {
        return raw.trim()
            .lowercase()
            .replaceFirstChar { it.uppercase() }
    }

    @Test
    fun formatName_correctFormat() {
        val result = formatName("  paUliNa ")
        assertEquals("Paulina", result)
    }
}
