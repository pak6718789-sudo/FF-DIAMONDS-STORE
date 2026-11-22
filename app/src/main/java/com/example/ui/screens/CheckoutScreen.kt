package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.local.DiamondPackageEntity
import com.example.data.local.PaymentConfigEntity
import com.example.data.local.UserEntity
import com.example.data.model.PaymentMethod
import com.example.data.repository.StoreRepository
import com.example.ui.components.NoticeBanner
import com.example.ui.components.formatPkr
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.StatusError
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StoreBackground
import com.example.ui.theme.StoreOutline
import com.example.ui.theme.StoreSurface
import com.example.ui.theme.StoreSurfaceElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    selectedPackage: DiamondPackageEntity?,
    currentUser: UserEntity?,
    quickUid: String,
    paymentConfigs: List<PaymentConfigEntity>,
    isBusy: Boolean,
    onBack: () -> Unit,
    onRequireLogin: () -> Unit,
    onPlaceOrder: (
        packageId: Int,
        playerUid: String,
        paymentMethod: PaymentMethod,
        transactionId: String,
        screenshotUri: Uri?
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)

    @Suppress("DEPRECATION")
    val clipboard = LocalClipboardManager.current

    var playerUid by remember(currentUser?.playerUid, quickUid) {
        mutableStateOf(
            quickUid.ifBlank {
                currentUser?.playerUid?.takeIf { it != "ADMIN" }.orEmpty()
            }
        )
    }
    var selectedMethod by remember { mutableStateOf(PaymentMethod.JAZZCASH) }
    var transactionId by remember { mutableStateOf("") }
    var screenshotUri by remember { mutableStateOf<Uri?>(null) }
    var copiedNotice by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> if (uri != null) screenshotUri = uri }
    )

    val activeConfig = paymentConfigs.firstOrNull { it.method == selectedMethod }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(StoreBackground)
    ) {
        TopAppBar(
            title = { Text("Checkout", style = MaterialTheme.typography.headlineSmall) },
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

        if (selectedPackage == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No package selected.", color = TextSecondary)
            }
            return
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 720.dp),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Selected Package Summary
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = StoreSurface),
                        border = BorderStroke(1.dp, NeonBlue.copy(alpha = 0.55f)),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_diamond_pack),
                                contentDescription = "Package image",
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${selectedPackage.diamonds} Diamonds",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Manual verification top-up",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                            Text(
                                text = formatPkr(selectedPackage.pricePkr),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = GoldAccent
                            )
                        }
                    }
                }

                // Auth Gate Check
                if (currentUser == null) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = StoreSurface),
                            border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.6f)),
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Filled.Lock, contentDescription = null, tint = GoldAccent)
                                    Text(
                                        text = "Sign In Required",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = TextPrimary
                                    )
                                }
                                Text(
                                    text = "Please sign in or register an account so you can track order status and receive notifications.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                                Button(
                                    onClick = onRequireLogin,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = NeonBlue,
                                        contentColor = Color(0xFF041320)
                                    )
                                ) {
                                    Text("Login / Register")
                                }
                            }
                        }
                    }
                    return@LazyColumn
                }

                // Player UID Field
                item {
                    val uidValid = playerUid.isEmpty() || StoreRepository.isValidPlayerUid(playerUid)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "1. Free Fire Player UID",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        OutlinedTextField(
                            value = playerUid,
                            onValueChange = { value -> playerUid = value.filter { it.isDigit() }.take(15) },
                            label = { Text("Player UID (6-15 digits)") },
                            isError = !uidValid,
                            supportingText = {
                                if (!uidValid) {
                                    Text(stringResource(R.string.error_invalid_uid), color = StatusError)
                                }
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("checkout_uid_input")
                        )
                    }
                }

                // Payment Method Selection
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "2. Select Payment Method",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            PaymentMethod.entries.forEach { method ->
                                val isSelected = selectedMethod == method
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(14.dp))
                                        .clickable {
                                            selectedMethod = method
                                            copiedNotice = false
                                        }
                                        .testTag("payment_method_${method.name.lowercase()}"),
                                    color = if (isSelected) NeonBlue.copy(alpha = 0.16f) else StoreSurface,
                                    border = BorderStroke(
                                        1.5.dp,
                                        if (isSelected) NeonCyan else StoreOutline
                                    ),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Filled.CheckCircle,
                                                contentDescription = null,
                                                tint = NeonCyan,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(Modifier.width(6.dp))
                                        }
                                        Text(
                                            text = method.displayName,
                                            style = MaterialTheme.typography.titleSmall,
                                            color = if (isSelected) NeonCyan else TextPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Selected Payment Account Details
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = StoreSurface),
                        border = BorderStroke(1.dp, StoreOutline),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "${selectedMethod.displayName} Account Details",
                                style = MaterialTheme.typography.titleMedium,
                                color = NeonCyan
                            )
                            if (activeConfig != null) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Account Holder", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                        Text(activeConfig.accountHolderName, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                                    }
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(StoreSurfaceElevated, RoundedCornerShape(12.dp))
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Account Number", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                        Text(
                                            text = activeConfig.accountNumber,
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontFamily = JetBrainsMonoFontFamily,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = GoldAccent
                                        )
                                    }
                                    OutlinedButton(
                                        onClick = {
                                            clipboard.setText(AnnotatedString(activeConfig.accountNumber))
                                            copiedNotice = true
                                        }
                                    ) {
                                        Icon(Icons.Filled.ContentCopy, contentDescription = "Copy account number", modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text(if (copiedNotice) "Copied" else "Copy")
                                    }
                                }
                                Text(
                                    text = activeConfig.instructions,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                            NoticeBanner(
                                text = stringResource(R.string.security_pin_notice),
                                accentColor = GoldAccent
                            )
                        }
                    }
                }

                // Transaction ID + Screenshot Upload
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "3. Payment Proof",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        OutlinedTextField(
                            value = transactionId,
                            onValueChange = { transactionId = it },
                            label = { Text("Transaction Reference ID (TID)") },
                            leadingIcon = { Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("checkout_tid_input")
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, NeonBlue.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                                .clip(RoundedCornerShape(16.dp))
                                .background(StoreSurface)
                                .clickable {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }
                                .padding(16.dp)
                                .testTag("checkout_upload_screenshot"),
                            contentAlignment = Alignment.Center
                        ) {
                            if (screenshotUri == null) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.CloudUpload,
                                        contentDescription = "Upload payment screenshot",
                                        tint = NeonCyan,
                                        modifier = Modifier.size(34.dp)
                                    )
                                    Text(
                                        text = "Tap to Upload Payment Screenshot",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "Uses Android Photo Picker (JPG / PNG)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    AsyncImage(
                                        model = screenshotUri,
                                        contentDescription = "Selected payment screenshot",
                                        modifier = Modifier
                                            .size(64.dp)
                                            .clip(RoundedCornerShape(10.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = StatusSuccess, modifier = Modifier.size(16.dp))
                                            Text("Receipt Attached", style = MaterialTheme.typography.titleSmall, color = StatusSuccess)
                                        }
                                        Text("Tap to change screenshot", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                    }
                                }
                            }
                        }
                    }
                }

                // Submit Order Button
                item {
                    val canSubmit = !isBusy &&
                        StoreRepository.isValidPlayerUid(playerUid) &&
                        transactionId.trim().length >= 4 &&
                        screenshotUri != null

                    Button(
                        onClick = {
                            onPlaceOrder(
                                selectedPackage.id,
                                playerUid,
                                selectedMethod,
                                transactionId,
                                screenshotUri
                            )
                        },
                        enabled = canSubmit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("checkout_submit_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonBlue,
                            contentColor = Color(0xFF041320)
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        if (isBusy) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = Color(0xFF041320),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "PLACE ORDER (${formatPkr(selectedPackage.pricePkr)})",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }
        }
    }
}
