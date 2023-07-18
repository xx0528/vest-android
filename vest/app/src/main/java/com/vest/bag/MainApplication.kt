package com.vest.bag

import android.app.Application
import com.vest.bag.utils.ContextHolder

class MainApplication : Application(){

    override fun onCreate() {
        super.onCreate()
        ContextHolder.application = this
    }
}