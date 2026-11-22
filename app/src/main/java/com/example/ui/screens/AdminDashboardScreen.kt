package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.DiamondPackageEntity
import com.example.data.local.FreeRewardEntity
import com.example.data.local.OrderEntity
import com.example.data.local.PaymentConfigEntity
import com.example.data.local.SupportMessageEntity
import com.example.data.local.UserEntity
import com.example.data.model.OrderStatus
import com.example.data.model.PaymentMethod
import com.example.data.model.UserRole
import com.example.ui.AdminMetrics
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.OrderStatusChip
import com.example.ui.components.ScreenshotPreviewDialog
import com.example.ui.components.formatDateTime
import com.example.ui.components.formatPkr
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.StatusError
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusWarning
import com.example.ui.theme.StoreBackground
import com.example.ui.theme.StoreOutline
import com.example.ui.theme.StoreSurface
import com.example.ui.theme.StoreSurfaceElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    currentUser: UserEntity?,
    metrics: AdminMetrics,
    orders: List<OrderEntity>,
    users: List<UserEntity>,
    userSearchQuery: String,
    packages: List<DiamondPackageEntity>,
    paymentConfigs: List<PaymentConfigEntity>,
    rewards: List<FreeRewardEntity>,
    supportMessages: List<SupportMessageEntity>,
    onBack: () -> Unit,
    onUpdateUserSearch: (String) -> Unit,
    onUpdateOrderStatus: (orderId: String, status: OrderStatus, adminNote: String) -> Unit,
    onSavePackage: (packageId: Int?, diamonds: Int, pricePkr: Int, badge: String?) -> Unit,
    onDeletePackage: (packageId: Int) -> Unit,
    onUpdatePaymentConfig: (PaymentMethod, String, String, String) -> Unit,
    onSendFreeDiamonds: (targetUserId: String, diamonds: Int, reason: String, onSent: () -> Unit) -> Unit,
    onMarkRewardFulfilled: (rewardId: String) -> Unit,
    onSendNotification: (targetUserId: String?, title: String, message: String, onSent: () -> Unit) -> Unit,
    onResolveSupportMessage: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Overview", "Orders", "Give Free Diamonds", "Users & Notify", "Packages & Pay")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(StoreBackground)
    ) {
        TopAppBar(
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Filled.AdminPanelSettings, contentDescription = null, tint = GoldAccent)
                    Text(stringResource(R.string.admin_panel_title), style = MaterialTheme.typography.headlineSmall)
                }
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = StoreSurface,
                titleContentColor = GoldAccent,
                navigationIconContentColor = TextPrimary
            )
        )

        if (currentUser?.role != UserRole.ADMIN) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                EmptyStateCard(
                    icon = Icons.Filled.Lock,
                    title = "Admin Authorization Required",
                    subtitle = "Only verified Admin accounts can access the Admin Dashboard.",
                    actionLabel = "Go Back",
                    onAction = onBack
                )
            }
            return
        }

        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = StoreSurface,
            contentColor = GoldAccent,
            edgePadding = 12.dp
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            color = if (selectedTab == index) GoldAccent else TextSecondary
                        )
                    },
                    modifier = Modifier.testTag("admin_tab_$index")
                )
            }
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            when (selectedTab) {
                0 -> AdminOverviewTab(
                    metrics = metrics,
                    recentOrders = orders.take(5),
                    onJumpToOrders = { selectedTab = 1 },
                    onJumpToFreeDiamonds = { selectedTab = 2 }
                )
                1 -> AdminOrdersTab(
                    orders = orders,
                    onUpdateOrderStatus = onUpdateOrderStatus
                )
                2 -> GiveFreeDiamondsTab(
                    users = users.filter { it.role == UserRole.USER },
                    searchQuery = userSearchQuery,
                    rewards = rewards,
                    onSearchChange = onUpdateUserSearch,
                    onSendFreeDiamonds = onSendFreeDiamonds,
                    onMarkRewardFulfilled = onMarkRewardFulfilled
                )
                3 -> UsersAndNotifyTab(
                    users = users.filter { it.role == UserRole.USER },
                    searchQuery = userSearchQuery,
                    supportMessages = supportMessages,
                    onSearchChange = onUpdateUserSearch,
                    onSendNotification = onSendNotification,
                    onResolveSupportMessage = onResolveSupportMessage
                )
                4 -> PackagesAndPayTab(
                    packages = packages,
                    paymentConfigs = paymentConfigs,
                    onSavePackage = onSavePackage,
                    onDeletePackage = onDeletePackage,
                    onUpdatePaymentConfig = onUpdatePaymentConfig
                )
            }
        }
    }
}

