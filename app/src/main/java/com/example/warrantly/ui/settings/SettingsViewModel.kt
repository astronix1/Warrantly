package com.example.warrantly.ui.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.warrantly.data.preferences.DataStoreManager
import com.example.warrantly.domain.repository.DocumentRepository
import com.example.warrantly.domain.repository.ProductRepository
import com.example.warrantly.notifications.NotificationScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
data class ExportedProduct(
    val id: Long,
    val name: String,
    val brand: String,
    val category: String,
    val modelNumber: String,
    val serialNumber: String,
    val purchaseDate: Long,
    val purchasePriceCents: Long?,
    val currencyCode: String,
    val warrantyStartDate: Long?,
    val warrantyEndDate: Long?,
    val warrantyDurationMonths: Int?,
    val seller: String,
    val notes: String,
    val reminderEnabled: Boolean,
)

@Serializable
data class ExportData(
    val app: String = "Warrantly",
    val version: String = "1.0",
    val exportDate: Long = System.currentTimeMillis(),
    val products: List<ExportedProduct>,
)

data class SettingsUiState(
    val themeMode: String = "SYSTEM",
    val globalRemindersEnabled: Boolean = true,
    val reminderDaysBefore: List<Int> = listOf(30, 7, 1),
    val expiringSoonThresholdDays: Int = 30,
    val defaultCurrencyCode: String = "USD",
    val appVersion: String = "1.0.0",
    val isExporting: Boolean = false,
    val message: String? = null,
)

class SettingsViewModel(
    private val dataStoreManager: DataStoreManager,
    private val productRepository: ProductRepository,
    private val notificationScheduler: NotificationScheduler,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            dataStoreManager.themeMode.collect { mode ->
                _uiState.update { it.copy(themeMode = mode) }
            }
        }
        viewModelScope.launch {
            dataStoreManager.globalRemindersEnabled.collect { enabled ->
                _uiState.update { it.copy(globalRemindersEnabled = enabled) }
            }
        }
        viewModelScope.launch {
            dataStoreManager.reminderDaysBefore.collect { days ->
                _uiState.update { it.copy(reminderDaysBefore = days) }
            }
        }
        viewModelScope.launch {
            dataStoreManager.expiringSoonThresholdDays.collect { threshold ->
                _uiState.update { it.copy(expiringSoonThresholdDays = threshold) }
            }
        }
        viewModelScope.launch {
            dataStoreManager.defaultCurrencyCode.collect { currency ->
                _uiState.update { it.copy(defaultCurrencyCode = currency) }
            }
        }
    }

    fun onThemeModeChange(mode: String) {
        viewModelScope.launch {
            dataStoreManager.setThemeMode(mode)
        }
    }

    fun onGlobalRemindersChange(enabled: Boolean) {
        viewModelScope.launch {
            dataStoreManager.setGlobalRemindersEnabled(enabled)
            if (!enabled) {
                notificationScheduler.cancelAll()
            } else {
                // Reschedule for all products with active warranty
                val products = productRepository.observeWithWarranty().first()
                val daysBefore = _uiState.value.reminderDaysBefore
                products.forEach { product ->
                    if (product.reminderEnabled) {
                        notificationScheduler.scheduleReminders(product, daysBefore)
                    }
                }
            }
        }
    }

    fun toggleReminderDay(day: Int) {
        viewModelScope.launch {
            val current = _uiState.value.reminderDaysBefore.toMutableList()
            if (current.contains(day)) {
                if (current.size > 1) { // keep at least 1
                    current.remove(day)
                }
            } else {
                current.add(day)
                current.sortDescending()
            }
            dataStoreManager.setReminderDaysBefore(current)

            // Reschedule
            val products = productRepository.observeWithWarranty().first()
            products.forEach { product ->
                if (product.reminderEnabled) {
                    notificationScheduler.scheduleReminders(product, current)
                }
            }
        }
    }

    fun onCurrencyCodeChange(code: String) {
        viewModelScope.launch {
            dataStoreManager.setDefaultCurrencyCode(code)
        }
    }

    fun exportData(context: Context, uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true) }
            try {
                val products = productRepository.observeAll().first()
                val exportList = products.map {
                    ExportedProduct(
                        id = it.id,
                        name = it.name,
                        brand = it.brand,
                        category = it.category.name,
                        modelNumber = it.modelNumber,
                        serialNumber = it.serialNumber,
                        purchaseDate = it.purchaseDate,
                        purchasePriceCents = it.purchasePriceCents,
                        currencyCode = it.currencyCode,
                        warrantyStartDate = it.warrantyStartDate,
                        warrantyEndDate = it.warrantyEndDate,
                        warrantyDurationMonths = it.warrantyDurationMonths,
                        seller = it.seller,
                        notes = it.notes,
                        reminderEnabled = it.reminderEnabled,
                    )
                }
                val exportData = ExportData(products = exportList)
                val jsonString = Json { prettyPrint = true }.encodeToString(exportData)

                withContext(Dispatchers.IO) {
                    context.contentResolver.openOutputStream(uri)?.use { os ->
                        os.write(jsonString.toByteArray())
                    }
                }
                _uiState.update {
                    it.copy(
                        isExporting = false,
                        message = "Exported ${products.size} products successfully"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isExporting = false,
                        message = "Export failed: ${e.message}"
                    )
                }
            }
        }
    }

    fun deleteAllData(context: Context) {
        viewModelScope.launch {
            // Cancel all notifications
            notificationScheduler.cancelAll()

            // Delete all files in documents dir
            withContext(Dispatchers.IO) {
                val docsDir = File(context.filesDir, "documents")
                if (docsDir.exists()) {
                    docsDir.deleteRecursively()
                }
            }

            // Clear database
            productRepository.deleteAll()

            _uiState.update { it.copy(message = "All data permanently deleted") }
        }
    }

    fun dismissMessage() {
        _uiState.update { it.copy(message = null) }
    }

    companion object {
        fun factory(
            dataStoreManager: DataStoreManager,
            productRepository: ProductRepository,
            notificationScheduler: NotificationScheduler,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SettingsViewModel(
                    dataStoreManager = dataStoreManager,
                    productRepository = productRepository,
                    notificationScheduler = notificationScheduler,
                ) as T
            }
        }
    }
}

