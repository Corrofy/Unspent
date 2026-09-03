package com.example

import com.example.util.formatCurrency
import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testCurrencyFormatting() {
        assertEquals("Rs. 1,000.00", formatCurrency(1000.0))
        assertEquals("Rs. 1,000", formatCurrency(1000.0, includeDecimals = false))
        assertEquals("-Rs. 200.00", formatCurrency(-200.0))
        assertEquals("-Rs. 200", formatCurrency(-200.0, includeDecimals = false))
        assertEquals("+Rs. 500.50", formatCurrency(500.50, explicitSign = true))
        assertEquals("Rs. 0.00", formatCurrency(0.0))
    }
}

