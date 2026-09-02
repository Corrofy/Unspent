package com.example.ui.lending

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LendingEntry
import com.example.ui.PersonLendingSummary
import com.example.ui.components.FrostedCard
import com.example.ui.components.FrostedItemCard
import com.example.ui.components.FrostedPillButton
import com.example.ui.theme.LocalAppExtendedColors
import com.example.util.formatCurrency
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LendingScreen(
    summaries: List<PersonLendingSummary>,
    allEntries: List<LendingEntry>,
    onAddEntry: (personName: String, amount: Double, isLentToThem: Boolean, note: String) -> Unit,
    onAddEntryForPerson: (personId: Long, amount: Double, isLentToThem: Boolean, note: String) -> Unit = { _, _, _, _ -> },
    onSettleEntry: (entry: LendingEntry, settleAmount: Double?) -> Unit,
    onDeleteEntry: (id: Long) -> Unit,
    onDeletePerson: (personId: Long, personName: String) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val extendedColors = LocalAppExtendedColors.current
    var showNewPersonSheet by remember { mutableStateOf(false) }
    var quickAddPersonSummary by remember { mutableStateOf<PersonLendingSummary?>(null) }
    var selectedLedgerPersonSummary by remember { mutableStateOf<PersonLendingSummary?>(null) }

    // Net Owed to Me vs I Owe
    val totalOwedToMe = summaries.filter { it.netAmount > 0 }.sumOf { it.netAmount }
    val totalIOwe = summaries.filter { it.netAmount < 0 }.sumOf { abs(it.netAmount) }
    val netOverall = totalOwedToMe - totalIOwe

    val dateFormat = remember {
        SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Net Lending Card
        item {
            FrostedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("hero_lending_card")
            ) {
                Column {
                    Text(
                        text = "NET LENDING POSITION",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = extendedColors.mutedText,
                            letterSpacing = 1.8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            val netColor = when {
                                netOverall > 0.001 -> extendedColors.greenIncome
                                netOverall < -0.001 -> extendedColors.redSpend
                                else -> MaterialTheme.colorScheme.onSurface
                            }
                            Text(
                                text = formatCurrency(abs(netOverall)),
                                style = MaterialTheme.typography.displayLarge.copy(
                                    color = netColor,
                                    fontWeight = FontWeight.Black
                                ),
                                modifier = Modifier.testTag("net_lending_text")
                            )
                            Text(
                                text = when {
                                    netOverall > 0.001 -> "People owe you overall"
                                    netOverall < -0.001 -> "You owe people overall"
                                    else -> "All accounts balanced"
                                },
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = extendedColors.mutedText
                                )
                            )
                        }

                        // Summary sub-stats: Green up-arrow for amount to receive (lent), Red down-arrow for amount to pay (borrowed)
                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.End
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.ArrowUpward,
                                    contentDescription = "Amount to be received",
                                    tint = extendedColors.greenIncome,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = formatCurrency(totalOwedToMe, includeDecimals = false),
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = extendedColors.greenIncome,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.End
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.ArrowDownward,
                                    contentDescription = "Amount to be paid",
                                    tint = extendedColors.redSpend,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = formatCurrency(totalIOwe, includeDecimals = false),
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = extendedColors.redSpend,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    FrostedPillButton(
                        text = "NEW PERSON",
                        onClick = { showNewPersonSheet = true },
                        isPrimary = true,
                        modifier = Modifier.fillMaxWidth(),
                        icon = Icons.Filled.PersonAdd,
                        testTag = "add_person_button"
                    )
                }
            }
        }

        // List Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PEOPLE LEDGER",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = extendedColors.mutedText,
                        letterSpacing = 1.4.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "${summaries.size} people",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }

        // Empty state or Person Cards
        if (summaries.isEmpty()) {
            item {
                FrostedItemCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.SyncAlt,
                            contentDescription = null,
                            tint = extendedColors.mutedText,
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No lending/borrowing records",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Medium
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap + NEW PERSON to add someone and track money",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = extendedColors.mutedText
                            )
                        )
                    }
                }
            }
        } else {
            items(summaries, key = { "${it.personId}_${it.personName}" }) { summary ->
                FrostedItemCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("person_item_${summary.personName}"),
                    onClick = {
                        // Frictionless quick-add on tapping person's card
                        quickAddPersonSummary = summary
                    }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            // Avatar Icon Box
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = summary.personName.take(2).uppercase(),
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }

                            Column {
                                Text(
                                    text = summary.personName,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                                Text(
                                    text = "${summary.transactions.size} records • ${summary.openEntriesCount} open",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = extendedColors.mutedText
                                    )
                                )
                            }
                        }

                        // Net Position Badge & Ledger Shortcut
                        val net = summary.netAmount
                        val (statusText, statusColor) = when {
                            net > 0.001 -> "${formatCurrency(net, includeDecimals = false)}" to extendedColors.greenIncome
                            net < -0.001 -> "${formatCurrency(abs(net), includeDecimals = false)}" to extendedColors.redSpend
                            else -> "Rs.00" to extendedColors.mutedText
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = statusText,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = statusColor,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }

                            IconButton(
                                onClick = { selectedLedgerPersonSummary = summary },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "View complete ledger",
                                    tint = extendedColors.mutedText,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Modal 1: Quick-Add Sheet for an Existing Person (Zero Name Typing, 2 Taps)
    quickAddPersonSummary?.let { personSummary ->
        QuickAddLendingSheet(
            personSummary = personSummary,
            onDismiss = { quickAddPersonSummary = null },
            onAdd = { amount, isLentToThem, note ->
                if (personSummary.personId != 0L) {
                    onAddEntryForPerson(personSummary.personId, amount, isLentToThem, note)
                } else {
                    onAddEntry(personSummary.personName, amount, isLentToThem, note)
                }
                quickAddPersonSummary = null
            },
            onViewLedger = {
                val current = quickAddPersonSummary
                quickAddPersonSummary = null
                selectedLedgerPersonSummary = current
            }
        )
    }

    // Modal 2: "+ New Person" Sheet (First-time add with duplicate protection)
    if (showNewPersonSheet) {
        AddNewPersonLendingSheet(
            existingSummaries = summaries,
            onDismiss = { showNewPersonSheet = false },
            onSelectExisting = { matchedSummary ->
                showNewPersonSheet = false
                quickAddPersonSummary = matchedSummary
            },
            onAddNew = { personName, amount, isLentToThem, note ->
                // Duplicate protection on submit: check if trimmed case-insensitive match exists
                val matched = summaries.firstOrNull { it.personName.trim().equals(personName.trim(), ignoreCase = true) }
                if (matched != null && matched.personId != 0L) {
                    onAddEntryForPerson(matched.personId, amount, isLentToThem, note)
                } else {
                    onAddEntry(personName.trim(), amount, isLentToThem, note)
                }
                showNewPersonSheet = false
            }
        )
    }

    // Modal 3: Complete Person Ledger History & Settle Sheet
    selectedLedgerPersonSummary?.let { currentSummary ->
        val personEntries = allEntries.filter {
            (currentSummary.personId != 0L && it.personId == currentSummary.personId) ||
            it.personName.equals(currentSummary.personName, ignoreCase = true)
        }.sortedByDescending { it.date }

        PersonLedgerSheet(
            personSummary = currentSummary,
            entries = personEntries,
            onDismiss = { selectedLedgerPersonSummary = null },
            onQuickAdd = {
                selectedLedgerPersonSummary = null
                quickAddPersonSummary = currentSummary
            },
            onSettle = { entry, settleAmount ->
                onSettleEntry(entry, settleAmount)
            },
            onDelete = { id ->
                onDeleteEntry(id)
            },
            onDeletePerson = { personId, personName ->
                onDeletePerson(personId, personName)
                selectedLedgerPersonSummary = null
            }
        )
    }
}

