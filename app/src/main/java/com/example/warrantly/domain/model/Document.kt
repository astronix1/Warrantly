package com.example.warrantly.domain.model

data class Document(
    val id: Long,
    val productId: Long,
    val displayName: String,
    val mimeType: String,
    val relativePath: String,
    val fileSizeBytes: Long,
    val addedAt: Long
)
