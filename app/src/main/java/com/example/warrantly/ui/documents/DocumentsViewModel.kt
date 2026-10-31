package com.example.warrantly.ui.documents

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.warrantly.domain.model.Document
import com.example.warrantly.domain.model.Product
import com.example.warrantly.domain.repository.DocumentRepository
import com.example.warrantly.domain.repository.ProductRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DocumentsUiState(
    val isLoading: Boolean = true,
    val documents: List<Pair<Document, Product?>> = emptyList(),
    val searchQuery: String = "",
    val error: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class DocumentsViewModel(
    private val documentRepository: DocumentRepository,
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")

    val uiState: StateFlow<DocumentsUiState> = combine(
        documentRepository.observeAll(),
        productRepository.observeAll(),
        _searchQuery
    ) { documents, products, query ->
        val productMap = products.associateBy { it.id }

        val paired = documents.map { doc ->
            doc to productMap[doc.productId]
        }

        val filtered = if (query.isBlank()) {
            paired
        } else {
            val lower = query.trim().lowercase()
            paired.filter { (doc, product) ->
                doc.displayName.lowercase().contains(lower) ||
                    product?.name?.lowercase()?.contains(lower) == true
            }
        }

        DocumentsUiState(
            isLoading = false,
            documents = filtered,
            searchQuery = query,
            error = null
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DocumentsUiState(isLoading = true)
    )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun deleteDocument(document: Document) {
        viewModelScope.launch {
            try {
                documentRepository.delete(document)
            } catch (e: Exception) {
                // Error handled via uiState in future enhancement
            }
        }
    }
}
