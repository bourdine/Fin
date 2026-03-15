package com.lottttto.miner.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lottttto.miner.models.CoinType
import com.lottttto.miner.viewmodels.MiningPoolsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MiningPoolsScreen(
    onBack: () -> Unit,
    viewModel: MiningPoolsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val pools = listOf(
        PoolItem("MoneroOcean", "pool.moneroocean.stream:5555", "Popular pool with PPLNS payout", true),
        PoolItem("SupportXMR", "pool.supportxmr.com:5555", "Large pool, low fee", false),
        PoolItem("C3Pool", "c3pool.com:15555", "Mobile-friendly pool", false)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mining Pools") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Monero (XMR)", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                        Text("Current pool: ${pools.find { it.isSelected }?.name ?: "None"}", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            item {
                Text("Available Pools", style = MaterialTheme.typography.titleMedium)
            }
            items(pools) { pool ->
                PoolCard(pool = pool, onSelect = {})
            }
        }
    }
}

@Composable
fun PoolCard(pool: PoolItem, onSelect: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        colors = CardDefaults.cardColors(
            containerColor = if (pool.isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(pool.name, style = MaterialTheme.typography.titleMedium)
                Text(pool.url, style = MaterialTheme.typography.bodySmall)
                Text(pool.description, style = MaterialTheme.typography.bodySmall)
            }
            if (pool.isSelected) Icon(Icons.Default.Check, contentDescription = "Selected", tint = MaterialTheme.colorScheme.primary)
        }
    }
}

data class PoolItem(val name: String, val url: String, val description: String, val isSelected: Boolean = false)
