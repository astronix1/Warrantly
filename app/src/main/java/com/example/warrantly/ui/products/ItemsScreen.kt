package com.example.warrantly.ui.products

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.warrantly.data.model.ProductCategory
import com.example.warrantly.domain.repository.ProductRepository
import com.example.warrantly.ui.components.CategoryIcon
import com.example.warrantly.ui.components.ProductCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemsScreen(
    productRepository: ProductRepository,
    onProductClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: ItemsViewModel = viewModel(
        factory = ItemsViewModel.factory(productRepository)
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var searchVisible by remember { mutableStateOf(false) }
    var sortMenuExpanded by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize()) {
        // Top App Bar
        TopAppBar(
            title = {
                AnimatedVisibility(visible = !searchVisible, enter = fadeIn(), exit = fadeOut()) {
                    Text(
                        text = "Items",
                        fontWeight = FontWeight.Bold,
                    )
                }
                AnimatedVisibility(visible = searchVisible, enter = fadeIn(), exit = fadeOut()) {
                    TextField(
                        value = uiState.searchQuery,
                        onValueChange = viewModel::onSearchQueryChange,
                        placeholder = { Text("Search products...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                        ),
                    )
                }
            },
            actions = {
                IconButton(onClick = {
                    searchVisible = !searchVisible
                    if (!searchVisible) viewModel.onSearchQueryChange("")
                }) {
                    Icon(
                        imageVector = if (searchVisible) Icons.Filled.Close else Icons.Filled.Search,
                        contentDescription = if (searchVisible) "Close search" else "Search",
                    )
                }
                Box {
                    IconButton(onClick = { sortMenuExpanded = true }) {
                        Icon(
                            imageVector = Icons.Filled.Sort,
                            contentDescription = "Sort",
                        )
                    }
                    DropdownMenu(
                        expanded = sortMenuExpanded,
                        onDismissRequest = { sortMenuExpanded = false },
                    ) {
                        SortOption.entries.forEach { sort ->
                            DropdownMenuItem(
                                text = { Text(sort.displayName()) },
                                onClick = {
                                    viewModel.onSortChange(sort)
                                    sortMenuExpanded = false
                                },
                                trailingIcon = if (uiState.sortOption == sort) {
                                    { Text("✓", color = MaterialTheme.colorScheme.primary) }
                                } else null,
                            )
                        }
                    }
                }
            },
        )

        // Status filter chips
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(FilterStatus.entries) { filter ->
                FilterChip(
                    selected = uiState.filterStatus == filter,
                    onClick = { viewModel.onStatusFilter(filter) },
                    label = { Text(filter.displayName()) },
                )
            }
        }

        // Category filter chips
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                FilterChip(
                    selected = uiState.selectedCategory == null,
                    onClick = { viewModel.onCategoryFilter(null) },
                    label = { Text("All") },
                )
            }
            items(ProductCategory.entries) { category ->
                FilterChip(
                    selected = uiState.selectedCategory == category,
                    onClick = { viewModel.onCategoryFilter(category) },
                    label = { Text(category.displayName) },
                    leadingIcon = {
                        CategoryIcon(
                            category = category,
                            modifier = Modifier.padding(end = 2.dp),
                        )
                    },
                )
            }
        }

        // Content
        when {
            uiState.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            uiState.products.isEmpty() -> {
                ItemsEmptyState(
                    hasFilters = uiState.searchQuery.isNotEmpty() ||
                            uiState.selectedCategory != null ||
                            uiState.filterStatus != FilterStatus.ALL,
                    onResetFilters = viewModel::resetFilters,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 88.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    item {
                        Text(
                            text = "${uiState.products.size} ${if (uiState.products.size == 1) "product" else "products"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        )
                    }
                    items(uiState.products, key = { it.id }) { product ->
                        ProductCard(
                            product = product,
                            onClick = { onProductClick(product.id) },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ItemsEmptyState(
    hasFilters: Boolean,
    onResetFilters: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = if (hasFilters) "No matching products" else "No products yet",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (hasFilters) {
                "Try adjusting your filters or search query."
            } else {
                "Tap the + button to add your first product."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        if (hasFilters) {
            Spacer(modifier = Modifier.height(16.dp))
            androidx.compose.material3.TextButton(onClick = onResetFilters) {
                Text("Reset Filters")
            }
        }
    }
}

private fun SortOption.displayName(): String = when (this) {
    SortOption.RECENT -> "Recently Added"
    SortOption.NAME -> "Name (A–Z)"
    SortOption.EXPIRY_DATE -> "Expiry Date"
    SortOption.PURCHASE_DATE -> "Purchase Date"
}

private fun FilterStatus.displayName(): String = when (this) {
    FilterStatus.ALL -> "All"
    FilterStatus.ACTIVE -> "Active"
    FilterStatus.EXPIRING_SOON -> "Expiring Soon"
    FilterStatus.EXPIRED -> "Expired"
    FilterStatus.NO_WARRANTY -> "No Warranty"
}
