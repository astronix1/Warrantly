package com.example.warrantly.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.warrantly.data.model.WarrantyStatus
import com.example.warrantly.domain.model.Product
import com.example.warrantly.domain.repository.ProductRepository
import com.example.warrantly.domain.usecase.WarrantyStatusUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

data class HomeUiState(
    val isLoading: Boolean = true,
    val totalProducts: Int = 0,
    val activeCount: Int = 0,
    val expiringSoonCount: Int = 0,
    val expiredCount: Int = 0,
    val upcomingExpiry: List<Product> = emptyList(),
    val recentProducts: List<Product> = emptyList(),
    val error: String? = null,
)

class HomeViewModel(
    private val productRepository: ProductRepository,
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = productRepository.observeAll()
        .map { products ->
            val statusCounts = products.groupBy { product ->
                WarrantyStatusUseCase.getStatus(product.warrantyEndDate)
            }

            val today = LocalDate.now()
            val in60Days = today.plusDays(60)

            val upcomingExpiry = products
                .filter { product ->
                    val endDate = product.warrantyEndDate ?: return@filter false
                    val localEnd = Instant.ofEpochMilli(endDate)
                        .atZone(ZoneOffset.UTC)
                        .toLocalDate()
                    val status = WarrantyStatusUseCase.getStatus(endDate)
                    // within next 60 days and not expired
                    status != WarrantyStatus.EXPIRED &&
                            !localEnd.isBefore(today) &&
                            !localEnd.isAfter(in60Days)
                }
                .sortedBy { it.warrantyEndDate }

            val recentProducts = products
                .sortedByDescending { it.createdAt }
                .take(5)

            HomeUiState(
                isLoading = false,
                totalProducts = products.size,
                activeCount = statusCounts[WarrantyStatus.ACTIVE]?.size ?: 0,
                expiringSoonCount = statusCounts[WarrantyStatus.EXPIRING_SOON]?.size ?: 0,
                expiredCount = statusCounts[WarrantyStatus.EXPIRED]?.size ?: 0,
                upcomingExpiry = upcomingExpiry,
                recentProducts = recentProducts,
                error = null,
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState(isLoading = true),
        )

    companion object {
        fun factory(productRepository: ProductRepository): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return HomeViewModel(productRepository) as T
                }
            }
        }
    }
}
