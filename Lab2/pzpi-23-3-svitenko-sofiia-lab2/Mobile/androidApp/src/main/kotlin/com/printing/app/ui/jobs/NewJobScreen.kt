package com.printing.app.ui.jobs

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.printing.app.R
import com.printing.app.domain.model.Material
import com.printing.app.domain.model.PrinterSimple

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewJobScreen(
    viewModel: JobViewModel,
    userId: Int,
    onBack: () -> Unit,
    onSubmitSuccess: () -> Unit
) {
    val materials by viewModel.materials.collectAsState()
    val printers by viewModel.printers.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val submitResult by viewModel.submitResult.collectAsState()

    var selectedMaterial by remember { mutableStateOf<Material?>(null) }
    var selectedPrinter by remember { mutableStateOf<PrinterSimple?>(null) }
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFileName by remember { mutableStateOf("") }

    var materialExpanded by remember { mutableStateOf(false) }
    var printerExpanded by remember { mutableStateOf(false) }

    val filePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            selectedFileUri = uri
            selectedFileName = uri.lastPathSegment ?: "model.stl"
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadFormData()
    }

    LaunchedEffect(submitResult) {
        if (submitResult == true) {
            viewModel.clearSubmitResult()
            onSubmitSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.new_job)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // STL file picker
            OutlinedButton(
                onClick = { filePicker.launch("*/*") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (selectedFileName.isEmpty()) stringResource(R.string.select_stl) else selectedFileName)
            }

            // Material dropdown
            ExposedDropdownMenuBox(
                expanded = materialExpanded,
                onExpandedChange = { materialExpanded = !materialExpanded }
            ) {
                OutlinedTextField(
                    value = selectedMaterial?.let { "${it.color} ${it.materialType}" } ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.select_material)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(materialExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                )
                ExposedDropdownMenu(
                    expanded = materialExpanded,
                    onDismissRequest = { materialExpanded = false }
                ) {
                    materials.forEach { material ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text("${material.color} ${material.materialType}")
                                    Text(
                                        "${material.availableOnPrinters.size} printer(s)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            },
                            onClick = {
                                selectedMaterial = material
                                selectedPrinter = null
                                materialExpanded = false
                            }
                        )
                    }
                }
            }

            // Printer dropdown (filtered by selected material)
            val availablePrinters = selectedMaterial?.availableOnPrinters
                ?.filter { it.quantityInG > 0 }
                ?.map { info -> PrinterSimple(info.printerId, info.printerName) }
                ?: printers

            ExposedDropdownMenuBox(
                expanded = printerExpanded,
                onExpandedChange = { printerExpanded = !printerExpanded }
            ) {
                OutlinedTextField(
                    value = selectedPrinter?.name ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.select_printer)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(printerExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                )
                ExposedDropdownMenu(
                    expanded = printerExpanded,
                    onDismissRequest = { printerExpanded = false }
                ) {
                    availablePrinters.forEach { printer ->
                        DropdownMenuItem(
                            text = { Text(printer.name) },
                            onClick = {
                                selectedPrinter = printer
                                printerExpanded = false
                            }
                        )
                    }
                }
            }

            if (submitResult == false) {
                Text(
                    "Submission failed. Check server connection.",
                    color = MaterialTheme.colorScheme.error
                )
            }

            Button(
                onClick = {
                    val uri = selectedFileUri ?: return@Button
                    val mat = selectedMaterial ?: return@Button
                    val printer = selectedPrinter ?: return@Button
                    viewModel.submitJob(userId, mat.id, printer.id, uri)
                },
                enabled = !isLoading
                        && selectedFileUri != null
                        && selectedMaterial != null
                        && selectedPrinter != null,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(stringResource(R.string.submit))
                }
            }
        }
    }
}
