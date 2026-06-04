package com.printing.app.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.printing.app.data.remote.PrintJobApi
import com.printing.app.domain.model.Material
import com.printing.app.domain.model.PrintJob
import com.printing.app.domain.model.PrinterMaterialInfo
import com.printing.app.domain.model.PrinterSimple

private const val TAG = "PrintJobRepo"

class PrintJobRepository(private val api: PrintJobApi, context: Context) {
    private val cache = context.getSharedPreferences("job_cache", Context.MODE_PRIVATE)

    suspend fun getMyJobs(userId: Int): List<PrintJob> {
        return try {
            val dtos = api.getMyJobs(userId)
            Log.d(TAG, "Fetched ${dtos.size} jobs for user $userId")
            val jobs = dtos.map { dto ->
                PrintJob(
                    id = dto.id,
                    status = dto.status,
                    materialColor = dto.requiredMaterial.color,
                    materialType = dto.requiredMaterial.materialType,
                    printerName = dto.printer.name,
                    estimatedMaterialGrams = dto.estimatedMaterialInGrams,
                    estimatedMinutes = dto.estimatedPrintTimeMinutes,
                    createdOn = dto.createdOn,
                    startedAt = dto.startedAt,
                    completedAt = dto.completedAt,
                    errorMessage = dto.errorMessage
                )
            }
            cache.edit().putInt("lastCount", jobs.size).apply()
            jobs
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch jobs: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun getMaterials(): List<Material> {
        return try {
            val dtos = api.getAvailableMaterials()
            Log.d(TAG, "Fetched ${dtos.size} materials")
            dtos.map { dto ->
                Material(
                    id = dto.id,
                    color = dto.color,
                    colorHex = dto.colorHex,
                    materialType = dto.materialType,
                    availableOnPrinters = dto.availableOnPrinters.map { pm ->
                        PrinterMaterialInfo(
                            printerId = pm.printerId,
                            printerName = pm.printerName,
                            quantityInG = pm.quantityInG
                        )
                    }
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch materials: ${e.message}", e)
            emptyList()
        }
    }

    fun getPrintersFromMaterials(materials: List<Material>): List<PrinterSimple> =
        materials
            .flatMap { it.availableOnPrinters }
            .distinctBy { it.printerId }
            .map { PrinterSimple(it.printerId, it.printerName) }

    suspend fun createJob(userId: Int, materialId: Int, printerId: Int, fileUri: Uri): Boolean {
        return try {
            api.createJob(userId, materialId, printerId, fileUri)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create job: ${e.message}", e)
            false
        }
    }
}
