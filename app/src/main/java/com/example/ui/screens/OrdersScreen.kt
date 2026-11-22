package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.OrderEntity
import com.example.data.local.UserEntity
import com.example.data.model.OrderStatus
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.OrderStatusChip
import com.example.ui.components.ScreenshotPreviewDialog
import com.example.ui.components.formatDateTime
import com.example.ui.components.formatPkr
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.StoreBackground
import com.example.ui.theme.StoreOutline
import com.example.ui.theme.StoreSurface
import com.example.ui.theme.StoreSurfaceElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun OrdersScreen(
    currentUser: UserEntity?,
    orders: List<OrderEntity>,
    onBrowsePackages: () -> Unit,
    onOpenLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf<OrderStatus?>(null) }
    var previewScreenshotPath by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(StoreBackground)
            .statusBarsPadding(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 760.dp),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "MY ORDERS",
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextPrimary
                    )
                    Text(
                        text = "Track your diamond top-up orders and payment verification status.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }

            if (currentUser == null) {
                item {
                    EmptyStateCard(
                        icon = Icons.Filled.Lock,
                        title = "Sign In to View Orders",
                        subtitle = "Your diamond orders and payment receipts are private to your account.",
                        actionLabel = "Login / Register",
                        onAction = onOpenLogin
                    )
                }
                return@LazyColumn
            }

            // Status filter chips
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedFilter == null,
                        onClick = { selectedFilter = null },
                        label = { Text("All (${orders.size})") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NeonBlue.copy(alpha = 0.22f),
                            selectedLabelColor = NeonCyan
                        )
                    )
                    OrderStatus.entries.forEach { status ->
                        val count = orders.count { it.status == status }
                        FilterChip(
                            selected = selectedFilter == status,
                            onClick = { selectedFilter = if (selectedFilter == status) null else status },
                            label = { Text("${status.label} ($count)") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NeonBlue.copy(alpha = 0.22f),
                                selectedLabelColor = NeonCyan
                            )
                        )
                    }
                }
            }

            val filtered = if (selectedFilter == null) orders else orders.filter { it.status == selectedFilter }

            if (filtered.isEmpty()) {
                item {
                    EmptyStateCard(
                        icon = Icons.AutoMirrored.Filled.ReceiptLong,
                        title = if (orders.isEmpty()) "No Orders Yet" else "No Matching Orders",
                        subtitle = if (orders.isEmpty()) {
                            "Choose a diamond package and submit your JazzCash or EasyPaisa payment receipt to place your first order."
                        } else {
                            "Try clearing the status filter above."
                        },
                        actionLabel = if (orders.isEmpty()) "Browse Packages" else null,
                        onAction = if (orders.isEmpty()) onBrowsePackages else null
                    )
                }
            } else {
                items(filtered, key = { it.id }) { order ->
                    OrderHistoryCard(
                        order = order,
                        onViewScreenshot = { previewScreenshotPath = order.screenshotPath }
                    )
                }
            }
        }
    }

    previewScreenshotPath?.let { path ->
        ScreenshotPreviewDialog(
            screenshotPath = path,
            title = "Payment Screenshot",
            onDismiss = { previewScreenshotPath = null }
        )
    }
}

@Composable
private fun OrderHistoryCard(
    order: OrderEntity,
    onViewScreenshot: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = StoreSurface),
        border = BorderStroke(1.dp, StoreOutline),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = order.id,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold
                        ),
                        color = NeonCyan
                    )
                    Text(
                        text = formatDateTime(order.createdAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                OrderStatusChip(status = order.status)
            }

            HorizontalDivider(color = StoreOutline)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "${order.diamonds} Diamonds",
                        style = MaterialTheme.typography.headlineSmall,
                        color = TextPrimary
                    )
                    Text(
                        text = "Player UID: ${order.playerUid}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontFamily = JetBrainsMonoFontFamily),
                        color = TextSecondary
                    )
                    Text(
                        text = "${order.paymentMethod.displayName} • TID: ${order.transactionId}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                Text(
                    text = formatPkr(order.totalPricePkr),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.Bold
                    ),
                    color = GoldAccent
                )
            }

            if (order.adminNote.isNotBlank()) {
                Text(
                    text = "Admin Note: ${order.adminNote}",
                    style = MaterialTheme.typography.bodySmall,
                    color = GoldAccent,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(StoreSurfaceElevated, RoundedCornerShape(10.dp))
                        .padding(10.dp)
                )
            }

            OutlinedButton(
                onClick = onViewScreenshot,
                border = BorderStroke(1.dp, NeonBlue.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.Image, contentDescription = null, modifier = Modifier.size(16.dp), tint = NeonCyan)
                Spacer(Modifier.width(6.dp))
                Text("View Payment Screenshot", color = Color.White)
            }
        }
    }
}
