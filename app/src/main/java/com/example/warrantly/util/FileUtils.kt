package com.example.warrantly.util

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.util.UUID

private const val DOCUMENTS_DIR = "documents"

/**
 * Copies a content URI into the app-private documents directory.
 * Returns the relative path (relative to filesDir/documents/) or null on failure.
 */
suspend fun copyDocumentToPrivateStorage(
    context: Context,
    sourceUri: Uri,
    displayName: String
): String? = withContext(Dispatchers.IO) {
    try {
        val docsDir = File(context.filesDir, DOCUMENTS_DIR).apply { mkdirs() }

        // Build a unique filename preserving the extension from displayName
        val extension = displayName.substringAfterLast('.', "")
        val uniqueName = if (extension.isNotEmpty()) {
            "${UUID.randomUUID()}.$extension"
        } else {
            UUID.randomUUID().toString()
        }

        val destFile = File(docsDir, uniqueName)

        context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
            destFile.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        } ?: return@withContext null

        uniqueName // relative path within DOCUMENTS_DIR
    } catch (e: IOException) {
        null
    } catch (e: SecurityException) {
        null
    } catch (e: Exception) {
        null
    }
}

/**
 * Returns the absolute [File] for a given relative document path.
 */
fun getDocumentFile(context: Context, relativePath: String): File =
    File(File(context.filesDir, DOCUMENTS_DIR), relativePath)

/**
 * Returns the file size in bytes from a content URI, or 0 if unavailable.
 */
fun getFileSizeFromUri(context: Context, uri: Uri): Long {
    return try {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (sizeIndex != -1 && cursor.moveToFirst()) {
                cursor.getLong(sizeIndex)
            } else {
                0L
            }
        } ?: 0L
    } catch (e: Exception) {
        0L
    }
}

/**
 * Returns the display name for a content URI, or null if unavailable.
 */
fun getDisplayNameFromUri(context: Context, uri: Uri): String? {
    return try {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex != -1 && cursor.moveToFirst()) {
                cursor.getString(nameIndex)
            } else {
                null
            }
        }
    } catch (e: Exception) {
        null
    }
}

/**
 * Returns the MIME type for a content URI, or null if unavailable.
 */
fun getMimeTypeFromUri(context: Context, uri: Uri): String? {
    return try {
        context.contentResolver.getType(uri)
    } catch (e: Exception) {
        null
    }
}

/**
 * Deletes a document file identified by its relative path.
 * Returns true if the file was deleted successfully, false otherwise.
 */
fun deleteDocumentFile(context: Context, relativePath: String): Boolean {
    return try {
        val file = getDocumentFile(context, relativePath)
        if (file.exists()) file.delete() else false
    } catch (e: Exception) {
        false
    }
}
