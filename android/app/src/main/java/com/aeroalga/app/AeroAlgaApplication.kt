package com.aeroalga.app

import android.app.Application
import com.aeroalga.app.data.repository.NodeRepository

class AeroAlgaApplication : Application() {
    lateinit var repository: NodeRepository
        private set

    override fun onCreate() {
        super.onCreate()
        repository = NodeRepository()
    }
}
