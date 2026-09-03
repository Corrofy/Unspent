package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ledger_entries")
data class LedgerEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val isIncome: Boolean, // true = income (+), false = spend (-)
    val label: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
