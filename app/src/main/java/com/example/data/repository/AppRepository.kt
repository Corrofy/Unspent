package com.example.data.repository

import android.content.Context
import com.example.data.db.AppDatabase
import com.example.data.model.AppSettingEntity
import com.example.data.model.LedgerEntry
import com.example.data.model.LendingEntry
import com.example.data.model.PersonEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppRepository(context: Context) {
    private val appContext = context.applicationContext
    private val database by lazy { AppDatabase.getDatabase(appContext) }
    private val ledgerDao by lazy { database.ledgerDao() }
    private val lendingDao by lazy { database.lendingDao() }
    private val settingDao by lazy { database.appSettingDao() }
    private val personDao by lazy { database.personDao() }

    init {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val prefs = appContext.getSharedPreferences("unspent_prefs", Context.MODE_PRIVATE)
                if (!prefs.getBoolean("migration_v3_complete", false)) {
                    // One-time migration for People:
                    val entries = lendingDao.getAllEntries().firstOrNull() ?: emptyList()
                    if (entries.isNotEmpty()) {
                        val existingPeople = personDao.getAllPeople().firstOrNull() ?: emptyList()
                        val personMap = existingPeople.associateBy { it.name.trim().lowercase() }.toMutableMap()

                        for (entry in entries) {
                            val trimmedName = entry.personName.trim()
                            if (trimmedName.isNotBlank()) {
                                val lower = trimmedName.lowercase()
                                val person = personMap.getOrPut(lower) {
                                    val newId = personDao.insertPerson(PersonEntity(name = trimmedName))
                                    PersonEntity(id = newId, name = trimmedName)
                                }
                                if (entry.personId != person.id || entry.personName != person.name) {
                                    lendingDao.updateEntry(entry.copy(personId = person.id, personName = person.name))
                                }
                            }
                        }
                    }
                    prefs.edit().putBoolean("migration_v3_complete", true).apply()
                }
            } catch (_: Exception) {}
        }
    }

    // --- Module 1: Balance Ledger ---
    val allLedgerEntries: Flow<List<LedgerEntry>> get() = ledgerDao.getAllEntries()

    val initialBalance: Flow<Double> get() = settingDao.getSetting("initial_balance").map {
        it?.toDoubleOrNull() ?: 1000.0
    }

    suspend fun setInitialBalance(amount: Double) = withContext(Dispatchers.IO) {
        settingDao.setSetting(AppSettingEntity("initial_balance", amount.toString()))
    }

    suspend fun addLedgerEntry(amount: Double, isIncome: Boolean, label: String) = withContext(Dispatchers.IO) {
        val entry = LedgerEntry(
            amount = amount,
            isIncome = isIncome,
            label = label.trim(),
            timestamp = System.currentTimeMillis()
        )
        ledgerDao.insertEntry(entry)
    }

    suspend fun deleteLedgerEntry(id: Long) = withContext(Dispatchers.IO) {
        ledgerDao.deleteById(id)
    }

    // --- Module 2: Lending Tracker & People ---
    val allPeople: Flow<List<PersonEntity>> get() = personDao.getAllPeople()
    val allLendingEntries: Flow<List<LendingEntry>> get() = lendingDao.getAllEntries()

    suspend fun getOrCreatePerson(name: String): PersonEntity = withContext(Dispatchers.IO) {
        val trimmed = name.trim()
        val existing = personDao.getPersonByName(trimmed)
        if (existing != null) {
            existing
        } else {
            val newId = personDao.insertPerson(PersonEntity(name = trimmed))
            PersonEntity(id = newId, name = trimmed)
        }
    }

    fun getLendingForPersonId(personId: Long): Flow<List<LendingEntry>> =
        lendingDao.getEntriesForPersonId(personId)

    fun getLendingForPerson(personName: String): Flow<List<LendingEntry>> =
        lendingDao.getEntriesForPerson(personName)

    suspend fun addLendingEntryForPerson(
        personId: Long,
        amount: Double,
        isLentToThem: Boolean,
        note: String
    ) = withContext(Dispatchers.IO) {
        val person = personDao.getPersonById(personId)
        val personName = person?.name ?: ""
        val entry = LendingEntry(
            personId = personId,
            personName = personName,
            amount = amount,
            isLentToThem = isLentToThem,
            date = System.currentTimeMillis(),
            note = note.trim(),
            isSettled = false
        )
        lendingDao.insertEntry(entry)
    }

    suspend fun addLendingEntry(
        personName: String,
        amount: Double,
        isLentToThem: Boolean,
        note: String
    ) = withContext(Dispatchers.IO) {
        val person = getOrCreatePerson(personName)
        val entry = LendingEntry(
            personId = person.id,
            personName = person.name,
            amount = amount,
            isLentToThem = isLentToThem,
            date = System.currentTimeMillis(),
            note = note.trim(),
            isSettled = false
        )
        lendingDao.insertEntry(entry)
    }

    suspend fun settleLendingEntry(entry: LendingEntry, settleAmount: Double? = null) = withContext(Dispatchers.IO) {
        val previousSettled = entry.settledAmount
        val additionalSettled = settleAmount ?: (entry.amount - previousSettled)
        val totalSettled = (previousSettled + additionalSettled).coerceAtMost(entry.amount)
        val isFullySettled = totalSettled >= entry.amount - 0.001

        val updated = entry.copy(
            isSettled = isFullySettled,
            settledTimestamp = System.currentTimeMillis(),
            settledAmount = totalSettled
        )
        lendingDao.updateEntry(updated)
    }

    suspend fun deleteLendingEntry(id: Long) = withContext(Dispatchers.IO) {
        lendingDao.deleteById(id)
    }

    suspend fun deletePerson(personId: Long, personName: String = "") = withContext(Dispatchers.IO) {
        if (personId != 0L) {
            lendingDao.deleteByPersonId(personId)
            personDao.deleteById(personId)
        }
        if (personName.isNotBlank()) {
            lendingDao.deleteByPersonName(personName.trim())
            personDao.deleteByName(personName.trim())
        }
    }
}


