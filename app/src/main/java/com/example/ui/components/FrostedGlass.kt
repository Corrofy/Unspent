package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.SyncAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalAppExtendedColors
import kotlinx.coroutines.launch

enum class NavTab(val title: String, val activeIcon: ImageVector, val inactiveIcon: ImageVector) {
    LENDING("Lent / Borrow", Icons.Filled.SyncAlt, Icons.Outlined.SyncAlt),
    LEDGER("Ledger", Icons.Filled.AccountBalanceWallet, Icons.Outlined.AccountBalanceWallet)
}

/**
 * Frosted Glass Container with subtle border and crisp elevation.
 */
@Composable
fun FrostedCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    borderColor: Color = LocalAppExtendedColors.current.glassBorder.copy(alpha = 0.35f),
    glowColor: Color = LocalAppExtendedColors.current.glassGlow,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier,
        shape = shape,
        color = backgroundColor,
        tonalElevation = 3.dp,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Box(modifier = Modifier.padding(20.dp)) {
            content()
        }
    }
}

/**
 * Frosted Glass Item Card (for list rows)
 */
@Composable
fun FrostedItemCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    shape: Shape = RoundedCornerShape(20.dp),
    backgroundColor: Color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
    borderColor: Color = LocalAppExtendedColors.current.glassBorder.copy(alpha = 0.25f),
    content: @Composable () -> Unit
) {
    val clickableModifier = if (onClick != null) {
        Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = ripple(),
            onClick = onClick
        )
    } else Modifier

    Surface(
        modifier = modifier
            .then(clickableModifier),
        shape = shape,
        color = backgroundColor,
        tonalElevation = 1.dp,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            content()
        }
    }
}

/**
 * Unspent Brand Mark:
 * A minimal circular mark that reads as both the letter U and a coin with a 60-degree
 * bite taken out of the top-right (representing spent money vs the unspent arc).
 */
@Composable
fun UnspentLogo(
    modifier: Modifier = Modifier,
    size: Dp = 38.dp,
    showGlow: Boolean = true
) {
    val ringColor = MaterialTheme.colorScheme.primary
    val glowColor = LocalAppExtendedColors.current.glassGlow

    Canvas(modifier = modifier.size(size)) {
        val canvasWidth = this.size.width
        val canvasHeight = this.size.height
        val strokeW = canvasWidth * 0.16f
        val radius = (canvasWidth - strokeW) / 2f

        // Sweeps 300 degrees clockwise starting at 0 degrees, leaving a 60 degree bite at the top-right
        val startAngle = 0f
        val sweepAngle = 300f

        if (showGlow) {
            drawArc(
                color = glowColor.copy(alpha = 0.35f),
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(strokeW / 2f, strokeW / 2f),
                size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                style = Stroke(width = strokeW * 1.8f, cap = StrokeCap.Round)
            )
        }

        drawArc(
            color = ringColor,
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = false,
            topLeft = Offset(strokeW / 2f, strokeW / 2f),
            size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
            style = Stroke(width = strokeW, cap = StrokeCap.Round)
        )
    }
}

/**
 * Frosted Header Bar
 */
@Composable
fun FrostedHeader(
    currentTab: NavTab,
    modifier: Modifier = Modifier
) {
    val extendedColors = LocalAppExtendedColors.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // App Branding Icon Box
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(extendedColors.elevatedSurface)
                    .border(1.dp, extendedColors.glassBorder.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                UnspentLogo(size = 24.dp)
            }

            Column {
                Text(
                    text = "Unspent",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
                Text(
                    text = currentTab.title,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = extendedColors.mutedText,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }
    }
}


/**
 * Frosted Bottom Navigation Bar
 */
@Composable
fun FrostedBottomBar(
    currentTab: NavTab,
    onTabSelected: (NavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val extendedColors = LocalAppExtendedColors.current

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        tonalElevation = 8.dp,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            extendedColors.glassBorder.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavTab.entries.forEach { tab ->
                val isSelected = tab == currentTab
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(),
                            onClick = { onTabSelected(tab) }
                        )
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .testTag("nav_tab_${tab.name.lowercase()}"),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(width = 54.dp, height = 30.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isSelected) tab.activeIcon else tab.inactiveIcon,
                            contentDescription = tab.title,
                            tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else extendedColors.mutedText,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = tab.title,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.onBackground else extendedColors.mutedText
                        )
                    )
                }
            }
        }
    }
}

/**
 * Primary Pill Action Button
 */
@Composable
fun FrostedPillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPrimary: Boolean = true,
    icon: ImageVector? = null,
    testTag: String = ""
) {
    val extendedColors = LocalAppExtendedColors.current
    val containerColor = if (isPrimary) MaterialTheme.colorScheme.primary else extendedColors.elevatedSurface
    val contentColor = if (isPrimary) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface

    Button(
        onClick = onClick,
        modifier = modifier
            .height(48.dp)
            .then(if (testTag.isNotEmpty()) Modifier.testTag(testTag) else Modifier),
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp, pressedElevation = 6.dp),
        contentPadding = PaddingValues(horizontal = 20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = contentColor
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            )
        }
    }
}

/**
 * Snappy Bottom Sheet:
 * Built on official Material 3 ModalBottomSheet with skipPartiallyExpanded = true so it
 * never stops at mid-screen, handles keyboard/IME insets automatically without hiding behind the keyboard,
 * and animates smoothly.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SnappyBottomSheetDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    containerColor: Color = MaterialTheme.colorScheme.surface,
    content: @Composable (dismissWithAnimation: () -> Unit) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    val scope = rememberCoroutineScope()

    val dismissWithAnimation: () -> Unit = {
        scope.launch {
            try {
                sheetState.hide()
            } catch (_: Exception) {}
            onDismissRequest()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        shape = shape,
        containerColor = containerColor,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
            )
        },
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
        ) {
            content(dismissWithAnimation)
        }
    }
}
