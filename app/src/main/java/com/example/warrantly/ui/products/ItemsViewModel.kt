package com.example.warrantly.ui.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.warrantly.data.model.ProductCategory
import com.example.warrantly.data.model.WarrantyStatus
import com.example.warrantly.domain.model.Product
import com.example.warrantly.domain.repository.ProductRepository
import com.example.warrantly.domain.usecase.WarrantyStatusUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

enum class SortOption { RECENT, NAME, EXPIRY_DATE, PURCHASE_DATE }
enum class FilterStatus { ALL, ACTIVE, EXPIRING_SOON, EXPIRED, NO_WARRANTY }

data class ItemsUiState(
    val isLoading: Boolean = true,
    val products: List<Product> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: ProductCategory? = null,
    val filterStatus: FilterStatus = FilterStatus.ALL,
    val sortOption: SortOption = SortOption.RECENT,
    val error: String? = null,
)

class ItemsViewModel(
    private val productRepository: ProductRepository,
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")
    private val selectedCategory = MutableStateFlow<ProductCategory?>(null)
    private val filterStatus = MutableStateFlow(FilterStatus.ALL)
    private val sortOption = MutableStateFlow(SortOption.RECENT)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<ItemsUiState> = combine(
        productRepository.observeAll(),
        searchQuery,
        selectedCategory,
        filterStatus,
        sortOption,
    ) { allProducts, query, category, status, sort ->
        val filtered = allProducts
            .filter { product ->
                // Search filter
                if (query.isBlank()) true
                else {
                    product.name.contains(query, ignoreCase = true) ||
                            product.brand.contains(query, ignoreCase = true) ||
                            product.modelNumber.contains(query, ignoreCase = true) ||
                            product.serialNumber.contains(query, ignoreCase = true)
                }
            }
            .filter { product ->
                // Category filter
                category == null || product.category == category
            }
            .filter { product ->
                // Status filter
                val warrantyStatus = WarrantyStatusUseCase.getStatus(product.warrantyEndDate)
                when (status) {
                    FilterStatus.ALL -> true
                    FilterStatus.ACTIVE -> warrantyStatus == WarrantyStatus.ACTIVE
                    FilterStatus.EXPIRING_SOON -> warrantyStatus == WarrantyStatus.EXPIRING_SOON
                    FilterStatus.EXPIRED -> warrantyStatus == WarrantyStatus.EXPIRED
                    FilterStatus.NO_WARRANTY -> warrantyStatus == WarrantyStatus.UNKNOWN
                }
            }
            .let { list ->
                // Sort
                when (sort) {
                    SortOption.RECENT -> list.sortedByDescending { it.createdAt }
                    SortOption.NAME -> list.sortedBy { it.name.lowercase() }
                    SortOption.EXPIRY_DATE -> list.sortedWith(
                        compareBy(nullsLast()) { it.warrantyEndDate }
                    )
                    SortOption.PURCHASE_DATE -> list.sortedByDescending { it.purchaseDate }
                }
            }

        ItemsUiState(
            isLoading = false,
            products = filtered,
            searchQuery = query,
            selectedCategory = category,
            filterStatus = status,
            sortOption = sort,
            error = null,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ItemsUiState(isLoading = true),
    )

    fun onSearchQueryChange(query: String) {
        searchQuery.value = query
    }

    fun onCategoryFilter(category: ProductCategory?) {
        selectedCategory.value = category
    }

    fun onStatusFilter(status: FilterStatus) {
        filterStatus.value = status
    }

    fun onSortChange(sort: SortOption) {
        sortOption.value = sort
    }

    fun resetFilters() {
        searchQuery.value = ""
        selectedCategory.value = null
        filterStatus.value = FilterStatus.ALL
        sortOption.value = SortOption.RECENT
    }

    companion object {
        fun factory(productRepository: ProductRepository): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ItemsViewModel(productRepository) as T
                }
            }
        }
    }
}
