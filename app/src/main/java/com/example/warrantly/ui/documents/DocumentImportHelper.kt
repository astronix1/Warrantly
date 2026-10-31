package com.example.warrantly.ui.documents

import android.content.Context
import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.example.warrantly.domain.model.Document
import com.example.warrantly.domain.repository.DocumentRepository
import com.example.warrantly.util.copyDocumentToPrivateStorage
import com.example.warrantly.util.getDisplayNameFromUri
import com.example.warrantly.util.getFileSizeFromUri
import com.example.warrantly.util.getMimeTypeFromUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Composable helper that provides a launcher for importing a document for a product.
 *
 * After the user picks a file, it is copied to private storage, validated, and inserted
 * into the repository via a background coroutine.
 *
 * @param productId      The product this document belongs to.
 * @param documentRepository Repository used to persist the document record.
 * @param onSuccess      Callback invoked with the newly created [Document] on success.
 * @param onError        Callback invoked with a human-readable error message on failure.
 * @return A [ManagedActivityResultLauncher] that can be launched with mime types.
 */
@Composable
fun rememberDocumentImportLauncher(
    productId: Long,
    documentRepository: DocumentRepository,
    onSuccess: (Document) -> Unit,
    onError: (String) -> Unit
): ManagedActivityResultLauncher<Array<String>, Uri?> {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    return rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult

        scope.launch(Dispatchers.IO) {
            try {
                val displayName = getDisplayNameFromUri(context, uri)
                    ?: uri.lastPathSegment
                    ?: "document"

                val mimeType = getMimeTypeFromUri(context, uri) ?: "application/octet-stream"
                val fileSizeBytes = getFileSizeFromUri(context, uri)

                val relativePath = copyDocumentToPrivateStorage(
                    context = context,
                    sourceUri = uri,
                    displayName = displayName
                )

                if (relativePath == null) {
                    withContext(Dispatchers.Main) {
                        onError("Failed to copy document to storage")
                    }
                    return@launch
                }

                val document = Document(
                    id = 0L, // Room auto-generates
                    productId = productId,
                    displayName = displayName,
                    mimeType = mimeType,
                    relativePath = relativePath,
                    fileSizeBytes = fileSizeBytes,
                    addedAt = System.currentTimeMillis()
                )

                val insertedId = documentRepository.insert(document)
                val savedDocument = document.copy(id = insertedId)

                withContext(Dispatchers.Main) {
                    onSuccess(savedDocument)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError("Error importing document: ${e.localizedMessage ?: "Unknown error"}")
                }
            }
        }
    }
}
