package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.FreeRewardEntity
import com.example.data.local.NotificationEntity
import com.example.data.local.OrderEntity
import com.example.data.local.UserEntity
import com.example.data.model.OrderStatus
import com.example.data.model.RewardStatus
import com.example.data.model.UserRole
import com.example.data.repository.StoreRepository
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.NoticeBanner
import com.example.ui.components.RewardStatusChip
import com.example.ui.components.formatDateTime
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StoreBackground
import com.example.ui.theme.StoreOutline
import com.example.ui.theme.StoreSurface
import com.example.ui.theme.StoreSurfaceElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun DashboardScreen(
    currentUser: UserEntity?,
    adminExists: Boolean,
    orders: List<OrderEntity>,
    rewards: List<FreeRewardEntity>,
    notifications: List<NotificationEntity>,
    isBusy: Boolean,
    onLogin: (email: String, password: String) -> Unit,
    onRegister: (fullName: String, email: String, playerUid: String, password: String) -> Unit,
    onClaimFirstAdmin: (fullName: String, email: String, passcode: String, onSuccess: () -> Unit) -> Unit,
    onLoginAdmin: (email: String, passcode: String, onSuccess: () -> Unit) -> Unit,
    onLogout: () -> Unit,
    onUpdatePlayerUid: (String) -> Unit,
    onMarkNotificationRead: (Int) -> Unit,
    onOpenPaymentInstructions: () -> Unit,
    onOpenContactUs: () -> Unit,
    onOpenAdminPanel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showAdminDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(StoreBackground)
            .statusBarsPadding(),
        contentAlignment = Alignment.TopCenter
    ) {
        if (currentUser == null) {
            AuthPortalView(
                adminExists = adminExists,
                isBusy = isBusy,
                onLogin = onLogin,
                onRegister = onRegister,
                onOpenAdminDialog = { showAdminDialog = true }
            )
        } else {
            AuthenticatedDashboardView(
                user = currentUser,
                orders = orders,
                rewards = rewards,
                notifications = notifications,
                onLogout = onLogout,
                onUpdatePlayerUid = onUpdatePlayerUid,
                onMarkNotificationRead = onMarkNotificationRead,
                onOpenPaymentInstructions = onOpenPaymentInstructions,
                onOpenContactUs = onOpenContactUs,
                onOpenAdminPanel = onOpenAdminPanel
            )
        }
    }

    if (showAdminDialog) {
        AdminAccessDialog(
            adminExists = adminExists,
            isBusy = isBusy,
            onDismiss = { showAdminDialog = false },
            onClaimFirstAdmin = { name, email, pass ->
                onClaimFirstAdmin(name, email, pass) {
                    showAdminDialog = false
                    onOpenAdminPanel()
                }
            },
            onLoginAdmin = { email, pass ->
                onLoginAdmin(email, pass) {
                    showAdminDialog = false
                    onOpenAdminPanel()
                }
            }
        )
    }
}

@Composable
private fun AuthPortalView(
    adminExists: Boolean,
    isBusy: Boolean,
    onLogin: (String, String) -> Unit,
    onRegister: (String, String, String, String) -> Unit,
    onOpenAdminDialog: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var playerUid by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .widthIn(max = 560.dp),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "ACCOUNT & LOGIN",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary
                )
                Text(
                    text = "Sign in or create an account to place orders and claim free diamond rewards.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = StoreSurface),
                border = BorderStroke(1.dp, StoreOutline),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = StoreSurfaceElevated,
                        contentColor = NeonCyan
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text("Login") },
                            modifier = Modifier.testTag("auth_tab_login")
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text("Register") },
                            modifier = Modifier.testTag("auth_tab_register")
                        )
                    }

                    if (selectedTab == 1) {
                        OutlinedTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            label = { Text("Full Name") },
                            leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("register_name_input")
                        )
                    }

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_email_input")
                    )

                    if (selectedTab == 1) {
                        OutlinedTextField(
                            value = playerUid,
                            onValueChange = { value -> playerUid = value.filter { it.isDigit() }.take(15) },
                            label = { Text("Free Fire Player UID") },
                            leadingIcon = { Icon(Icons.Filled.Badge, contentDescription = null) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("register_uid_input")
                        )
                    }

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password (min 6 chars)") },
                        leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_password_input")
                    )

                    Button(
                        onClick = {
                            if (selectedTab == 0) {
                                onLogin(email, password)
                            } else {
                                onRegister(fullName, email, playerUid, password)
                            }
                        },
                        enabled = !isBusy,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("auth_submit_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonBlue,
                            contentColor = Color(0xFF041320)
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        if (isBusy) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color(0xFF041320), strokeWidth = 2.dp)
                        } else {
                            Text(
                                text = if (selectedTab == 0) "LOGIN" else "CREATE ACCOUNT",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }
        }

        // Admin Portal Entry Card (no hardcoded passwords!)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = StoreSurface),
                border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(18.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(GoldContainer, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.AdminPanelSettings, contentDescription = null, tint = GoldAccent)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (adminExists) "Admin Portal" else "First-Run Admin Setup",
                            style = MaterialTheme.typography.titleMedium,
                            color = GoldAccent
                        )
                        Text(
                            text = if (adminExists) {
                                "Admin (${StoreRepository.DESIGNATED_ADMIN_EMAIL}): Manage orders, PKR rates & rewards."
                            } else {
                                "Designated Admin (${StoreRepository.DESIGNATED_ADMIN_EMAIL}): Set your Admin passcode to unlock."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                    OutlinedButton(
                        onClick = onOpenAdminDialog,
                        border = BorderStroke(1.dp, GoldAccent),
                        modifier = Modifier.testTag("open_admin_auth_button")
                    ) {
                        Text(if (adminExists) "Admin Login" else "Claim Admin", color = GoldAccent)
                    }
                }
            }
        }
    }
}

