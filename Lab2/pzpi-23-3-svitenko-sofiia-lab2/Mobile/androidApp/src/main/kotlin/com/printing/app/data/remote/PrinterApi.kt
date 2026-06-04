package com.printing.app.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

// ── GET /api/admin/printers ───────────────────────────────────────────────────
// C#: PrinterManagementResponse → camelCase JSON
data class PrinterDto(
    val id: Int = 0,
    val name: String = "",
    val status: String = "",              // "idle" | "printing" | "offline"
    val ipAddress: String = "",
    val lastPing: String = "",
    val createdOn: String = "",
    val loadedMaterials: List<PrinterMaterialDetailsDto> = emptyList(),
    val pendingJobsCount: Int = 0,
    val completedJobsCount: Int = 0
)

// C#: PrinterMaterialDetails
data class PrinterMaterialDetailsDto(
    val materialId: Int = 0,
    val materialType: String = "",
    val color: String = "",
    val quantityInG: Double = 0.0
)

class PrinterApi(private val client: HttpClient) {
    suspend fun getPrinters(): List<PrinterDto> =
        client.get("/api/printer").body<ApiList<PrinterDto>>().data
}
