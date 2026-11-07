package com.barbosa.marketcompare

import android.app.Application
import com.barbosa.marketcompare.data.local.MarketCompareDatabase
import com.barbosa.marketcompare.data.repository.PurchaseRepositoryImpl
import com.barbosa.marketcompare.domain.repository.PurchaseRepository

class MarketCompareApplication : Application() {

    val database: MarketCompareDatabase by lazy {
        MarketCompareDatabase.getDatabase(this)
    }

    val purchaseRepository: PurchaseRepository by lazy {
        PurchaseRepositoryImpl(database)
    }

    override fun onCreate() {
        super.onCreate()
    }
}

