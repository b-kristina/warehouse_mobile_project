package ru.vsu.warehouse.app

import android.app.Application

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Здесь можно инициализировать что-то глобальное
        // Например: Timber, Crashlytics, DI и т.д.
    }
}