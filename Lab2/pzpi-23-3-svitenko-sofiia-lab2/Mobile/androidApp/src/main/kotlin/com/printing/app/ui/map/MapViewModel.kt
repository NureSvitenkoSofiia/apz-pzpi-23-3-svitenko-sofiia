package com.printing.app.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.printing.app.data.repository.PrinterRepository
import com.printing.app.domain.model.Printer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MapViewModel(private val repo: PrinterRepository) : ViewModel() {
    private val _printers = MutableStateFlow<List<Printer>>(emptyList())
    val printers = _printers.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _isLoading.value = true
            _printers.value = repo.getPrinters()
            _isLoading.value = false
        }
    }
}
