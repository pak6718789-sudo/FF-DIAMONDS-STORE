package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.UserEntity
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.StoreBackground
import com.example.ui.theme.StoreOutline
import com.example.ui.theme.StoreSurface
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactUsScreen(
    currentUser: UserEntity?,
    isBusy: Boolean,
    onBack: () -> Unit,
    onSubmitMessage: (
        name: String,
        email: String,
        playerUid: String,
        subject: String,
        message: String,
        onSubmitted: () -> Unit
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)

    var name by remember(currentUser?.fullName) { mutableStateOf(currentUser?.fullName.orEmpty()) }
    var email by remember(currentUser?.email) { mutableStateOf(currentUser?.email.orEmpty()) }
    var playerUid by remember(currentUser?.playerUid) {
        mutableStateOf(currentUser?.playerUid?.takeIf { it != "ADMIN" }.orEmpty())
    }
    var subject by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(StoreBackground)
    ) {
        TopAppBar(
            title = { Text(stringResource(R.string.contact_support_title), style = MaterialTheme.typography.headlineSmall) },
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
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = StoreSurface),
                        border = BorderStroke(1.dp, NeonBlue.copy(alpha = 0.45f)),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Filled.HeadsetMic, contentDescription = null, tint = NeonCyan)
                            Text("Support Hours", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                            Text(
                                "Daily 9:00 AM – 12:00 AM PKT. Average payment verification time is 5–20 minutes.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                            Text(
                                "Always include your Order ID and Player UID when asking about a pending top-up.",
                                style = MaterialTheme.typography.bodySmall,
                                color = GoldAccent
                            )
                        }
                    }
                }

                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = StoreSurface),
                        border = BorderStroke(1.dp, StoreOutline),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text("Send Support Message", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = { Text("Your Name") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = { Text("Email Address") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = playerUid,
                                onValueChange = { value -> playerUid = value.filter { it.isDigit() }.take(15) },
                                label = { Text("Free Fire Player UID (Optional)") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = subject,
                                onValueChange = { subject = it },
                                label = { Text("Subject / Order ID") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = message,
                                onValueChange = { message = it },
                                label = { Text("How can we help?") },
                                minLines = 3,
                                maxLines = 5,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Button(
                                onClick = {
                                    onSubmitMessage(name, email, playerUid, subject, message) {
                                        subject = ""
                                        message = ""
                                    }
                                },
                                enabled = !isBusy,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("contact_submit_button"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = NeonBlue,
                                    contentColor = Color(0xFF041320)
                                )
                            ) {
                                Text("SUBMIT INQUIRY", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                            }
                        }
                    }
                }
            }
        }
    }
}