/**
 * Frictionless Quick-Add Sheet for an existing Person.
 * - Name is shown as a read-only header (never editable).
 * - Numeric keyboard & auto-focused amount.
 * - LENT (+) and BORROW (-) buttons.
 * - 2-tap transaction logging.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickAddLendingSheet(
    personSummary: PersonLendingSummary,
    onDismiss: () -> Unit,
    onAdd: (amount: Double, isLentToThem: Boolean, note: String) -> Unit,
    onViewLedger: () -> Unit
) {
    val extendedColors = LocalAppExtendedColors.current
    var amountText by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Read-Only Person Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = personSummary.personName.take(2).uppercase(),
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    Column {
                        Text(
                            text = personSummary.personName,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Text(
                            text = "Add transaction for this person",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = extendedColors.mutedText
                            )
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Close",
                        tint = extendedColors.mutedText
                    )
                }
            }

            // Amount Input with auto-focus
            OutlinedTextField(
                value = amountText,
                onValueChange = { input ->
                    if (input.count { it == '.' } <= 1) {
                        amountText = input.filter { it.isDigit() || it == '.' }
                    }
                },
                label = { Text("Amount (Rs.)") },
                placeholder = { Text("0.00") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Next
                ),
                singleLine = true,
                textStyle = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = extendedColors.glassBorder
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .testTag("quick_add_amount_input")
            )

            // Optional Note Input
            OutlinedTextField(
                value = noteText,
                onValueChange = { noteText = it },
                label = { Text("Note / Purpose (Optional)") },
                placeholder = { Text("e.g. Lunch split, Cab fare, Coffee") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = extendedColors.glassBorder
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("quick_add_note_input")
            )

            // Action Buttons: Borrow (-) and Lent (+)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        val amount = amountText.toDoubleOrNull()
                        if (amount != null && amount > 0) {
                            onAdd(amount, false, noteText)
                        }
                    },
                    enabled = (amountText.toDoubleOrNull() ?: 0.0) > 0,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("quick_add_borrow_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = extendedColors.redSpend,
                        contentColor = Color.White
                    )
                ) {
                    Icon(imageVector = Icons.Filled.ArrowDownward, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "BORROW (-)",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Button(
                    onClick = {
                        val amount = amountText.toDoubleOrNull()
                        if (amount != null && amount > 0) {
                            onAdd(amount, true, noteText)
                        }
                    },
                    enabled = (amountText.toDoubleOrNull() ?: 0.0) > 0,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("quick_add_lent_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = extendedColors.greenIncome,
                        contentColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Icon(imageVector = Icons.Filled.ArrowUpward, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "LENT (+)",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            // Quick Link to Ledger History & Settle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                TextButton(onClick = onViewLedger) {
                    Text(
                        text = "View complete history / settle transactions →",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }
        }
    }
}

/**
 * Sheet for "+ NEW PERSON":
 * - Name input for first-time entries.
 * - Real-time duplicate protection with suggestion chip.
 * - Amount + Note + LENT/BORROW.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNewPersonLendingSheet(
    existingSummaries: List<PersonLendingSummary>,
    onDismiss: () -> Unit,
    onSelectExisting: (PersonLendingSummary) -> Unit,
    onAddNew: (personName: String, amount: Double, isLentToThem: Boolean, note: String) -> Unit
) {
    val extendedColors = LocalAppExtendedColors.current
    var personName by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    // Find matching existing people as user types
    val trimmedInput = personName.trim()
    val matchingExisting = remember(trimmedInput, existingSummaries) {
        if (trimmedInput.isBlank()) emptyList()
        else existingSummaries.filter {
            it.personName.contains(trimmedInput, ignoreCase = true)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ADD NEW PERSON",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = extendedColors.mutedText,
                        letterSpacing = 1.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Close",
                        tint = extendedColors.mutedText
                    )
                }
            }

            // Person Name Input
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedTextField(
                    value = personName,
                    onValueChange = { personName = it },
                    label = { Text("Person Name") },
                    placeholder = { Text("e.g. Rahul, Alex, Sara") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = extendedColors.glassBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .testTag("lending_person_input")
                )

                // Duplicate Protection Suggestions
                if (matchingExisting.isNotEmpty()) {
                    Text(
                        text = "Already in your records (tap to switch to quick-add):",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 11.sp
                        )
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(matchingExisting, key = { it.personId }) { match ->
                            AssistChip(
                                onClick = { onSelectExisting(match) },
                                label = { Text("Add to ${match.personName}") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Filled.Person,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    labelColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }
                }
            }

            // Amount Input
            OutlinedTextField(
                value = amountText,
                onValueChange = { input ->
                    if (input.count { it == '.' } <= 1) {
                        amountText = input.filter { it.isDigit() || it == '.' }
                    }
                },
                label = { Text("Amount (Rs.)") },
                placeholder = { Text("0.00") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Next
                ),
                singleLine = true,
                textStyle = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = extendedColors.glassBorder
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("lending_amount_input")
            )

            // Optional Note Input
            OutlinedTextField(
                value = noteText,
                onValueChange = { noteText = it },
                label = { Text("Note / Purpose (Optional)") },
                placeholder = { Text("e.g. Dinner split, Concert tickets") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = extendedColors.glassBorder
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("lending_note_input")
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Action Buttons: Borrow (-) and Lent (+)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        val amount = amountText.toDoubleOrNull()
                        if (personName.isNotBlank() && amount != null && amount > 0) {
                            onAddNew(personName.trim(), amount, false, noteText)
                        }
                    },
                    enabled = personName.isNotBlank() && (amountText.toDoubleOrNull() ?: 0.0) > 0,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("submit_borrow_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = extendedColors.redSpend,
                        contentColor = Color.White
                    )
                ) {
                    Icon(imageVector = Icons.Filled.ArrowDownward, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "BORROW (-)",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Button(
                    onClick = {
                        val amount = amountText.toDoubleOrNull()
                        if (personName.isNotBlank() && amount != null && amount > 0) {
                            onAddNew(personName.trim(), amount, true, noteText)
                        }
                    },
                    enabled = personName.isNotBlank() && (amountText.toDoubleOrNull() ?: 0.0) > 0,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("submit_lent_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = extendedColors.greenIncome,
                        contentColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Icon(imageVector = Icons.Filled.ArrowUpward, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "LENT (+)",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonLedgerSheet(
    personSummary: PersonLendingSummary,
    entries: List<LendingEntry>,
    onDismiss: () -> Unit,
    onQuickAdd: () -> Unit,
    onSettle: (entry: LendingEntry, settleAmount: Double?) -> Unit,
    onDelete: (id: Long) -> Unit,
    onDeletePerson: (personId: Long, personName: String) -> Unit = { _, _ -> }
) {
    val extendedColors = LocalAppExtendedColors.current
    var entryToSettle by remember { mutableStateOf<LendingEntry?>(null) }
    var entryToDelete by remember { mutableStateOf<LendingEntry?>(null) }
    var showDeletePersonDialog by remember { mutableStateOf(false) }

    val isFullySettled = abs(personSummary.netAmount) < 0.001

    val dateFormat = remember {
        SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = personSummary.personName,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Text(
                        text = "Complete Transaction Ledger",
                        style = MaterialTheme.typography.labelSmall.copy(color = extendedColors.mutedText)
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = onQuickAdd) {
                        Text(
                            text = "+ Quick Add",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Filled.Close, contentDescription = "Close", tint = extendedColors.mutedText)
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(entries, key = { it.id }) { entry ->
                    FrostedItemCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    val (typeLabel, typeColor) = if (entry.isLentToThem) {
                                        "Lent to them" to extendedColors.greenIncome
                                    } else {
                                        "Borrowed from them" to extendedColors.redSpend
                                    }

                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(typeColor)
                                    )
                                    Text(
                                        text = typeLabel,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            color = typeColor,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }

                                Text(
                                    text = formatCurrency(entry.amount),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            }

                            if (entry.note.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = entry.note,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = dateFormat.format(Date(entry.date)),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = extendedColors.mutedText,
                                        fontSize = 10.sp
                                    )
                                )

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    if (entry.isSettled) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.CheckCircle,
                                                contentDescription = "Settled",
                                                tint = extendedColors.greenIncome,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text(
                                                text = "Rs.00",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = extendedColors.greenIncome,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            )
                                        }
                                    } else {
                                        val outstanding = entry.amount - entry.settledAmount
                                        TextButton(
                                            onClick = { entryToSettle = entry },
                                            modifier = Modifier.height(30.dp)
                                        ) {
                                            Text(
                                                text = if (entry.settledAmount > 0.001) "Settle Left (${formatCurrency(outstanding, includeDecimals = false)})" else "Mark Settled",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = MaterialTheme.colorScheme.primary,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            )
                                        }
                                    }

                                    IconButton(
                                        onClick = { entryToDelete = entry },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Delete,
                                            contentDescription = "Delete",
                                            tint = extendedColors.mutedText,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Destructive Delete Section or Explanation
            if (isFullySettled) {
                Button(
                    onClick = { showDeletePersonDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("delete_person_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = extendedColors.redSpend.copy(alpha = 0.12f),
                        contentColor = extendedColors.redSpend
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Delete ${personSummary.personName}",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Settle the remaining balance first to delete this person",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = extendedColors.mutedText
                        )
                    )
                }
            }
        }
    }

    // Settle Dialog
    entryToSettle?.let { entry ->
        val outstanding = (entry.amount - entry.settledAmount).coerceAtLeast(0.0)
        val defaultAmountStr = if (outstanding % 1.0 == 0.0) outstanding.toInt().toString() else "%.2f".format(outstanding)
        var settleAmountInput by remember { mutableStateOf(defaultAmountStr) }

        AlertDialog(
            onDismissRequest = { entryToSettle = null },
            title = { Text("Settle Transaction", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Settle remaining ${formatCurrency(outstanding)} with ${entry.personName}?",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    OutlinedTextField(
                        value = settleAmountInput,
                        onValueChange = { input ->
                            if (input.count { it == '.' } <= 1) {
                                settleAmountInput = input.filter { it.isDigit() || it == '.' }
                            }
                        },
                        label = { Text("Settled Amount (Rs.)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = extendedColors.glassBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = settleAmountInput.toDoubleOrNull() ?: outstanding
                        onSettle(entry, amount)
                        entryToSettle = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = extendedColors.greenIncome)
                ) {
                    Text("Confirm Settle", color = MaterialTheme.colorScheme.surface, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { entryToSettle = null }) {
                    Text("Cancel", color = extendedColors.mutedText)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    // Delete Dialog
    entryToDelete?.let { entry ->
        AlertDialog(
            onDismissRequest = { entryToDelete = null },
            title = { Text("Delete Record", fontWeight = FontWeight.Bold) },
            text = { Text("Delete this transaction of ${formatCurrency(entry.amount)} with ${entry.personName}?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete(entry.id)
                        entryToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = extendedColors.redSpend)
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.onError)
                }
            },
            dismissButton = {
                TextButton(onClick = { entryToDelete = null }) {
                    Text("Cancel", color = extendedColors.mutedText)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    // Delete Person Confirmation Dialog
    if (showDeletePersonDialog) {
        AlertDialog(
            onDismissRequest = { showDeletePersonDialog = false },
            title = {
                Text(
                    text = "Delete ${personSummary.personName}?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Delete ${personSummary.personName}? This removes their entire transaction history. This can't be undone.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeletePersonDialog = false
                        onDeletePerson(personSummary.personId, personSummary.personName)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = extendedColors.redSpend,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.testTag("confirm_delete_person_button")
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeletePersonDialog = false },
                    modifier = Modifier.testTag("cancel_delete_person_button")
                ) {
                    Text("Cancel", color = extendedColors.mutedText)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}
