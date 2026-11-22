package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.DiamondPackageEntity
import com.example.data.local.FreeRewardEntity
import com.example.data.local.PaymentConfigEntity
import com.example.data.local.SupportMessageEntity
import com.example.data.local.UserEntity
import com.example.data.model.PaymentMethod
import com.example.data.model.RewardStatus
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.NoticeBanner
import com.example.ui.components.RewardStatusChip
import com.example.ui.components.formatDateTime
import com.example.ui.components.formatPkr
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.StatusError
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StoreOutline
import com.example.ui.theme.StoreSurface
import com.example.ui.theme.StoreSurfaceElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun GiveFreeDiamondsTab(
    users: List<UserEntity>,
    searchQuery: String,
    rewards: List<FreeRewardEntity>,
    onSearchChange: (String) -> Unit,
    onSendFreeDiamonds: (targetUserId: String, diamonds: Int, reason: String, onSent: () -> Unit) -> Unit,
    onMarkRewardFulfilled: (rewardId: String) -> Unit
) {
    var selectedUserId by remember { mutableStateOf<String?>(null) }
    var customQuantity by remember { mutableStateOf("100") }
    var reason by remember { mutableStateOf("") }

    val selectedUser = users.firstOrNull { it.id == selectedUserId }
    val presets = listOf(50, 100, 210, 520)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .widthIn(max = 840.dp),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = StoreSurface),
                border = BorderStroke(1.2.dp, GoldAccent.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Filled.CardGiftcard, contentDescription = null, tint = GoldAccent)
                        Text("GIVE FREE DIAMONDS", style = MaterialTheme.typography.titleLarge, color = GoldAccent)
                    }

                    NoticeBanner(
                        text = stringResource(R.string.reward_fulfilment_notice),
                        accentColor = GoldAccent
                    )

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchChange,
                        label = { Text("Search Registered User by Player UID or Name") },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("free_diamonds_uid_search")
                    )

                    if (users.isEmpty()) {
                        Text(
                            text = "No matching registered users found.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Select Recipient User:", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                            users.take(5).forEach { user ->
                                val isSelected = user.id == selectedUserId
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { selectedUserId = user.id }
                                        .testTag("select_reward_user_${user.playerUid}"),
                                    color = if (isSelected) NeonBlue.copy(alpha = 0.16f) else StoreSurfaceElevated,
                                    border = BorderStroke(1.dp, if (isSelected) NeonCyan else StoreOutline)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(user.fullName, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                                            Text("UID: ${user.playerUid} • ${user.email}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                        }
                                        if (isSelected) {
                                            Icon(Icons.Filled.CheckCircle, contentDescription = "Selected", tint = NeonCyan)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Text("Diamond Quantity:", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        presets.forEach { qty ->
                            FilterChip(
                                selected = customQuantity == qty.toString(),
                                onClick = { customQuantity = qty.toString() },
                                label = { Text("$qty💎") }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = customQuantity,
                        onValueChange = { value -> customQuantity = value.filter { it.isDigit() }.take(6) },
                        label = { Text("Custom Diamond Quantity") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("free_diamonds_qty_input")
                    )

                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        label = { Text("Reward Reason (e.g., Giveaway Winner)") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("free_diamonds_reason_input")
                    )

                    Button(
                        onClick = {
                            val target = selectedUser ?: return@Button
                            val qty = customQuantity.toIntOrNull() ?: 0
                            onSendFreeDiamonds(target.id, qty, reason) {
                                reason = ""
                            }
                        },
                        enabled = selectedUser != null && (customQuantity.toIntOrNull() ?: 0) > 0 && reason.trim().length >= 3,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("send_free_diamonds_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = Color(0xFF211600))
                    ) {
                        Text("SEND FREE DIAMONDS", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }
        }

        item {
            Text("REWARD HISTORY (${rewards.size})", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
        }

        if (rewards.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Filled.CardGiftcard,
                    title = "No Rewards Issued Yet",
                    subtitle = "Free diamond rewards issued to players will be tracked here."
                )
            }
        } else {
            items(rewards, key = { it.id }) { reward ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = StoreSurface),
                    border = BorderStroke(1.dp, StoreOutline),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("${reward.id} • +${reward.diamonds}💎", style = MaterialTheme.typography.titleSmall, color = GoldAccent)
                            RewardStatusChip(status = reward.status)
                        }
                        Text("To: ${reward.userName} (UID: ${reward.playerUid})", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                        Text("Reason: ${reward.reason}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        Text(
                            text = "Issued by Admin: ${reward.issuedByAdminName} • ${formatDateTime(reward.createdAt)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = NeonCyan
                        )
                        if (reward.status == RewardStatus.PENDING_MANUAL_FULFILMENT) {
                            Button(
                                onClick = { onMarkRewardFulfilled(reward.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = StatusSuccess, contentColor = Color.White)
                            ) {
                                Text("Mark Manual Top-Up Fulfilled")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UsersAndNotifyTab(
    users: List<UserEntity>,
    searchQuery: String,
    supportMessages: List<SupportMessageEntity>,
    onSearchChange: (String) -> Unit,
    onSendNotification: (targetUserId: String?, title: String, message: String, onSent: () -> Unit) -> Unit,
    onResolveSupportMessage: (Int) -> Unit
) {
    var notifTitle by remember { mutableStateOf("") }
    var notifMessage by remember { mutableStateOf("") }
    var selectedTargetUserId by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .widthIn(max = 840.dp),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Send Notification Box
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = StoreSurface),
                border = BorderStroke(1.dp, NeonBlue.copy(alpha = 0.45f)),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Filled.NotificationsActive, contentDescription = null, tint = NeonCyan)
                        Text("SEND NOTIFICATION", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                    }
                    Text(
                        text = if (selectedTargetUserId == null) {
                            "Target: All Registered Users (Broadcast)"
                        } else {
                            val target = users.firstOrNull { it.id == selectedTargetUserId }
                            "Target: ${target?.fullName ?: "Selected User"} (UID ${target?.playerUid ?: ""})"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = GoldAccent
                    )
                    if (selectedTargetUserId != null) {
                        TextButton(onClick = { selectedTargetUserId = null }) {
                            Text("Switch to Broadcast All")
                        }
                    }
                    OutlinedTextField(
                        value = notifTitle,
                        onValueChange = { notifTitle = it },
                        label = { Text("Notification Title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = notifMessage,
                        onValueChange = { notifMessage = it },
                        label = { Text("Message") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = {
                            onSendNotification(selectedTargetUserId, notifTitle, notifMessage) {
                                notifTitle = ""
                                notifMessage = ""
                            }
                        },
                        enabled = notifTitle.trim().length >= 2 && notifMessage.trim().length >= 3,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonBlue, contentColor = Color(0xFF041320))
                    ) {
                        Text("Send Notification")
                    }
                }
            }
        }

        // Registered Users Search
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("REGISTERED USERS (${users.size})", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    label = { Text("Search Users by Player UID or Name") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        items(users, key = { it.id }) { user ->
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
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(user.fullName, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                        Text(user.email, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        Text(
                            text = "UID: ${user.playerUid}",
                            style = MaterialTheme.typography.labelMedium.copy(fontFamily = JetBrainsMonoFontFamily),
                            color = NeonCyan
                        )
                    }
                    OutlinedButton(onClick = { selectedTargetUserId = user.id }) {
                        Text("Notify")
                    }
                }
            }
        }

        // Support Messages Inbox
        if (supportMessages.isNotEmpty()) {
            item {
                Text("SUPPORT INQUIRIES (${supportMessages.size})", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
            }
            items(supportMessages, key = { it.id }) { msg ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = StoreSurface),
                    border = BorderStroke(1.dp, StoreOutline),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(msg.subject, style = MaterialTheme.typography.titleSmall, color = GoldAccent)
                            Text(msg.status, style = MaterialTheme.typography.labelSmall, color = NeonCyan)
                        }
                        Text("${msg.name} (${msg.email}) • UID: ${msg.playerUid.ifBlank { "N/A" }}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        Text(msg.message, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                        if (msg.status != "Resolved") {
                            TextButton(onClick = { onResolveSupportMessage(msg.id) }) {
                                Text("Mark Resolved")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PackagesAndPayTab(
    packages: List<DiamondPackageEntity>,
    paymentConfigs: List<PaymentConfigEntity>,
    onSavePackage: (packageId: Int?, diamonds: Int, pricePkr: Int, badge: String?) -> Unit,
    onDeletePackage: (packageId: Int) -> Unit,
    onUpdatePaymentConfig: (PaymentMethod, String, String, String) -> Unit
) {
    var editingPackage by remember { mutableStateOf<DiamondPackageEntity?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .widthIn(max = 840.dp),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Packages Header + Add Button
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("DIAMOND PACKAGES", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
                Button(
                    onClick = { showAddDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonBlue, contentColor = Color(0xFF041320)),
                    modifier = Modifier.testTag("admin_add_package_button")
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Add Package")
                }
            }
        }

        items(packages, key = { it.id }) { pkg ->
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
                    Column {
                        Text("${pkg.diamonds} Diamonds", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                        Text(
                            text = formatPkr(pkg.pricePkr) + (pkg.badge?.let { " • $it" } ?: ""),
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = JetBrainsMonoFontFamily),
                            color = GoldAccent
                        )
                    }
                    Row {
                        IconButton(onClick = { editingPackage = pkg }) {
                            Icon(Icons.Filled.Edit, contentDescription = "Edit package", tint = NeonCyan)
                        }
                        IconButton(onClick = { onDeletePackage(pkg.id) }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete package", tint = StatusError)
                        }
                    }
                }
            }
        }

        item {
            HorizontalDivider(color = StoreOutline, modifier = Modifier.padding(vertical = 4.dp))
            Text("PAYMENT ACCOUNTS (JAZZCASH & EASYPAISA)", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
        }

        items(paymentConfigs, key = { it.method }) { config ->
            PaymentConfigEditorCard(
                config = config,
                onSave = { holder, number, instr ->
                    onUpdatePaymentConfig(config.method, holder, number, instr)
                }
            )
        }
    }

    if (showAddDialog) {
        PackageEditorDialog(
            initial = null,
            onDismiss = { showAddDialog = false },
            onSave = { diamonds, price, badge ->
                onSavePackage(null, diamonds, price, badge)
                showAddDialog = false
            }
        )
    }

    editingPackage?.let { pkg ->
        PackageEditorDialog(
            initial = pkg,
            onDismiss = { editingPackage = null },
            onSave = { diamonds, price, badge ->
                onSavePackage(pkg.id, diamonds, price, badge)
                editingPackage = null
            }
        )
    }
}

@Composable
private fun PaymentConfigEditorCard(
    config: PaymentConfigEntity,
    onSave: (holder: String, number: String, instructions: String) -> Unit
) {
    var holder by remember(config.accountHolderName) { mutableStateOf(config.accountHolderName) }
    var number by remember(config.accountNumber) { mutableStateOf(config.accountNumber) }
    var instructions by remember(config.instructions) { mutableStateOf(config.instructions) }

    Card(
        colors = CardDefaults.cardColors(containerColor = StoreSurface),
        border = BorderStroke(1.dp, NeonBlue.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(config.method.displayName, style = MaterialTheme.typography.titleMedium, color = NeonCyan)
            OutlinedTextField(
                value = holder,
                onValueChange = { holder = it },
                label = { Text("Account Holder Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = number,
                onValueChange = { number = it },
                label = { Text("Account Number") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = instructions,
                onValueChange = { instructions = it },
                label = { Text("Payment Instructions") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = { onSave(holder, number, instructions) },
                colors = ButtonDefaults.buttonColors(containerColor = NeonBlue, contentColor = Color(0xFF041320))
            ) {
                Text("Save ${config.method.displayName}")
            }
        }
    }
}

@Composable
private fun PackageEditorDialog(
    initial: DiamondPackageEntity?,
    onDismiss: () -> Unit,
    onSave: (diamonds: Int, pricePkr: Int, badge: String?) -> Unit
) {
    var diamondsStr by remember { mutableStateOf(initial?.diamonds?.toString().orEmpty()) }
    var priceStr by remember { mutableStateOf(initial?.pricePkr?.toString().orEmpty()) }
    var badge by remember { mutableStateOf(initial?.badge.orEmpty()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = StoreSurface,
        title = { Text(if (initial == null) "Add Diamond Package" else "Edit Package", color = NeonCyan) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = diamondsStr,
                    onValueChange = { v -> diamondsStr = v.filter { it.isDigit() } },
                    label = { Text("Diamond Quantity") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = priceStr,
                    onValueChange = { v -> priceStr = v.filter { it.isDigit() } },
                    label = { Text("Price (PKR)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = badge,
                    onValueChange = { badge = it },
                    label = { Text("Badge Label (optional, e.g., Popular)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val d = diamondsStr.toIntOrNull() ?: 0
                    val p = priceStr.toIntOrNull() ?: 0
                    if (d > 0 && p > 0) onSave(d, p, badge.ifBlank { null })
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
