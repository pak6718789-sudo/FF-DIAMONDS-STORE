package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.DiamondPackageEntity
import com.example.ui.components.DiamondPackageCard
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.NoticeBanner
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.StoreBackground
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun PackagesScreen(
    packages: List<DiamondPackageEntity>,
    onSelectPackage: (DiamondPackageEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(StoreBackground)
            .statusBarsPadding(),
        contentAlignment = Alignment.TopCenter
    ) {
        val columns = if (maxWidth >= 600.dp) 3 else 2

        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 840.dp),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(span = { GridItemSpan(columns) }) {
                Column(
                    modifier = Modifier.padding(bottom = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "DIAMOND PACKAGES",
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextPrimary
                    )
                    Text(
                        text = "Select a Free Fire diamond bundle in Pakistani Rupees (PKR).",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    NoticeBanner(
                        text = stringResource(R.string.demo_rates_notice),
                        accentColor = GoldAccent
                    )
                }
            }

            if (packages.isEmpty()) {
                item(span = { GridItemSpan(columns) }) {
                    EmptyStateCard(
                        icon = Icons.Filled.Diamond,
                        title = "No Packages Available",
                        subtitle = "Packages can be added from the Admin Panel."
                    )
                }
            } else {
                items(packages, key = { it.id }) { pkg ->
                    DiamondPackageCard(
                        pkg = pkg,
                        onClick = { onSelectPackage(pkg) }
                    )
                }
            }
        }
    }
}