@Composable
private fun AuthenticatedDashboardView(
    user: UserEntity,
    orders: List<OrderEntity>,
    rewards: List<FreeRewardEntity>,
    notifications: List<NotificationEntity>,
    onLogout: () -> Unit,
    onUpdatePlayerUid: (String) -> Unit,
    onMarkNotificationRead: (Int) -> Unit,
    onOpenPaymentInstructions: () -> Unit,
    onOpenContactUs: () -> Unit,
    onOpenAdminPanel: () -> Unit
) {
    var editingUid by remember { mutableStateOf(false) }
    var uidDraft by remember(user.playerUid) { mutableStateOf(user.playerUid) }

    val deliveredDiamonds = orders.filter { it.status == OrderStatus.DELIVERED }.sumOf { it.diamonds }
    val pendingOrders = orders.count { it.status.isPending }
    val pendingRewardDiamonds = rewards
        .filter { it.status == RewardStatus.PENDING_MANUAL_FULFILMENT }
        .sumOf { it.diamonds }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .widthIn(max = 760.dp),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Profile Header Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = StoreSurface),
                border = BorderStroke(1.dp, NeonBlue.copy(alpha = 0.45f)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
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
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(NeonBlue.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.Person, contentDescription = null, tint = NeonCyan)
                            }
                            Column {
                                Text(user.fullName, style = MaterialTheme.typography.titleLarge, color = TextPrimary)
                                Text(user.email, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            }
                        }
                        IconButton(onClick = onLogout, modifier = Modifier.testTag("logout_button")) {
                            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Sign out", tint = TextSecondary)
                        }
                    }

                    if (user.role != UserRole.ADMIN) {
                        HorizontalDivider(color = StoreOutline)
                        if (editingUid) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = uidDraft,
                                    onValueChange = { value -> uidDraft = value.filter { it.isDigit() }.take(15) },
                                    label = { Text("Free Fire Player UID") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f)
                                )
                                Button(
                                    onClick = {
                                        onUpdatePlayerUid(uidDraft)
                                        editingUid = false
                                    }
                                ) {
                                    Icon(Icons.Filled.Save, contentDescription = "Save UID", modifier = Modifier.size(18.dp))
                                }
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Filled.Badge, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(18.dp))
                                    Text(
                                        text = "Player UID: ${user.playerUid}",
                                        style = MaterialTheme.typography.titleSmall.copy(fontFamily = JetBrainsMonoFontFamily),
                                        color = TextPrimary
                                    )
                                }
                                TextButton(onClick = { editingUid = true }) {
                                    Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Edit UID")
                                }
                            }
                        }
                    }
                }
            }
        }

        // Admin Panel CTA if user is Admin
        if (user.role == UserRole.ADMIN) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .clickable(onClick = onOpenAdminPanel)
                        .testTag("open_admin_panel_card"),
                    colors = CardDefaults.cardColors(containerColor = GoldContainer),
                    border = BorderStroke(1.5.dp, GoldAccent)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Filled.AdminPanelSettings, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(30.dp))
                            Column {
                                Text("ADMIN DASHBOARD", style = MaterialTheme.typography.titleMedium, color = GoldAccent)
                                Text(
                                    "Manage orders, PKR packages, payments & Give Free Diamonds",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextPrimary
                                )
                            }
                        }
                        Button(
                            onClick = onOpenAdminPanel,
                            colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = Color(0xFF211600))
                        ) {
                            Text("Open")
                        }
                    }
                }
            }
        }

        // Summary Metrics
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DashboardStatTile(
                    label = "Orders",
                    value = orders.size.toString(),
                    accent = NeonBlue,
                    modifier = Modifier.weight(1f)
                )
                DashboardStatTile(
                    label = "Pending",
                    value = pendingOrders.toString(),
                    accent = GoldAccent,
                    modifier = Modifier.weight(1f)
                )
                DashboardStatTile(
                    label = "Delivered",
                    value = "$deliveredDiamonds💎",
                    accent = StatusSuccess,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Free Diamond Rewards Section
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = StoreSurface),
                border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.45f)),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_reward_chest),
                            contentDescription = "Free diamond rewards",
                            modifier = Modifier
                                .size(54.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "FREE DIAMOND REWARDS",
                                style = MaterialTheme.typography.titleMedium,
                                color = GoldAccent
                            )
                            Text(
                                text = if (pendingRewardDiamonds > 0) {
                                    "$pendingRewardDiamonds Diamonds pending manual fulfilment"
                                } else {
                                    "Admin-issued gift rewards appear here"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }

                    NoticeBanner(
                        text = stringResource(R.string.reward_fulfilment_notice),
                        accentColor = GoldAccent
                    )

                    if (rewards.isEmpty()) {
                        Text(
                            text = "No free diamond rewards issued to your account yet.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    } else {
                        rewards.forEach { reward ->
                            Surface(
                                color = StoreSurfaceElevated,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, StoreOutline)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "+${reward.diamonds} Diamonds",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = GoldAccent
                                        )
                                        RewardStatusChip(status = reward.status)
                                    }
                                    Text(
                                        text = "Reason: ${reward.reason}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "Issued by ${reward.issuedByAdminName} • ${formatDateTime(reward.createdAt)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Notifications Section
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Filled.Notifications, contentDescription = null, tint = NeonCyan)
                    Text("NOTIFICATIONS", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                }
                if (notifications.isEmpty()) {
                    EmptyStateCard(
                        icon = Icons.Filled.Notifications,
                        title = "All Caught Up",
                        subtitle = "Order updates and admin notices will appear here."
                    )
                }
            }
        }

        items(notifications, key = { it.id }) { notif ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { if (!notif.isRead) onMarkNotificationRead(notif.id) },
                colors = CardDefaults.cardColors(
                    containerColor = if (notif.isRead) StoreSurface else StoreSurfaceElevated
                ),
                border = BorderStroke(
                    1.dp,
                    if (notif.isRead) StoreOutline else NeonBlue.copy(alpha = 0.6f)
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(notif.title, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                        if (!notif.isRead) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(NeonCyan, CircleShape)
                            )
                        }
                    }
                    Text(notif.message, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    Text(formatDateTime(notif.createdAt), style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                }
            }
        }

        // Help Links
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onOpenPaymentInstructions,
                    modifier = Modifier.weight(1f),
                    border = BorderStroke(1.dp, StoreOutline)
                ) {
                    Icon(Icons.Filled.AccountBalanceWallet, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Payment Guide")
                }
                OutlinedButton(
                    onClick = onOpenContactUs,
                    modifier = Modifier.weight(1f),
                    border = BorderStroke(1.dp, StoreOutline)
                ) {
                    Icon(Icons.Filled.HeadsetMic, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Contact Us")
                }
            }
        }
    }
}

