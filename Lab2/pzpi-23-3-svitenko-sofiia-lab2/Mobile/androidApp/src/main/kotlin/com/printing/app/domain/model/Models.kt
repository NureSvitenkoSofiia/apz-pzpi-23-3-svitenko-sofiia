package com.printing.app.domain.model

data class PrintJob(
    val id: Int,
    val status: String,
    val materialColor: String,
    val materialType: String,
    val printerName: String,
    val estimatedMaterialGrams: Double,
    val estimatedMinutes: Double,
    val createdOn: String,
    val startedAt: String?,
    val completedAt: String?,
    val errorMessage: String?
)

data class Material(
    val id: Int,
    val color: String,
    val colorHex: String,
    val materialType: String,
    val availableOnPrinters: List<PrinterMaterialInfo>
)

data class PrinterMaterialInfo(
    val printerId: Int,
    val printerName: String,
    val quantityInG: Double
)

data class PrinterSimple(
    val id: Int,
    val name: String
)

data class Printer(
    val id: Int,
    val name: String,
    val status: String,
    val ipAddress: String,
    val pendingJobsCount: Int,
    val completedJobsCount: Int
)
