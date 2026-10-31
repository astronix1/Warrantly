package com.example.warrantly.ui.addproduct

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.warrantly.data.model.ProductCategory
import com.example.warrantly.data.preferences.DataStoreManager
import com.example.warrantly.domain.model.Document
import com.example.warrantly.domain.model.Product
import com.example.warrantly.domain.repository.DocumentRepository
import com.example.warrantly.domain.repository.ProductRepository
import com.example.warrantly.domain.usecase.ProductFormValidator
import com.example.warrantly.notifications.NotificationScheduler
import com.example.warrantly.util.copyDocumentToPrivateStorage
import com.example.warrantly.util.getDisplayNameFromUri
import com.example.warrantly.util.getFileSizeFromUri
import com.example.warrantly.util.getMimeTypeFromUri
import com.example.warrantly.util.toEpochMillis
import com.example.warrantly.util.toLocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class AddProductUiState(
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val name: String = "",
    val brand: String = "",
    val category: ProductCategory = ProductCategory.OTHER,
    val modelNumber: String = "",
    val serialNumber: String = "",
    val purchaseDate: Long? = LocalDate.now().toEpochMillis(),
    val purchasePriceText: String = "",
    val currencyCode: String = "USD",
    val warrantyStartDate: Long? = LocalDate.now().toEpochMillis(),
    val warrantyEndDate: Long? = null,
    val warrantyDurationMonths: Int? = null,
    val seller: String = "",
    val imageUri: String = "",
    val notes: String = "",
    val reminderEnabled: Boolean = true,
    // Temporary list for documents added during product creation
    val selectedDocumentUris: List<Uri> = emptyList(),
    // Validation errors
    val nameError: String? = null,
    val purchaseDateError: String? = null,
    val warrantyDateError: String? = null,
    val priceError: String? = null,
    val generalError: String? = null,
    val savedProductId: Long? = null,
)

