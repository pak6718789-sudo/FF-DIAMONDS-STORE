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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.DiamondPackageEntity
import com.example.data.local.UserEntity
import com.example.data.model.UserRole
import com.example.ui.components.AnimatedDiamondIcon
import com.example.ui.components.DiamondPackageCard
import com.example.ui.components.NoticeBanner
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.StoreBackground
import com.example.ui.theme.StoreOutline
import com.example.ui.theme.StoreSurface
import com.example.ui.theme.StoreSurfaceElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun HomeScreen(
    currentUser: UserEntity?,
    packages: List<DiamondPackageEntity>,
    quickUid: String,
    onQuickUidChange: (String) -> Unit,
    onBrowsePackages: () -> Unit,
    onSelectPackage: (DiamondPackageEntity) -> Unit,
    onOpenPaymentInstructions: () -> Unit,
    onOpenContactUs: () -> Unit,
    onOpenAccount: () -> Unit,
    onOpenAdmin: () -> Unit,
    modifier: Modifier = Modifier
) {
    var uidInput by remember(quickUid, currentUser?.playerUid) {
        mutableStateOf(
            quickUid.ifBlank {
                currentUser?.playerUid?.takeIf { it != "ADMIN" }.orEmpty()
            }
        )
    }

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
                .widthIn(max = 720.dp),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Top Brand Bar
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AnimatedDiamondIcon(sizeDp = 24, tint = NeonCyan)
                        Column {
                            Text(
                                text = "FF DIAMOND STORE",
                                style = MaterialTheme.typography.headlineSmall,
                                color = TextPrimary
                            )
                            Text(
                                text = "PAKISTAN PKR TOP-UP",
                                style = MaterialTheme.typography.labelSmall,
                                color = NeonBlue
                            )
                        }
                    }

                    if (currentUser?.role == UserRole.ADMIN) {
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .clickable(onClick = onOpenAdmin)
                                .testTag("home_admin_pill"),
                            color = GoldAccent.copy(alpha = 0.18f),
                            border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.6f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.AdminPanelSettings,
                                    contentDescription = "Admin Panel",
                                    tint = GoldAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Admin",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = GoldAccent
                                )
                            }
                        }
                    } else {
                        OutlinedButton(
                            onClick = onOpenAccount,
                            modifier = Modifier.testTag("home_account_button"),
                            border = BorderStroke(1.dp, NeonBlue.copy(alpha = 0.55f)),
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = currentUser?.fullName?.split(" ")?.firstOrNull() ?: "Login / Sign Up",
                                style = MaterialTheme.typography.labelMedium,
                                color = NeonCyan
                            )
                        }
                    }
                }
            }

            // Hero Banner Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp),
                    shape = RoundedCornerShape(22.dp),
                    border = BorderStroke(
                        1.dp,
                        Brush.linearGradient(listOf(NeonBlue, NeonCyan.copy(alpha = 0.4f), GoldAccent.copy(alpha = 0.5f)))
                    ),
                    colors = CardDefaults.cardColors(containerColor = StoreSurface)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Image(
                            painter = painterResource(id = R.drawable.img_hero_banner),
                            contentDescription = "FF Diamond Store Hero Banner",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            StoreBackground.copy(alpha = 0.94f),
                                            StoreBackground.copy(alpha = 0.72f),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Surface(
                                color = NeonBlue.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(1.dp, NeonBlue.copy(alpha = 0.6f))
                            ) {
                                Text(
                                    text = "JAZZCASH & EASYPAISA",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = NeonCyan,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = stringResource(R.string.hero_title),
                                    style = MaterialTheme.typography.headlineLarge,
                                    color = TextPrimary
                                )
                                Text(
                                    text = stringResource(R.string.hero_subtitle),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary,
                                    modifier = Modifier.fillMaxWidth(0.75f)
                                )
                            }

                            Button(
                                onClick = onBrowsePackages,
                                modifier = Modifier.testTag("hero_shop_button"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = NeonBlue,
                                    contentColor = Color(0xFF041320)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Filled.Diamond, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Top Up Diamonds", style = MaterialTheme.typography.labelLarge)
                                Spacer(Modifier.width(4.dp))
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            // Quick Player UID Entry
            item {
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
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Filled.Badge, contentDescription = null, tint = GoldAccent)
                            Text(
                                text = "Free Fire Player UID",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = uidInput,
                                onValueChange = { value ->
                                    val digits = value.filter { it.isDigit() }.take(15)
                                    uidInput = digits
                                    onQuickUidChange(digits)
                                },
                                label = { Text("Enter 6-15 digit Player UID") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("home_uid_input")
                            )
                            Button(
                                onClick = onBrowsePackages,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = NeonCyan,
                                    contentColor = Color(0xFF041320)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.height(52.dp)
                            ) {
                                Text("Go", style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }
                }
            }

            // Featured Packages Horizontal Carousel
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "POPULAR PACKAGES",
                            style = MaterialTheme.typography.headlineSmall,
                            color = TextPrimary
                        )
                        Text(
                            text = "View All",
                            style = MaterialTheme.typography.labelLarge,
                            color = NeonCyan,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(onClick = onBrowsePackages)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    val featured = remember(packages) {
                        packages.filter { it.badge != null }.ifEmpty { packages.take(4) }
                    }
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(featured, key = { it.id }) { pkg ->
                            DiamondPackageCard(
                                pkg = pkg,
                                onClick = { onSelectPackage(pkg) },
                                modifier = Modifier.width(178.dp)
                            )
                        }
                    }
                }
            }

            // How Top-Up Works
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "HOW IT WORKS",
                        style = MaterialTheme.typography.headlineSmall,
                        color = TextPrimary
                    )
                    StepRowCard(
                        step = "01",
                        title = "Choose Package & Player UID",
                        subtitle = "Pick a PKR diamond bundle and verify your Free Fire Player UID.",
                        icon = Icons.Filled.Diamond
                    )
                    StepRowCard(
                        step = "02",
                        title = "Pay via JazzCash or EasyPaisa",
                        subtitle = "Send exact PKR to our displayed account and upload your TID + receipt screenshot.",
                        icon = Icons.Filled.Payments
                    )
                    StepRowCard(
                        step = "03",
                        title = "Manual Verification & Delivery",
                        subtitle = "Admins verify your receipt and manually fulfill diamonds via an authorized service.",
                        icon = Icons.Filled.VerifiedUser
                    )
                }
            }

            // Quick Support & Payment Guides
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickLinkCard(
                        title = "Payment Guide",
                        subtitle = "JazzCash & EasyPaisa details",
                        icon = Icons.Filled.AccountBalanceWallet,
                        accent = NeonBlue,
                        onClick = onOpenPaymentInstructions,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("home_payment_guide_card")
                    )
                    QuickLinkCard(
                        title = "Contact Us",
                        subtitle = "24/7 order support",
                        icon = Icons.Filled.HeadsetMic,
                        accent = GoldAccent,
                        onClick = onOpenContactUs,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("home_contact_card")
                    )
                }
            }

            // Notices
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    NoticeBanner(
                        text = stringResource(R.string.demo_rates_notice),
                        accentColor = GoldAccent
                    )
                    NoticeBanner(
                        text = stringResource(R.string.local_mode_notice),
                        accentColor = NeonBlue
                    )
                }
            }
        }
    }
}

@Composable
private fun StepRowCard(
    step: String,
    title: String,
    subtitle: String,
    icon: ImageVector
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = StoreSurface),
        border = BorderStroke(1.dp, StoreOutline),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(StoreSurfaceElevated, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(22.dp))
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "STEP $step  •  $title",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun QuickLinkCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = StoreSurface),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(accent.copy(alpha = 0.16f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = title, tint = accent, modifier = Modifier.size(20.dp))
            }
            Text(title, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }
    }
}
