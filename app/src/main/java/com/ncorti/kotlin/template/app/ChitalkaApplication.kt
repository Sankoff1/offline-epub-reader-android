package com.ncorti.kotlin.template.app

import android.app.Application
import com.chitalka.debug.installConsoleCapture

class ChitalkaApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        installConsoleCapture()
        container = AppContainer(this)
    }
}
