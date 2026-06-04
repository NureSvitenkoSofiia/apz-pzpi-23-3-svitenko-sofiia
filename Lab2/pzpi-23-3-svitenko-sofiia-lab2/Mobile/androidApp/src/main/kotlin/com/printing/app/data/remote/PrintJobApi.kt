package com.printing.app.data.remote

import android.net.Uri
import android.content.Context
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.append
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders

// ── BaseResponse wrapper ──────────────────────────────────────────────────────
// All list endpoints return: { "data": [...] }
// ASP.NET Core default → camelCase → "data" (from C# "Data")
data class ApiList<T>(val data: List<T> = emptyList())

// ── GET /api/printjobs/materials ──────────────────────────────────────────────
// C#: AvailableMaterialResponse  → JSON camelCase fields below
data class AvailableMaterialDto(
    val id: Int = 0,
    val color: String = "",
    val colorHex: String = "",          // "#RRGGBB"
    val materialType: String = "",
    val densityInGramsPerCm3: Double = 0.0,
    val diameterMm: Double = 0.0,
    val availableOnPrinters: List<PrinterMaterialInfoDto> = emptyList()
)

// C#: PrinterMaterialInfo
data class PrinterMaterialInfoDto(
    val printerId: Int = 0,
    val printerName: String = "",
    val quantityInG: Double = 0.0
)

// ── GET /api/users/{userId}/jobs ──────────────────────────────────────────────
// C#: PrintJobResponse
data class PrintJobDto(
    val id: Int = 0,
    val stlFilePath: String = "",
    val gCodeFilePath: String? = null,
    val requiredMaterial: MaterialInfoDto = MaterialInfoDto(),
    val estimatedMaterialInGrams: Double = 0.0,
    val actualMaterialInGrams: Double = 0.0,
    val estimatedPrintTimeMinutes: Double = 0.0,
    val printer: PrinterInfoDto = PrinterInfoDto(),
    val status: String = "",
    val errorMessage: String? = null,
    val createdOn: String = "",
    val startedAt: String? = null,
    val completedAt: String? = null
)

// C#: MaterialInfo
data class MaterialInfoDto(
    val id: Int = 0,
    val color: String = "",
    val materialType: String = ""
)

// C#: PrinterInfo
data class PrinterInfoDto(
    val id: Int = 0,
    val name: String = "",
    val status: String = ""
)

class PrintJobApi(private val client: HttpClient, private val context: Context) {

    suspend fun getAvailableMaterials(): List<AvailableMaterialDto> =
        client.get("/api/printjobs/materials").body<ApiList<AvailableMaterialDto>>().data

    suspend fun getMyJobs(userId: Int): List<PrintJobDto> =
        client.get("/api/users/$userId/jobs").body<ApiList<PrintJobDto>>().data

    suspend fun createJob(userId: Int, materialId: Int, printerId: Int, fileUri: Uri): Boolean {
        val inputStream = context.contentResolver.openInputStream(fileUri) ?: return false
        val fileBytes = inputStream.readBytes()
        inputStream.close()

        val fileName = context.contentResolver.query(fileUri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            cursor.moveToFirst()
            if (nameIndex >= 0) cursor.getString(nameIndex) else "model.stl"
        } ?: "model.stl"

        // POST /api/printjobs form fields (C# [FromForm]):
        // userId, printerId, requiredMaterialId, stlFile
        val response = client.post("/api/printjobs") {
            setBody(MultiPartFormDataContent(formData {
                append("userId", userId.toString())
                append("requiredMaterialId", materialId.toString())
                append("printerId", printerId.toString())
                append("stlFile", fileBytes, Headers.build {
                    append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                    append(HttpHeaders.ContentType, "application/octet-stream")
                })
            }))
        }
        return response.status.value in 200..299
    }
}
