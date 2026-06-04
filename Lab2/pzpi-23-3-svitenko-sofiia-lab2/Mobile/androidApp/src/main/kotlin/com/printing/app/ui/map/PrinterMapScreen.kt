package com.printing.app.ui.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.printing.app.R
import com.printing.app.domain.model.Printer

private data class StatusConfig(val label: String, val dot: Color, val bg: Color, val text: Color)

private fun statusConfig(status: String): StatusConfig = when (status.lowercase()) {
    "idle"     -> StatusConfig("Online",    Color(0xFF22C55E), Color(0xFFDCFCE7), Color(0xFF15803D))
    "printing" -> StatusConfig("Printing",  Color(0xFF3B82F6), Color(0xFFDBEAFE), Color(0xFF1D4ED8))
    "offline"  -> StatusConfig("Offline",   Color(0xFFEF4444), Color(0xFFFEE2E2), Color(0xFFB91C1C))
    else       -> StatusConfig(status,      Color(0xFF9CA3AF), Color(0xFFF3F4F6), Color(0xFF4B5563))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrinterMapScreen(viewModel: MapViewModel) {
    val printers by viewModel.printers.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) { viewModel.load() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.printers)) },
                actions = {
                    IconButton(onClick = { viewModel.load() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when {
                isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                printers.isEmpty() -> EmptyState(Modifier.align(Alignment.Center))
                else -> PrinterList(printers)
            }
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text("No printers found", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.outline)
    }
}

@Composable
private fun PrinterList(printers: List<Printer>) {
    // Summary row at top
    val online   = printers.count { it.status.lowercase() == "idle" }
    val printing = printers.count { it.status.lowercase() == "printing" }
    val offline  = printers.count { it.status.lowercase() == "offline" }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SummaryRow(online, printing, offline)
            Spacer(Modifier.height(4.dp))
        }
        items(printers) { printer -> PrinterCard(printer) }
    }
}

@Composable
private fun SummaryRow(online: Int, printing: Int, offline: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        SummaryChip("Online $online",   Color(0xFF22C55E), Color(0xFFDCFCE7), Modifier.weight(1f))
        SummaryChip("Busy $printing",   Color(0xFF3B82F6), Color(0xFFDBEAFE), Modifier.weight(1f))
        SummaryChip("Offline $offline", Color(0xFFEF4444), Color(0xFFFEE2E2), Modifier.weight(1f))
    }
}

@Composable
private fun SummaryChip(label: String, textColor: Color, bgColor: Color, modifier: Modifier) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = bgColor
    ) {
        Text(
            text = label,
            color = textColor,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp)
        )
    }
}

@Composable
private fun PrinterCard(printer: Printer) {
    val cfg = statusConfig(printer.status)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Status dot
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(cfg.dot)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = printer.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = printer.ipAddress,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                    fontSize = 11.sp
                )
                if (printer.pendingJobsCount > 0) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "${printer.pendingJobsCount} job${if (printer.pendingJobsCount != 1) "s" else ""} in queue",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Status badge
            Surface(shape = MaterialTheme.shapes.small, color = cfg.bg) {
                Text(
                    text = cfg.label,
                    color = cfg.text,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }
        }

        // Completed jobs counter
        if (printer.completedJobsCount > 0) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "${printer.completedJobsCount} completed",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}
