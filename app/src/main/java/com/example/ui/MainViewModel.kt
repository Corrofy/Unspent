package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.LedgerEntry
import com.example.data.model.LendingEntry
import com.example.data.model.PersonEntity
import com.example.data.repository.AppRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PersonLendingSummary(
    val personId: Long,
    val personName: String,
    val netAmount: Double, // Positive: they owe me; Negative: I owe them; 0: settled
    val openEntriesCount: Int,
    val transactions: List<LendingEntry>
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AppRepository(application)

    // --- Module 1: Personal Balance Ledger ---
    val initialBalance: StateFlow<Double> = repository.initialBalance
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1000.0)

    val ledgerEntries: StateFlow<List<LedgerEntry>> = repository.allLedgerEntries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentBalance: StateFlow<Double> = combine(initialBalance, ledgerEntries) { initBal, entries ->
        val totalDelta = entries.fold(0.0) { acc, entry ->
            if (entry.isIncome) acc + entry.amount else acc - entry.amount
        }
        initBal + totalDelta
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1000.0)

    fun addLedgerEntry(amount: Double, isIncome: Boolean, label: String) {
        viewModelScope.launch {
            repository.addLedgerEntry(amount, isIncome, label)
        }
    }

    fun setInitialBalance(amount: Double) {
        viewModelScope.launch {
            repository.setInitialBalance(amount)
        }
    }

    fun deleteLedgerEntry(id: Long) {
        viewModelScope.launch {
            repository.deleteLedgerEntry(id)
        }
    }

    // --- Module 2: Lending Tracker & People ---
    val allPeople: StateFlow<List<PersonEntity>> = repository.allPeople
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lendingEntries: StateFlow<List<LendingEntry>> = repository.allLendingEntries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val personSummaries: StateFlow<List<PersonLendingSummary>> = combine(allPeople, lendingEntries) { people, entries ->
        val peopleMap = people.associateBy { it.id }
        val peopleByName = people.associateBy { it.name.trim().lowercase() }

        val entriesByPerson = mutableMapOf<Long, MutableList<LendingEntry>>()
        val unassignedEntries = mutableListOf<LendingEntry>()

        for (entry in entries) {
            if (entry.personId != 0L && peopleMap.containsKey(entry.personId)) {
                entriesByPerson.getOrPut(entry.personId) { mutableListOf() }.add(entry)
            } else {
                val matchedPerson = peopleByName[entry.personName.trim().lowercase()]
                if (matchedPerson != null) {
                    entriesByPerson.getOrPut(matchedPerson.id) { mutableListOf() }.add(entry)
                } else {
                    unassignedEntries.add(entry)
                }
            }
        }

        val summaries = mutableListOf<PersonLendingSummary>()

        people.forEach { person ->
            val personEntries = entriesByPerson[person.id] ?: emptyList()
            var net = 0.0
            var openCount = 0
            personEntries.forEach { entry ->
                val outstanding = (entry.amount - entry.settledAmount).coerceAtLeast(0.0)
                if (!entry.isSettled && outstanding > 0.001) {
                    openCount++
                    if (entry.isLentToThem) {
                        net += outstanding
                    } else {
                        net -= outstanding
                    }
                }
            }
            summaries.add(
                PersonLendingSummary(
                    personId = person.id,
                    personName = person.name,
                    netAmount = net,
                    openEntriesCount = openCount,
                    transactions = personEntries.sortedByDescending { it.date }
                )
            )
        }

        if (unassignedEntries.isNotEmpty()) {
            unassignedEntries.groupBy { it.personName.trim() }.forEach { (name, pEntries) ->
                if (name.isNotBlank()) {
                    var net = 0.0
                    var openCount = 0
                    pEntries.forEach { entry ->
                        val outstanding = (entry.amount - entry.settledAmount).coerceAtLeast(0.0)
                        if (!entry.isSettled && outstanding > 0.001) {
                            openCount++
                            if (entry.isLentToThem) {
                                net += outstanding
                            } else {
                                net -= outstanding
                            }
                        }
                    }
                    summaries.add(
                        PersonLendingSummary(
                            personId = 0L,
                            personName = name,
                            netAmount = net,
                            openEntriesCount = openCount,
                            transactions = pEntries.sortedByDescending { it.date }
                        )
                    )
                }
            }
        }

        summaries.sortedByDescending { kotlin.math.abs(it.netAmount) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addLendingEntryForPerson(personId: Long, amount: Double, isLentToThem: Boolean, note: String) {
        viewModelScope.launch {
            repository.addLendingEntryForPerson(personId, amount, isLentToThem, note)
        }
    }

    fun addLendingEntry(personName: String, amount: Double, isLentToThem: Boolean, note: String) {
        viewModelScope.launch {
            repository.addLendingEntry(personName, amount, isLentToThem, note)
        }
    }

    fun settleLendingEntry(entry: LendingEntry, settleAmount: Double? = null) {
        viewModelScope.launch {
            repository.settleLendingEntry(entry, settleAmount)
        }
    }

    fun deleteLendingEntry(id: Long) {
        viewModelScope.launch {
            repository.deleteLendingEntry(id)
        }
    }

    fun deletePerson(personId: Long, personName: String = "") {
        viewModelScope.launch {
            repository.deletePerson(personId, personName)
        }
    }
}


