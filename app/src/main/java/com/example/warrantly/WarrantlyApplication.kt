package com.example.warrantly

import android.app.Application
import com.example.warrantly.data.local.AppDatabase
import com.example.warrantly.data.preferences.DataStoreManager
import com.example.warrantly.data.repository.DocumentRepositoryImpl
import com.example.warrantly.data.repository.ProductRepositoryImpl
import com.example.warrantly.domain.repository.DocumentRepository
import com.example.warrantly.domain.repository.ProductRepository
import com.example.warrantly.notifications.NotificationChannels
import com.example.warrantly.notifications.NotificationScheduler

class WarrantlyApplication : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var productRepository: ProductRepository
        private set

    lateinit var documentRepository: DocumentRepository
        private set

    lateinit var dataStoreManager: DataStoreManager
        private set

    lateinit var notificationScheduler: NotificationScheduler
        private set

    override fun onCreate() {
        super.onCreate()
        NotificationChannels.createChannels(this)
        database = AppDatabase.getInstance(this)
        productRepository = ProductRepositoryImpl(database.productDao())
        documentRepository = DocumentRepositoryImpl(database.documentDao())
        dataStoreManager = DataStoreManager(this)
        notificationScheduler = NotificationScheduler(this)
    }
}
