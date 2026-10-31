package com.example.warrantly.ui.productdetail

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.warrantly.data.model.WarrantyStatus
import com.example.warrantly.domain.model.Document
import com.example.warrantly.domain.model.Product
import com.example.warrantly.domain.repository.DocumentRepository
import com.example.warrantly.domain.repository.ProductRepository
import com.example.warrantly.domain.usecase.WarrantyStatusUseCase
import com.example.warrantly.notifications.NotificationScheduler
import com.example.warrantly.util.deleteDocumentFile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProductDetailUiState(
    val isLoading: Boolean = true,
    val product: Product? = null,
    val documents: List<Document> = emptyList(),
    val warrantyStatus: WarrantyStatus = WarrantyStatus.UNKNOWN,
    val timeRemainingText: String = "",
    val error: String? = null,
    val isDeleted: Boolean = false,
)

class ProductDetailViewModel(
    private val productId: Long,
    private val productRepository: ProductRepository,
    private val documentRepository: DocumentRepository,
    private val notificationScheduler: NotificationScheduler,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductDetailUiState())
    val uiState: StateFlow<ProductDetailUiState> = _uiState.asStateFlow()

    init {
        loadProductAndDocuments()
    }

    private fun loadProductAndDocuments() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val product = productRepository.getById(productId)
            if (product == null) {
                _uiState.update { it.copy(isLoading = false, error = "Product not found") }
                return@launch
            }

            documentRepository.observeForProduct(productId).collect { docs ->
                val status = WarrantyStatusUseCase.getStatus(product.warrantyEndDate)
                val timeRemaining = WarrantyStatusUseCase.getTimeRemainingText(product.warrantyEndDate)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        product = product,
                        documents = docs,
                        warrantyStatus = status,
                        timeRemainingText = timeRemaining,
                        error = null,
                    )
                }
            }
        }
    }

    fun deleteProduct(context: Context, onDeleted: () -> Unit) {
        viewModelScope.launch {
            val currentState = _uiState.value
            val currentProduct = currentState.product ?: return@launch

            // Delete associated documents and their physical files
            currentState.documents.forEach { doc ->
                deleteDocumentFile(context, doc.relativePath)
                documentRepository.delete(doc)
            }

            // Cancel scheduled notifications for this product
            notificationScheduler.cancelReminders(productId)

            // Delete product
            productRepository.delete(currentProduct)
            _uiState.update { it.copy(isDeleted = true) }
            onDeleted()
        }
    }

    fun deleteDocument(context: Context, document: Document) {
        viewModelScope.launch {
            deleteDocumentFile(context, document.relativePath)
            documentRepository.delete(document)
        }
    }

    companion object {
        fun factory(
            productId: Long,
            productRepository: ProductRepository,
            documentRepository: DocumentRepository,
            notificationScheduler: NotificationScheduler,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ProductDetailViewModel(
                    productId = productId,
                    productRepository = productRepository,
                    documentRepository = documentRepository,
                    notificationScheduler = notificationScheduler,
                ) as T
            }
        }
    }
}

