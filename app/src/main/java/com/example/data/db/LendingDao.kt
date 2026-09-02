package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.LendingEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface LendingDao {
    @Query("SELECT * FROM lending_entries ORDER BY date DESC")
    fun getAllEntries(): Flow<List<LendingEntry>>

    @Query("SELECT * FROM lending_entries WHERE personId = :personId ORDER BY date DESC")
    fun getEntriesForPersonId(personId: Long): Flow<List<LendingEntry>>

    @Query("SELECT * FROM lending_entries WHERE personName = :personName ORDER BY date DESC")
    fun getEntriesForPerson(personName: String): Flow<List<LendingEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: LendingEntry): Long

    @Update
    suspend fun updateEntry(entry: LendingEntry)

    @Delete
    suspend fun deleteEntry(entry: LendingEntry)

    @Query("DELETE FROM lending_entries WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM lending_entries WHERE personId = :personId")
    suspend fun deleteByPersonId(personId: Long)

    @Query("DELETE FROM lending_entries WHERE LOWER(TRIM(personName)) = LOWER(TRIM(:personName))")
    suspend fun deleteByPersonName(personName: String)
}

