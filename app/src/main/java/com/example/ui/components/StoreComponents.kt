package com.example.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.R
import com.example.data.local.DiamondPackageEntity
import com.example.data.model.OrderStatus
import com.example.data.model.RewardStatus
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.StatusError
import com.example.ui.theme.StatusErrorContainer
import com.example.ui.theme.StatusInfo
import com.example.ui.theme.StatusInfoContainer
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusSuccessContainer
import com.example.ui.theme.StatusWarning
import com.example.ui.theme.StatusWarningContainer
import com.example.ui.theme.StoreOutline
import com.example.ui.theme.StoreSurface
import com.example.ui.theme.StoreSurfaceElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.io.File
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatPkr(amount: Int): String {
    val nf = NumberFormat.getIntegerInstance(Locale.US)
    return "Rs. ${nf.format(amount)}"
}

fun formatDateTime(millis: Long): String {
    val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    return sdf.format(Date(millis))
}

@Composable
fun AnimatedDiamondIcon(
    modifier: Modifier = Modifier,
    sizeDp: Int = 28,
    tint: Color = NeonCyan
) {
    val transition = rememberInfiniteTransition(label = "diamond_pulse")
    val scale by transition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.07f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "diamond_scale"
    )
    Box(
        modifier = modifier
            .size((sizeDp + 14).dp)
            .scale(scale)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(tint.copy(alpha = 0.30f), Color.Transparent)
                ),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Diamond,
            contentDescription = "Diamond icon",
            tint = tint,
            modifier = Modifier.size(sizeDp.dp)
        )
    }
}

@Composable
fun DiamondPackageCard(
    pkg: DiamondPackageEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isHighlighted = pkg.badge == "Popular" || pkg.badge == "Best Value" || pkg.badge == "Mega Pack"
    val borderBrush = if (isHighlighted) {
        Brush.linearGradient(listOf(NeonBlue, NeonCyan, GoldAccent))
    } else {
        Brush.linearGradient(listOf(StoreOutline, NeonBlue.copy(alpha = 0.35f)))
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(width = 1.2.dp, brush = borderBrush, shape = RoundedCornerShape(18.dp))
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = StoreSurface),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_diamond_pack),
                    contentDescription = "${pkg.diamonds} Diamonds",
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
                if (!pkg.badge.isNullOrBlank()) {
                    Surface(
                        color = if (pkg.badge == "Best Value" || pkg.badge == "Mega Pack") GoldContainer else NeonBlue.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(
                            1.dp,
                            if (pkg.badge == "Best Value" || pkg.badge == "Mega Pack") GoldAccent.copy(alpha = 0.6f)
                            else NeonBlue.copy(alpha = 0.5f)
                        )
                    ) {
                        Text(
                            text = pkg.badge,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (pkg.badge == "Best Value" || pkg.badge == "Mega Pack") GoldAccent else NeonCyan,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "${pkg.diamonds} Diamonds",
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = formatPkr(pkg.pricePkr),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.Bold
                    ),
                    color = GoldAccent
                )
            }

            Button(
                onClick = onClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isHighlighted) NeonBlue else StoreSurfaceElevated,
                    contentColor = if (isHighlighted) Color(0xFF041320) else NeonCyan
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Buy Now",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
fun OrderStatusChip(status: OrderStatus, modifier: Modifier = Modifier) {
    val (bg, fg, icon) = when (status) {
        OrderStatus.PENDING_PAYMENT -> Triple(StatusWarningContainer, StatusWarning, Icons.Filled.HourglassTop)
        OrderStatus.PAYMENT_UNDER_REVIEW -> Triple(StatusInfoContainer, StatusInfo, Icons.Filled.Sync)
        OrderStatus.PAYMENT_APPROVED -> Triple(NeonBlue.copy(alpha = 0.18f), NeonCyan, Icons.Filled.Verified)
        OrderStatus.PROCESSING -> Triple(GoldContainer, GoldAccent, Icons.Filled.Sync)
        OrderStatus.DELIVERED -> Triple(StatusSuccessContainer, StatusSuccess, Icons.Filled.CheckCircle)
        OrderStatus.REJECTED -> Triple(StatusErrorContainer, StatusError, Icons.Filled.Close)
    }
    StatusPill(label = status.label, backgroundColor = bg, contentColor = fg, icon = icon, modifier = modifier)
}

@Composable
fun RewardStatusChip(status: RewardStatus, modifier: Modifier = Modifier) {
    val (bg, fg, icon) = when (status) {
        RewardStatus.PENDING_MANUAL_FULFILMENT -> Triple(StatusWarningContainer, StatusWarning, Icons.Filled.HourglassTop)
        RewardStatus.FULFILLED -> Triple(StatusSuccessContainer, StatusSuccess, Icons.Filled.CheckCircle)
    }
    StatusPill(label = status.label, backgroundColor = bg, contentColor = fg, icon = icon, modifier = modifier)
}

@Composable
private fun StatusPill(
    label: String,
    backgroundColor: Color,
    contentColor: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = backgroundColor,
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.dp, contentColor.copy(alpha = 0.45f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun NoticeBanner(
    text: String,
    accentColor: Color = NeonBlue,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = accentColor.copy(alpha = 0.10f),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Info,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = TextPrimary
            )
        }
    }
}

@Composable
fun EmptyStateCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = StoreSurface),
        border = BorderStroke(1.dp, StoreOutline),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(NeonBlue.copy(alpha = 0.14f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = NeonCyan,
                    modifier = Modifier.size(28.dp)
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            if (actionLabel != null && onAction != null) {
                Spacer(Modifier.height(4.dp))
                Button(onClick = onAction) {
                    Text(actionLabel)
                }
            }
        }
    }
}

@Composable
fun ScreenshotPreviewDialog(
    screenshotPath: String,
    title: String = "Payment Receipt",
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = StoreSurface),
            border = BorderStroke(1.dp, NeonBlue.copy(alpha = 0.5f)),
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
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close receipt preview",
                            tint = TextSecondary
                        )
                    }
                }
                val file = File(screenshotPath)
                if (file.exists()) {
                    AsyncImage(
                        model = file,
                        contentDescription = "Uploaded payment screenshot",
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 220.dp, max = 420.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(StoreSurfaceElevated),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(StoreSurfaceElevated),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Receipt reference: $screenshotPath",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}
