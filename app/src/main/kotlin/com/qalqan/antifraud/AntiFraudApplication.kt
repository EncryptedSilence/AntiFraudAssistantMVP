package com.qalqan.antifraud

import android.app.Application

class AntiFraudApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AlertWiring.installInto(this)
    }
}
