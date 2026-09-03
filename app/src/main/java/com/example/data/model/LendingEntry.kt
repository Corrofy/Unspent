package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lending_entries")
data class LendingEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val personId: Long = 0,
    val personName: String = "",
    val amount: Double,
    val isLentToThem: Boolean, // true = I lent them (they owe me), false = They lent me (I owe them)
    val date: Long = System.currentTimeMillis(),
    val note: String = "",
    val isSettled: Boolean = false,
    val settledTimestamp: Long? = null,
    val settledAmount: Double = 0.0
)