@Composable
private fun AdminOverviewTab(
    metrics: AdminMetrics,
    recentOrders: List<OrderEntity>,
    onJumpToOrders: () -> Unit,
    onJumpToFreeDiamonds: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .widthIn(max = 840.dp),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("STORE ANALYTICS", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricCard("Total Users", metrics.totalUsers.toString(), NeonBlue, Modifier.weight(1f))
                    MetricCard("Total Orders", metrics.totalOrders.toString(), NeonCyan, Modifier.weight(1f))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricCard("Pending Orders", metrics.pendingOrders.toString(), StatusWarning, Modifier.weight(1f))
                    MetricCard("Completed", metrics.completedOrders.toString(), StatusSuccess, Modifier.weight(1f))
                }
                MetricCard("Total Confirmed Sales", formatPkr(metrics.totalSalesPkr), GoldAccent, Modifier.fillMaxWidth())
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onJumpToOrders,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonBlue, contentColor = Color(0xFF041320))
                ) {
                    Text("Verify Orders (${metrics.pendingOrders})")
                }
                Button(
                    onClick = onJumpToFreeDiamonds,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = Color(0xFF211600))
                ) {
                    Text("Give Free Diamonds")
                }
            }
        }

        item {
            Text("RECENT ORDERS", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
        }

        if (recentOrders.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.AutoMirrored.Filled.ReceiptLong,
                    title = "No Orders Yet",
                    subtitle = "Customer orders will appear here once placed."
                )
            }
        } else {
            items(recentOrders, key = { it.id }) { order ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = StoreSurface),
                    border = BorderStroke(1.dp, StoreOutline),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text("${order.id} • ${order.diamonds}💎", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                            Text("UID ${order.playerUid} • ${formatPkr(order.totalPricePkr)}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                        OrderStatusChip(status = order.status)
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricCard(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = StoreSurface),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.Bold
                ),
                color = accent
            )
        }
    }
}

