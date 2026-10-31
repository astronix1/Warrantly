package com.example.warrantly.ui.addproduct

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.warrantly.data.model.ProductCategory
import com.example.warrantly.data.preferences.DataStoreManager
import com.example.warrantly.domain.repository.DocumentRepository
import com.example.warrantly.domain.repository.ProductRepository
import com.example.warrantly.notifications.NotificationScheduler
import com.example.warrantly.ui.components.CategoryIcon
import com.example.warrantly.util.formatDate

@Composable
fun EditProductScreen(
    productId: Long,
    productRepository: ProductRepository,
    documentRepository: DocumentRepository,
    notificationScheduler: NotificationScheduler,
    dataStoreManager: DataStoreManager,
    onProductSaved: () -> Unit,
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AddProductScreen(
        productRepository = productRepository,
        documentRepository = documentRepository,
        notificationScheduler = notificationScheduler,
        dataStoreManager = dataStoreManager,

        onProductSaved = onProductSaved,
        onNavigateUp = onNavigateUp,
        existingProductId = productId,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(
    productRepository: ProductRepository,
    documentRepository: DocumentRepository,
    notificationScheduler: NotificationScheduler,
    dataStoreManager: DataStoreManager,
    onProductSaved: () -> Unit,
    onNavigateUp: () -> Unit,
    existingProductId: Long? = null,
    modifier: Modifier = Modifier,
) {


    val viewModel: AddProductViewModel = viewModel(
        key = "add_product_${existingProductId ?: "new"}",
        factory = AddProductViewModel.factory(
            productRepository = productRepository,
            notificationScheduler = notificationScheduler,
            documentRepository = documentRepository,
            dataStoreManager = dataStoreManager,
            existingProductId = existingProductId,
        )
    )

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onProductSaved()
        }
    }

    LaunchedEffect(uiState.generalError) {
        uiState.generalError?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let { viewModel.onImageUriChange(it.toString()) }
    }

    // Date picker dialog states
    var showPurchaseDatePicker by remember { mutableStateOf(false) }
    var showWarrantyStartDatePicker by remember { mutableStateOf(false) }
    var showWarrantyEndDatePicker by remember { mutableStateOf(false) }

    val context = LocalContext.current

    val docPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { viewModel.onDocumentSelected(it) }
    }


    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.isEditing) "Edit Product" else "Add Product") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(24.dp)
                                .padding(end = 16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        IconButton(onClick = { viewModel.save(context) }) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Save",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Section 1: Basic Information
            FormSectionCard(title = "Basic Information") {
                // Product Image Picker
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (uiState.imageUri.isNotBlank()) {
                        AsyncImage(
                            model = uiState.imageUri,
                            contentDescription = "Product image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        IconButton(
                            onClick = { viewModel.onImageUriChange("") },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .background(
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                    RoundedCornerShape(50)
                                )
                        ) {
                            Icon(Icons.Default.Clear, contentDescription = "Remove photo", modifier = Modifier.size(20.dp))
                        }
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = null,
                                modifier = Modifier.size(36.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Add Product Photo",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = viewModel::onNameChange,
                    label = { Text("Product Name *") },
                    isError = uiState.nameError != null,
                    supportingText = uiState.nameError?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = uiState.brand,
                    onValueChange = viewModel::onBrandChange,
                    label = { Text("Brand / Manufacturer") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ProductCategory.entries.forEach { category ->
                        FilterChip(
                            selected = uiState.category == category,
                            onClick = { viewModel.onCategoryChange(category) },
                            label = { Text(category.displayName) },
                            leadingIcon = {
                                CategoryIcon(
                                    category = category,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.modelNumber,
                        onValueChange = viewModel::onModelNumberChange,
                        label = { Text("Model Number") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = uiState.serialNumber,
                        onValueChange = viewModel::onSerialNumberChange,
                        label = { Text("Serial Number") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }

            // Section 2: Purchase Information
            FormSectionCard(title = "Purchase Details") {
                // Purchase Date
                OutlinedTextField(
                    value = uiState.purchaseDate?.formatDate() ?: "Select date",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Purchase Date *") },
                    trailingIcon = {
                        IconButton(onClick = { showPurchaseDatePicker = true }) {
                            Icon(Icons.Default.CalendarToday, contentDescription = "Pick date")
                        }
                    },
                    isError = uiState.purchaseDateError != null,
                    supportingText = uiState.purchaseDateError?.let { { Text(it) } },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showPurchaseDatePicker = true }
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.purchasePriceText,
                        onValueChange = viewModel::onPurchasePriceChange,
                        label = { Text("Price (e.g. 299.99)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1.5f),
                        singleLine = true,
                        isError = uiState.priceError != null,
                        supportingText = uiState.priceError?.let { { Text(it) } }
                    )

                    OutlinedTextField(
                        value = uiState.currencyCode,
                        onValueChange = viewModel::onCurrencyCodeChange,
                        label = { Text("Currency") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = uiState.seller,
                    onValueChange = viewModel::onSellerChange,
                    label = { Text("Seller / Retailer (e.g. Best Buy, Amazon)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            // Section 3: Warranty Information
            FormSectionCard(title = "Warranty Coverage") {
                Text(
                    text = "Specify duration or pick an end date. Leave blank if unknown.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Warranty duration quick buttons
                Text(
                    text = "Warranty Duration",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val durations = listOf(
                        6 to "6 Months",
                        12 to "1 Year",
                        24 to "2 Years",
                        36 to "3 Years",
                        60 to "5 Years"
                    )
                    durations.forEach { (months, label) ->
                        FilterChip(
                            selected = uiState.warrantyDurationMonths == months,
                            onClick = {
                                if (uiState.warrantyDurationMonths == months) {
                                    viewModel.onWarrantyDurationMonthsChange(null)
                                } else {
                                    viewModel.onWarrantyDurationMonthsChange(months)
                                }
                            },
                            label = { Text(label) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.warrantyStartDate?.formatDate() ?: "Not set",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Start Date") },
                        trailingIcon = {
                            IconButton(onClick = { showWarrantyStartDatePicker = true }) {
                                Icon(Icons.Default.CalendarToday, contentDescription = "Pick start date")
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = uiState.warrantyEndDate?.formatDate() ?: "Not set",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("End Date") },
                        trailingIcon = {
                            IconButton(onClick = { showWarrantyEndDatePicker = true }) {
                                Icon(Icons.Default.CalendarToday, contentDescription = "Pick end date")
                            }
                        },
                        isError = uiState.warrantyDateError != null,
                        supportingText = uiState.warrantyDateError?.let { { Text(it) } },
                        modifier = Modifier.weight(1f)
                    )
                }

                if (uiState.warrantyEndDate != null) {
                    TextButton(
                        onClick = { viewModel.onWarrantyEndDateChange(null) },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Clear warranty")
                    }
                }
            }

            FormSectionCard(title = "Documents") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Attach receipts & invoices",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TextButton(onClick = {
                        docPickerLauncher.launch(arrayOf("application/pdf", "image/*"))
                    }) {
                        Text("Browse")
                    }
                }

                // Show selected documents
                uiState.selectedDocumentUris.forEach { uri ->
                    val fileName = com.example.warrantly.util.getDisplayNameFromUri(context, uri) ?: "Document"
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = fileName,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { viewModel.onDocumentRemoved(uri) }) {
                            Icon(Icons.Default.Clear, contentDescription = "Remove document")
                        }
                    }
                }
            }

            // Section 4: Reminders & Notes
            FormSectionCard(title = "Reminders & Notes") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column {
                            Text(
                                text = "Expiry Reminders",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Get notified before warranty expires",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                    Switch(
                        checked = uiState.reminderEnabled,
                        onCheckedChange = viewModel::onReminderEnabledChange
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = uiState.notes,
                    onValueChange = viewModel::onNotesChange,
                    label = { Text("Notes (service info, conditions, etc.)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )
            }

            Button(
                onClick = { viewModel.save(context) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = !uiState.isSaving
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(if (uiState.isEditing) "Save Changes" else "Add Product")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Purchase Date Picker
    if (showPurchaseDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.purchaseDate ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showPurchaseDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        viewModel.onPurchaseDateChange(it)
                    }
                    showPurchaseDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPurchaseDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Warranty Start Date Picker
    if (showWarrantyStartDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.warrantyStartDate ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showWarrantyStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        viewModel.onWarrantyStartDateChange(it)
                    }
                    showWarrantyStartDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showWarrantyStartDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Warranty End Date Picker
    if (showWarrantyEndDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.warrantyEndDate ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showWarrantyEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        viewModel.onWarrantyEndDateChange(it)
                    }
                    showWarrantyEndDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showWarrantyEndDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun FormSectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

