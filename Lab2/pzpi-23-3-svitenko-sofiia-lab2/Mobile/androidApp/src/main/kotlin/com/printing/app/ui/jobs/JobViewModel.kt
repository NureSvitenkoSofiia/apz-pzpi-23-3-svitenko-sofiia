package com.printing.app.ui.jobs

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.printing.app.data.repository.PrintJobRepository
import com.printing.app.domain.model.Material
import com.printing.app.domain.model.PrintJob
import com.printing.app.domain.model.PrinterSimple
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class JobViewModel(private val repo: PrintJobRepository) : ViewModel() {

    private val _jobs = MutableStateFlow<List<PrintJob>>(emptyList())
    val jobs = _jobs.asStateFlow()

    private val _materials = MutableStateFlow<List<Material>>(emptyList())
    val materials = _materials.asStateFlow()

    private val _printers = MutableStateFlow<List<PrinterSimple>>(emptyList())
    val printers = _printers.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _submitResult = MutableStateFlow<Boolean?>(null)
    val submitResult = _submitResult.asStateFlow()

    fun loadJobs(userId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _jobs.value = repo.getMyJobs(userId)
            _isLoading.value = false
        }
    }

    fun loadFormData() {
        viewModelScope.launch {
            val mats = repo.getMaterials()
            _materials.value = mats
            _printers.value = repo.getPrintersFromMaterials(mats)
        }
    }

    fun submitJob(userId: Int, materialId: Int, printerId: Int, fileUri: Uri) {
        viewModelScope.launch {
            _isLoading.value = true
            _submitResult.value = repo.createJob(userId, materialId, printerId, fileUri)
            _isLoading.value = false
        }
    }

    fun clearSubmitResult() {
        _submitResult.value = null
    }
}
