package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.MainViewModel
import com.example.ui.components.FrostedBottomBar
import com.example.ui.components.FrostedHeader
import com.example.ui.components.NavTab
import com.example.ui.ledger.LedgerScreen
import com.example.ui.lending.LendingScreen
import com.example.ui.notes.NotesScreen
import com.example.ui.theme.LocalAppExtendedColors
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: MainViewModel = viewModel()

            MyApplicationTheme {
                UnspentApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun UnspentApp(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    var currentTab by remember { mutableStateOf(NavTab.LENDING) }
    val extendedColors = LocalAppExtendedColors.current

    // If on Ledger or Notes tab, pressing back smoothly returns to primary Lent/Borrow tab
    BackHandler(enabled = currentTab != NavTab.LENDING) {
        currentTab = NavTab.LENDING
    }

    // Balance Ledger State
    val currentBalance by viewModel.currentBalance.collectAsStateWithLifecycle()
    val initialBalance by viewModel.initialBalance.collectAsStateWithLifecycle()
    val ledgerEntries by viewModel.ledgerEntries.collectAsStateWithLifecycle()

    // Lending Tracker State
    val personSummaries by viewModel.personSummaries.collectAsStateWithLifecycle()
    val allLendingEntries by viewModel.lendingEntries.collectAsStateWithLifecycle()

    // Simple Notes State
    val notes by viewModel.notes.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            FrostedBottomBar(
                currentTab = currentTab,
                onTabSelected = { currentTab = it }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            extendedColors.elevatedSurface.copy(alpha = 0.4f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
                .statusBarsPadding()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                FrostedHeader(
                    currentTab = currentTab
                )


                Crossfade(
                    targetState = currentTab,
                    label = "tab_crossfade",
                    modifier = Modifier.weight(1f)
                ) { tab ->
                    when (tab) {
                        NavTab.LEDGER -> LedgerScreen(
                            currentBalance = currentBalance,
                            initialBalance = initialBalance,
                            entries = ledgerEntries,
                            onAddEntry = { amount, isIncome, label ->
                                viewModel.addLedgerEntry(amount, isIncome, label)
                            },
                            onSetInitialBalance = { amount ->
                                viewModel.setInitialBalance(amount)
                            },
                            onDeleteEntry = { id ->
                                viewModel.deleteLedgerEntry(id)
                            }
                        )

                        NavTab.LENDING -> LendingScreen(
                            summaries = personSummaries,
                            allEntries = allLendingEntries,
                            onAddEntry = { personName, amount, isLentToThem, note ->
                                viewModel.addLendingEntry(personName, amount, isLentToThem, note)
                            },
                            onAddEntryForPerson = { personId, amount, isLentToThem, note ->
                                viewModel.addLendingEntryForPerson(personId, amount, isLentToThem, note)
                            },
                            onSettleEntry = { entry, settleAmount ->
                                viewModel.settleLendingEntry(entry, settleAmount)
                            },
                            onDeleteEntry = { id ->
                                viewModel.deleteLendingEntry(id)
                            },
                            onDeletePerson = { personId, personName ->
                                viewModel.deletePerson(personId, personName)
                            }
                        )

                        NavTab.NOTES -> NotesScreen(
                            notes = notes,
                            searchQuery = searchQuery,
                            onSearchQueryChange = { viewModel.setSearchQuery(it) },
                            onSaveNote = { id, title, content, tag ->
                                viewModel.saveNote(id, title, content, tag)
                            },
                            onDeleteNote = { note ->
                                viewModel.deleteNote(note)
                            }
                        )
                    }
                }
            }
        }
    }
}
