package com.printing.app.ui.jobs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.printing.app.R
import com.printing.app.domain.model.PrintJob
import com.printing.app.ui.settings.SettingsSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobListScreen(
    viewModel: JobViewModel,
    userId: Int,
    onLogout: () -> Unit
) {
    val jobs by viewModel.jobs.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var showSettings by remember { mutableStateOf(false) }

    LaunchedEffect(userId) {
        viewModel.loadJobs(userId)
    }

    if (showSettings) {
        SettingsSheet(
            onDismiss = { showSettings = false },
            onLogout = onLogout
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.my_jobs)) },
                actions = {
                    IconButton(onClick = { showSettings = true }) {
                        Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.settings))
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                jobs.isEmpty() -> Text(
                    stringResource(R.string.no_jobs),
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.bodyLarge
                )
                else -> LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(jobs) { job -> JobCard(job) }
                }
            }
        }
    }
}

@Composable
private fun JobCard(job: PrintJob) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Job #${job.id}",
                    style = MaterialTheme.typography.titleMedium
                )
                StatusChip(job.status)
            }
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(R.string.material_label, job.materialColor, job.materialType),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                stringResource(R.string.printer_label, job.printerName),
                style = MaterialTheme.typography.bodyMedium
            )
            if (job.estimatedMinutes > 0) {
                Text(
                    "Est. time: ${job.estimatedMinutes.toInt()} min",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            if (job.errorMessage != null) {
                Text(
                    "Error: ${job.errorMessage}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun StatusChip(status: String) {
    val (label, color) = when (status.lowercase()) {
        "pending" -> stringResource(R.string.status_pending) to MaterialTheme.colorScheme.tertiary
        "printing" -> stringResource(R.string.status_printing) to MaterialTheme.colorScheme.primary
        "completed" -> stringResource(R.string.status_completed) to MaterialTheme.colorScheme.secondary
        "failed" -> stringResource(R.string.status_failed) to MaterialTheme.colorScheme.error
        else -> status to MaterialTheme.colorScheme.outline
    }
    Surface(
        shape = MaterialTheme.shapes.small,
        color = color.copy(alpha = 0.15f)
    ) {
        Text(
            text = label,
            color = color,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}
