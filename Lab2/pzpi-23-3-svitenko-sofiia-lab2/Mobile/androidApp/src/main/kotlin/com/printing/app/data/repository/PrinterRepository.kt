package com.printing.app.data.repository

import android.util.Log
import com.printing.app.data.remote.PrinterApi
import com.printing.app.domain.model.Printer

private const val TAG = "PrinterRepo"

class PrinterRepository(private val api: PrinterApi) {
    suspend fun getPrinters(): List<Printer> {
        return try {
            val dtos = api.getPrinters()
            Log.d(TAG, "Fetched ${dtos.size} printers")
            dtos.map { dto ->
                Printer(
                    id = dto.id,
                    name = dto.name,
                    status = dto.status,
                    ipAddress = dto.ipAddress,
                    pendingJobsCount = dto.pendingJobsCount,
                    completedJobsCount = dto.completedJobsCount
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch printers: ${e.message}", e)
            emptyList()
        }
    }
}
