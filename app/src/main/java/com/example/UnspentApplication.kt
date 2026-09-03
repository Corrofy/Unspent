package com.example

import android.app.Application
import com.example.data.db.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UnspentApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Pre-warm Room database connection asynchronously on background IO thread
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getDatabase(this@UnspentApplication)
                db.openHelper.writableDatabase
            } catch (_: Exception) {}
        }
    }
}