@Composable
private fun DashboardStatTile(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = StoreSurface),
        border = BorderStroke(1.dp, StoreOutline),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(fontFamily = JetBrainsMonoFontFamily),
                color = accent
            )
        }
    }
}

@Composable
private fun AdminAccessDialog(
    adminExists: Boolean,
    isBusy: Boolean,
    onDismiss: () -> Unit,
    onClaimFirstAdmin: (name: String, email: String, passcode: String) -> Unit,
    onLoginAdmin: (email: String, passcode: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf(StoreRepository.DESIGNATED_ADMIN_EMAIL) }
    var passcode by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = StoreSurface,
        title = {
            Text(
                text = if (adminExists) "Admin Sign In" else "Claim First-Run Admin",
                color = GoldAccent
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = if (adminExists) {
                        "Enter your Admin email and passcode."
                    } else {
                        "Create your initial Admin account. Passcode is stored securely with PBKDF2 hashing."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                if (!adminExists) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Admin Name") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_claim_name")
                    )
                }
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Admin Email") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_auth_email")
                )
                OutlinedTextField(
                    value = passcode,
                    onValueChange = { passcode = it },
                    label = { Text("Admin Passcode (min 6 chars)") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_auth_passcode")
                )
            }
        },
        confirmButton = {
            Button(
                enabled = !isBusy,
                onClick = {
                    if (adminExists) {
                        onLoginAdmin(email, passcode)
                    } else {
                        onClaimFirstAdmin(name, email, passcode)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = Color(0xFF211600)),
                modifier = Modifier.testTag("admin_auth_submit")
            ) {
                Text(if (adminExists) "Unlock Admin" else "Claim Admin")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
