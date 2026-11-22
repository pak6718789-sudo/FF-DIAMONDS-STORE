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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.PaymentConfigEntity
import com.example.data.model.PaymentMethod
import com.example.ui.components.NoticeBanner
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentInstructionsScreen(
    paymentConfigs: List<PaymentConfigEntity>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)

    @Suppress("DEPRECATION")
    val clipboard = LocalClipboardManager.current
    var copiedMethod by remember { mutableStateOf<PaymentMethod?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(StoreBackground)
    ) {
        TopAppBar(
            title = { Text(stringResource(R.string.payment_instructions_title), style = MaterialTheme.typography.headlineSmall) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = StoreSurface,
                titleContentColor = TextPrimary,
                navigationIconContentColor = TextPrimary
            )
        )

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 720.dp),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    NoticeBanner(
                        text = stringResource(R.string.security_pin_notice),
                        accentColor = GoldAccent
                    )
                }

                items(paymentConfigs, key = { it.method }) { config ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = StoreSurface),
                        border = BorderStroke(1.dp, NeonBlue.copy(alpha = 0.45f)),
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
                                Icon(Icons.Filled.AccountBalanceWallet, contentDescription = null, tint = NeonCyan)
                                Text(
                                    text = config.method.displayName,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = NeonCyan
                                )
                            }

                            Text(
                                text = "Account Title: ${config.accountHolderName}",
                                style = MaterialTheme.typography.titleSmall,
                                color = TextPrimary
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(StoreSurfaceElevated, RoundedCornerShape(12.dp))
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = config.accountNumber,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = GoldAccent
                                )
                                OutlinedButton(
                                    onClick = {
                                        clipboard.setText(AnnotatedString(config.accountNumber))
                                        copiedMethod = config.method
                                    }
                                ) {
                                    Icon(Icons.Filled.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(if (copiedMethod == config.method) "Copied" else "Copy")
                                }
                            }

                            Text(
                                text = config.instructions,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}
