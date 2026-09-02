package com.example.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.abs

/**
 * Centralized currency formatting utility for the Unspent app.
 *
 * Rules:
 * - Symbol: "Rs. " (capital R, lowercase s, period, then a space)
 * - Negative format: "-Rs. 200" or "-Rs. 200.00"
 * - Positive explicit signed format: "+Rs. 200.00"
 * - Standard positive format: "Rs. 200.00" or "Rs. 200"
 */
object CurrencyFormatter {
    private val decimalSymbols = DecimalFormatSymbols(Locale.US).apply {
        groupingSeparator = ','
        decimalSeparator = '.'
    }

    private val decimalFormat = DecimalFormat("#,##0.00", decimalSymbols)
    private val integerFormat = DecimalFormat("#,##0", decimalSymbols)

    fun format(
        amount: Double,
        includeDecimals: Boolean = true,
        explicitSign: Boolean = false
    ): String {
        val isNegative = amount < -0.0001
        val isPositive = amount > 0.0001
        val absVal = abs(amount)
        val formattedNumber = if (includeDecimals) {
            decimalFormat.format(absVal)
        } else {
            integerFormat.format(absVal)
        }

        return when {
            isNegative -> "-Rs. $formattedNumber"
            explicitSign && isPositive -> "+Rs. $formattedNumber"
            else -> "Rs. $formattedNumber"
        }
    }
}

/**
 * Global helper function to format currency throughout the app.
 */
fun formatCurrency(
    amount: Double,
    includeDecimals: Boolean = true,
    explicitSign: Boolean = false
): String = CurrencyFormatter.format(amount, includeDecimals, explicitSign)
