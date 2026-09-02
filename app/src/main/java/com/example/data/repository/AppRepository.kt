package com.example.data.repository

import android.content.Context
import com.example.data.db.AppDatabase
import com.example.data.model.AppSettingEntity
import com.example.data.model.LedgerEntry
import com.example.data.model.LendingEntry
import com.example.data.model.NoteEntity
import com.example.data.model.PersonEntity
import com.example.util.stripMarkdown
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppRepository(private val context: Context) {
    private val database = AppDatabase.getDatabase(context)
    private val ledgerDao = database.ledgerDao()
    private val lendingDao = database.lendingDao()
    private val noteDao = database.noteDao()
    private val settingDao = database.appSettingDao()
    private val personDao = database.personDao()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 1. One-time migration for People:
                // Group existing lending entries by trimmed, case-insensitive name match,
                // create one Person per unique name, and re-point existing entries at that personId.
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

                // 2. One-time cleanup pass on existing notes already in the database:
                val existingNotes = noteDao.getAllNotes().firstOrNull() ?: emptyList()
                for (note in existingNotes) {
                    val cleanTitle = stripMarkdown(note.title).ifBlank { note.title }
                    val cleanContent = stripMarkdown(note.content)
                    if (cleanTitle != note.title || cleanContent != note.content) {
                        noteDao.updateNote(
                            note.copy(
                                title = cleanTitle,
                                content = cleanContent
                            )
                        )
                    }
                }
            } catch (_: Exception) {}
        }
    }

    // --- Module 1: Balance Ledger ---
    val allLedgerEntries: Flow<List<LedgerEntry>> = ledgerDao.getAllEntries()

    val initialBalance: Flow<Double> = settingDao.getSetting("initial_balance").map {
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
    val allPeople: Flow<List<PersonEntity>> = personDao.getAllPeople()
    val allLendingEntries: Flow<List<LendingEntry>> = lendingDao.getAllEntries()

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

    // --- Module 3: Plain Text Notes (Database-Only) ---
    val allNotes: Flow<List<NoteEntity>> = noteDao.getAllNotes()

    fun searchNotes(query: String): Flow<List<NoteEntity>> =
        if (query.isBlank()) noteDao.getAllNotes() else noteDao.searchNotes(query.trim())

    suspend fun saveNote(id: Long = 0, title: String, content: String, tag: String = ""): Long = withContext(Dispatchers.IO) {
        val safeTitle = if (title.isBlank()) "Untitled Note" else stripMarkdown(title.trim())
        val plainContent = stripMarkdown(content)
        val currentTime = System.currentTimeMillis()

        val existing = if (id != 0L) noteDao.getNoteById(id) else null

        val noteEntity = NoteEntity(
            id = id,
            title = safeTitle,
            content = plainContent,
            tag = tag.trim(),
            createdAt = existing?.createdAt ?: currentTime,
            modifiedAt = currentTime
        )

        noteDao.insertNote(noteEntity)
    }

    suspend fun deleteNote(note: NoteEntity) = withContext(Dispatchers.IO) {
        noteDao.deleteNote(note)
    }
}