class AddProductViewModel(
    private val productRepository: ProductRepository,
    private val documentRepository: DocumentRepository,
    private val notificationScheduler: NotificationScheduler,
    private val dataStoreManager: DataStoreManager,
    private val existingProductId: Long? = null,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddProductUiState(isEditing = existingProductId != null))
    val uiState: StateFlow<AddProductUiState> = _uiState.asStateFlow()

    init {
        // Load default currency code from DataStore
        viewModelScope.launch {
            val defaultCurrency = dataStoreManager.defaultCurrencyCode.first()
            _uiState.update { it.copy(currencyCode = defaultCurrency) }
        }

        // If editing, load existing product
        if (existingProductId != null) {
            viewModelScope.launch {
                val product = productRepository.getById(existingProductId)
                if (product != null) {
                    val priceText = product.purchasePriceCents?.let {
                        String.format("%.2f", it / 100.0)
                    } ?: ""
                    _uiState.update {
                        it.copy(
                            isEditing = true,
                            name = product.name,
                            brand = product.brand,
                            category = product.category,
                            modelNumber = product.modelNumber,
                            serialNumber = product.serialNumber,
                            purchaseDate = product.purchaseDate,
                            purchasePriceText = priceText,
                            currencyCode = product.currencyCode,
                            warrantyStartDate = product.warrantyStartDate,
                            warrantyEndDate = product.warrantyEndDate,
                            warrantyDurationMonths = product.warrantyDurationMonths,
                            seller = product.seller,
                            imageUri = product.imageUri,
                            notes = product.notes,
                            reminderEnabled = product.reminderEnabled,
                        )
                    }
                }
            }
        }
    }

    // Existing update functions...
    fun onNameChange(value: String) { _uiState.update { it.copy(name = value, nameError = null) } }
    fun onBrandChange(value: String) { _uiState.update { it.copy(brand = value) } }
    fun onCategoryChange(value: ProductCategory) { _uiState.update { it.copy(category = value) } }
    fun onModelNumberChange(value: String) { _uiState.update { it.copy(modelNumber = value) } }
    fun onSerialNumberChange(value: String) { _uiState.update { it.copy(serialNumber = value) } }
    fun onPurchasePriceChange(value: String) { _uiState.update { it.copy(purchasePriceText = value, priceError = null) } }
    fun onCurrencyCodeChange(value: String) { _uiState.update { it.copy(currencyCode = value) } }
    fun onSellerChange(value: String) { _uiState.update { it.copy(seller = value) } }
    fun onImageUriChange(value: String) { _uiState.update { it.copy(imageUri = value) } }
    fun onNotesChange(value: String) { _uiState.update { it.copy(notes = value) } }
    fun onReminderEnabledChange(value: Boolean) { _uiState.update { it.copy(reminderEnabled = value) } }

    fun onPurchaseDateChange(value: Long?) {
        _uiState.update {
            it.copy(
                purchaseDate = value,
                purchaseDateError = null,
                warrantyStartDate = it.warrantyStartDate ?: value
            )
        }
        recalculateEndDate()
    }

    fun onWarrantyStartDateChange(value: Long?) {
        _uiState.update { it.copy(warrantyStartDate = value, warrantyDateError = null) }
        recalculateEndDate()
    }

    fun onWarrantyEndDateChange(value: Long?) {
        _uiState.update { it.copy(warrantyEndDate = value, warrantyDurationMonths = null, warrantyDateError = null) }
    }

    fun onWarrantyDurationMonthsChange(months: Int?) {
        _uiState.update { it.copy(warrantyDurationMonths = months) }
        recalculateEndDate()
    }

    private fun recalculateEndDate() {
        val state = _uiState.value
        val months = state.warrantyDurationMonths
        val startDate = state.warrantyStartDate ?: state.purchaseDate
        if (months != null && startDate != null) {
            val localStart = startDate.toLocalDate()
            val calculatedEnd = localStart.plusMonths(months.toLong()).toEpochMillis()
            _uiState.update { it.copy(warrantyEndDate = calculatedEnd) }
        }
    }

    // New Document functions
    fun onDocumentSelected(uri: Uri) {
        _uiState.update { it.copy(selectedDocumentUris = it.selectedDocumentUris + uri) }
    }

    fun onDocumentRemoved(uri: Uri) {
        _uiState.update { it.copy(selectedDocumentUris = it.selectedDocumentUris - uri) }
    }

    // Pass context to process documents
    fun save(context: Context) {
        val state = _uiState.value
        val nameErr = ProductFormValidator.validateName(state.name)
        val dateErr = ProductFormValidator.validatePurchaseDate(state.purchaseDate)
        val warrantyErr = ProductFormValidator.validateWarrantyDates(
            state.warrantyStartDate,
            state.warrantyEndDate,
            state.purchaseDate
        )
        val (priceCents, priceErr) = ProductFormValidator.validatePrice(state.purchasePriceText)

        if (nameErr != null || dateErr != null || warrantyErr != null || priceErr != null) {
            _uiState.update {
                it.copy(nameError = nameErr, purchaseDateError = dateErr, warrantyDateError = warrantyErr, priceError = priceErr)
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            try {
                val now = System.currentTimeMillis()
                val product = Product(
                    id = existingProductId ?: 0L,
                    name = state.name.trim(),
                    brand = state.brand.trim(),
                    category = state.category,
                    modelNumber = state.modelNumber.trim(),
                    serialNumber = state.serialNumber.trim(),
                    purchaseDate = state.purchaseDate ?: now,
                    purchasePriceCents = priceCents,
                    currencyCode = state.currencyCode,
                    warrantyStartDate = state.warrantyStartDate,
                    warrantyEndDate = state.warrantyEndDate,
                    warrantyDurationMonths = state.warrantyDurationMonths,
                    seller = state.seller.trim(),
                    imageUri = state.imageUri,
                    notes = state.notes.trim(),
                    reminderEnabled = state.reminderEnabled,
                    createdAt = now,
                    updatedAt = now,
                )

                val id = if (existingProductId != null) {
                    productRepository.update(product)
                    existingProductId
                } else {
                    productRepository.insert(product)
                }

                // Process documents immediately after generating the product ID
                state.selectedDocumentUris.forEach { uri ->
                    val displayName = getDisplayNameFromUri(context, uri) ?: uri.lastPathSegment ?: "document"
                    val mimeType = getMimeTypeFromUri(context, uri) ?: "application/octet-stream"
                    val fileSizeBytes = getFileSizeFromUri(context, uri)

                    val relativePath = copyDocumentToPrivateStorage(context, uri, displayName)
                    if (relativePath != null) {
                        val document = Document(
                            id = 0L,
                            productId = id,
                            displayName = displayName,
                            mimeType = mimeType,
                            relativePath = relativePath,
                            fileSizeBytes = fileSizeBytes,
                            addedAt = System.currentTimeMillis()
                        )
                        documentRepository.insert(document)
                    }
                }

                // Schedule or cancel reminders
                if (product.reminderEnabled && product.warrantyEndDate != null) {
                    val daysBefore = dataStoreManager.reminderDaysBefore.first()
                    notificationScheduler.scheduleReminders(product.copy(id = id), daysBefore)
                } else {
                    notificationScheduler.cancelReminders(id)
                }

                _uiState.update { it.copy(isSaving = false, isSaved = true, savedProductId = id) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, generalError = e.message ?: "Failed to save product") }
            }
        }
    }

    companion object {
        fun factory(
            productRepository: ProductRepository,
            documentRepository: DocumentRepository, // Added to factory
            notificationScheduler: NotificationScheduler,
            dataStoreManager: DataStoreManager,
            existingProductId: Long? = null,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AddProductViewModel(
                    productRepository = productRepository,
                    documentRepository = documentRepository,
                    notificationScheduler = notificationScheduler,
                    dataStoreManager = dataStoreManager,
                    existingProductId = existingProductId,
                ) as T
            }
        }
    }
}