@Composable
private fun AdminOrdersTab(
    orders: List<OrderEntity>,
    onUpdateOrderStatus: (orderId: String, status: OrderStatus, adminNote: String) -> Unit
) {
    var statusFilter by remember { mutableStateOf<OrderStatus?>(null) }
    var screenshotPreview by remember { mutableStateOf<String?>(null) }
    var rejectTargetOrder by remember { mutableStateOf<OrderEntity?>(null) }
    var rejectReason by remember { mutableStateOf("") }

    val filtered = if (statusFilter == null) orders else orders.filter { it.status == statusFilter }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .widthIn(max = 840.dp),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = statusFilter == null,
                    onClick = { statusFilter = null },
                    label = { Text("All (${orders.size})") }
                )
                FilterChip(
                    selected = statusFilter == OrderStatus.PENDING_PAYMENT,
                    onClick = {
                        statusFilter = if (statusFilter == OrderStatus.PENDING_PAYMENT) null else OrderStatus.PENDING_PAYMENT
                    },
                    label = { Text("Pending") }
                )
                FilterChip(
                    selected = statusFilter == OrderStatus.PAYMENT_APPROVED,
                    onClick = {
                        statusFilter = if (statusFilter == OrderStatus.PAYMENT_APPROVED) null else OrderStatus.PAYMENT_APPROVED
                    },
                    label = { Text("Approved") }
                )
                FilterChip(
                    selected = statusFilter == OrderStatus.DELIVERED,
                    onClick = {
                        statusFilter = if (statusFilter == OrderStatus.DELIVERED) null else OrderStatus.DELIVERED
                    },
                    label = { Text("Delivered") }
                )
            }
        }

        if (filtered.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.AutoMirrored.Filled.ReceiptLong,
                    title = "No Orders Found",
                    subtitle = "Orders matching this status filter will appear here."
                )
            }
        } else {
            items(filtered, key = { it.id }) { order ->
                Card(
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
                                Text(order.id, style = MaterialTheme.typography.titleMedium.copy(fontFamily = JetBrainsMonoFontFamily), color = NeonCyan)
                                Text("${order.userName} • ${formatDateTime(order.createdAt)}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            }
                            OrderStatusChip(status = order.status)
                        }

                        HorizontalDivider(color = StoreOutline)

                        Text(
                            text = "${order.diamonds} Diamonds • ${formatPkr(order.totalPricePkr)} • UID: ${order.playerUid}",
                            style = MaterialTheme.typography.titleSmall,
                            color = TextPrimary
                        )
                        Text(
                            text = "${order.paymentMethod.displayName} • TID: ${order.transactionId}",
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = JetBrainsMonoFontFamily),
                            color = GoldAccent
                        )

                        OutlinedButton(
                            onClick = { screenshotPreview = order.screenshotPath },
                            border = BorderStroke(1.dp, NeonBlue.copy(alpha = 0.5f))
                        ) {
                            Icon(Icons.Filled.Image, contentDescription = null, modifier = Modifier.size(16.dp), tint = NeonCyan)
                            Spacer(Modifier.width(6.dp))
                            Text("View Uploaded Screenshot")
                        }

                        // Action Buttons Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (order.status == OrderStatus.PENDING_PAYMENT) {
                                OutlinedButton(
                                    onClick = { onUpdateOrderStatus(order.id, OrderStatus.PAYMENT_UNDER_REVIEW, "") },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Filled.Sync, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Review")
                                }
                            }
                            if (order.status.isPending) {
                                Button(
                                    onClick = { onUpdateOrderStatus(order.id, OrderStatus.PAYMENT_APPROVED, "Payment verified") },
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonBlue, contentColor = Color(0xFF041320)),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("admin_approve_${order.id}")
                                ) {
                                    Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Approve")
                                }
                                Button(
                                    onClick = {
                                        rejectReason = ""
                                        rejectTargetOrder = order
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = StatusError, contentColor = Color.White),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Filled.Close, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Reject")
                                }
                            }
                            if (order.status == OrderStatus.PAYMENT_APPROVED) {
                                OutlinedButton(
                                    onClick = { onUpdateOrderStatus(order.id, OrderStatus.PROCESSING, "Top-up in progress") },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Processing")
                                }
                            }
                            if (order.status == OrderStatus.PAYMENT_APPROVED || order.status == OrderStatus.PROCESSING) {
                                Button(
                                    onClick = { onUpdateOrderStatus(order.id, OrderStatus.DELIVERED, "Fulfilled via authorized top-up") },
                                    colors = ButtonDefaults.buttonColors(containerColor = StatusSuccess, contentColor = Color.White),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("admin_deliver_${order.id}")
                                ) {
                                    Icon(Icons.Filled.LocalShipping, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Mark Delivered")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    screenshotPreview?.let { path ->
        ScreenshotPreviewDialog(
            screenshotPath = path,
            title = "Customer Payment Proof",
            onDismiss = { screenshotPreview = null }
        )
    }

    rejectTargetOrder?.let { order ->
        AlertDialog(
            onDismissRequest = { rejectTargetOrder = null },
            containerColor = StoreSurface,
            title = { Text("Reject Order ${order.id}", color = StatusError) },
            text = {
                OutlinedTextField(
                    value = rejectReason,
                    onValueChange = { rejectReason = it },
                    label = { Text("Rejection Reason (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateOrderStatus(
                            order.id,
                            OrderStatus.REJECTED,
                            rejectReason.ifBlank { "Payment could not be verified" }
                        )
                        rejectTargetOrder = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusError, contentColor = Color.White)
                ) {
                    Text("Confirm Reject")
                }
            },
            dismissButton = {
                TextButton(onClick = { rejectTargetOrder = null }) { Text("Cancel") }
            }
        )
    }
}
