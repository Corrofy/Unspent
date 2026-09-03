package com.example.ui.ledger

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LedgerEntry
import com.example.ui.components.FrostedCard
import com.example.ui.components.FrostedItemCard
import com.example.ui.components.FrostedPillButton
import com.example.ui.components.SnappyBottomSheetDialog
import com.example.ui.theme.LocalAppExtendedColors
import com.example.util.formatCurrency
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LedgerScreen(
    currentBalance: Double,
    initialBalance: Double,
    entries: List<LedgerEntry>,
    onAddEntry: (amount: Double, isIncome: Boolean, label: String) -> Unit,
    onSetInitialBalance: (amount: Double) -> Unit,
    onDeleteEntry: (id: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val extendedColors = LocalAppExtendedColors.current
    var showAddSheet by remember { mutableStateOf(false) }
    var showBaseBalanceDialog by remember { mutableStateOf(false) }
    var entryToDelete by remember { mutableStateOf<LedgerEntry?>(null) }

    val dateFormat = remember {
        SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Frosted Balance Card
        item {
            FrostedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("hero_balance_card")
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "CURRENT BALANCE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = extendedColors.mutedText,
                                letterSpacing = 1.8.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { showBaseBalanceDialog = true }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = "Edit Base Balance",
                                tint = extendedColors.mutedText,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Base: ${formatCurrency(initialBalance, includeDecimals = false)}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = extendedColors.mutedText,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = formatCurrency(currentBalance),
                        style = MaterialTheme.typography.displayLarge.copy(
                            color = if (currentBalance >= 0) MaterialTheme.colorScheme.onSurface else extendedColors.redSpend,
                            fontWeight = FontWeight.Black
                        ),
                        modifier = Modifier.testTag("current_balance_text")
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FrostedPillButton(
                            text = "+ ADD ENTRY",
                            onClick = { showAddSheet = true },
                            isPrimary = true,
                            modifier = Modifier.weight(1f),
                            testTag = "add_entry_button"
                        )
                        FrostedPillButton(
                            text = "SET BASE",
                            onClick = { showBaseBalanceDialog = true },
                            isPrimary = false,
                            modifier = Modifier.weight(1f),
                            testTag = "set_base_button"
                        )
                    }
                }
            }
        }

        // Section Title
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RECENT ACTIVITY",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = extendedColors.mutedText,
                        letterSpacing = 1.4.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "${entries.size} entries",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }

        // Empty State
        if (entries.isEmpty()) {
            item {
                FrostedItemCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ReceiptLong,
                            contentDescription = null,
                            tint = extendedColors.mutedText,
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No spending or income logged yet",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Medium
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap + ADD ENTRY to record your first transaction",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = extendedColors.mutedText
                            )
                        )
                    }
                }
            }
        } else {
            items(entries, key = { it.id }) { entry ->
                FrostedItemCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("ledger_item_${entry.id}"),
                    onClick = { entryToDelete = entry }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Icon Badge
                            val badgeBg = if (entry.isIncome) extendedColors.greenIncome.copy(alpha = 0.18f) else extendedColors.redSpend.copy(alpha = 0.18f)
                            val badgeTint = if (entry.isIncome) extendedColors.greenIncome else extendedColors.redSpend

                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(badgeBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (entry.isIncome) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward,
                                    contentDescription = if (entry.isIncome) "Income" else "Spend",
                                    tint = badgeTint,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = entry.label.ifBlank { if (entry.isIncome) "Income" else "Expense" },
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                                Text(
                                    text = dateFormat.format(Date(entry.timestamp)),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = extendedColors.mutedText
                                    )
                                )
                            }
                        }

                        // Formatted amount
                        val amountColor = if (entry.isIncome) extendedColors.greenIncome else extendedColors.redSpend

                        Text(
                            text = formatCurrency(if (entry.isIncome) entry.amount else -entry.amount, explicitSign = true),
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = amountColor,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Modal Bottom Sheet: Fast 2-Tap Add Entry
    if (showAddSheet) {
        AddLedgerEntrySheet(
            onDismiss = { showAddSheet = false },
            onAdd = { amount, isIncome, label ->
                onAddEntry(amount, isIncome, label)
                showAddSheet = false
            }
        )
    }

    // Dialog: Set Initial Base Balance
    if (showBaseBalanceDialog) {
        var baseInput by remember { mutableStateOf(initialBalance.toInt().toString()) }

        AlertDialog(
            onDismissRequest = { showBaseBalanceDialog = false },
            title = {
                Text(
                    text = "Starting Balance",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Enter your starting wallet/account balance (e.g. Rs. 1,000).",
                        style = MaterialTheme.typography.bodyMedium.copy(color = extendedColors.mutedText)
                    )
                    OutlinedTextField(
                        value = baseInput,
                        onValueChange = { input ->
                            if (input.count { it == '.' } <= 1) {
                                baseInput = input.filter { it.isDigit() || it == '.' }
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = extendedColors.glassBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("base_balance_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = baseInput.toDoubleOrNull() ?: 0.0
                        onSetInitialBalance(amount)
                        showBaseBalanceDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Save", color = MaterialTheme.colorScheme.onPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBaseBalanceDialog = false }) {
                    Text("Cancel", color = extendedColors.mutedText)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    // Dialog: Delete Entry Confirmation
    entryToDelete?.let { entry ->
        AlertDialog(
            onDismissRequest = { entryToDelete = null },
            title = { Text("Delete Entry", fontWeight = FontWeight.Bold) },
            text = {
                Text("Delete \"${entry.label.ifBlank { "Entry" }}\" (${formatCurrency(entry.amount)})?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteEntry(entry.id)
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLedgerEntrySheet(
    onDismiss: () -> Unit,
    onAdd: (amount: Double, isIncome: Boolean, label: String) -> Unit
) {
    val extendedColors = LocalAppExtendedColors.current
    var amountText by remember { mutableStateOf("") }
    var labelText by remember { mutableStateOf("") }

    SnappyBottomSheetDialog(
        onDismissRequest = onDismiss
    ) { dismissWithAnimation ->
        val focusRequester = remember { FocusRequester() }
        val keyboardController = LocalSoftwareKeyboardController.current

        LaunchedEffect(focusRequester) {
            delay(280)
            try {
                focusRequester.requestFocus()
                keyboardController?.show()
            } catch (_: Exception) {}
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "LOG TRANSACTION",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = extendedColors.mutedText,
                    letterSpacing = 1.5.sp,
                    fontWeight = FontWeight.Bold
                )
            )

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
                    .focusRequester(focusRequester)
                    .testTag("entry_amount_input")
            )

            // Optional Label Input
            OutlinedTextField(
                value = labelText,
                onValueChange = { labelText = it },
                label = { Text("Note / Label (Optional)") },
                placeholder = { Text("e.g. Grocery, Lunch, Salary") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = extendedColors.glassBorder
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("entry_label_input")
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Action Buttons: Spend (-) and Income (+)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Spend Button
                Button(
                    onClick = {
                        val amount = amountText.toDoubleOrNull()
                        if (amount != null && amount > 0) {
                            onAdd(amount, false, labelText)
                        }
                    },
                    enabled = (amountText.toDoubleOrNull() ?: 0.0) > 0,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("submit_spend_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = extendedColors.redSpend,
                        contentColor = Color.White
                    )
                ) {
                    Icon(imageVector = Icons.Filled.ArrowDownward, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "SPEND (-)",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }

                // Income Button
                Button(
                    onClick = {
                        val amount = amountText.toDoubleOrNull()
                        if (amount != null && amount > 0) {
                            onAdd(amount, true, labelText)
                        }
                    },
                    enabled = (amountText.toDoubleOrNull() ?: 0.0) > 0,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("submit_income_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = extendedColors.greenIncome,
                        contentColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Icon(imageVector = Icons.Filled.ArrowUpward, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "INCOME (+)",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}
