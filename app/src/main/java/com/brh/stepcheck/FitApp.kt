package com.brh.stepcheck

import android.app.Application
import android.content.Context
import com.brh.stepcheck.dagger.DaggerFitComponent
import com.brh.stepcheck.dagger.FitAPIModule
import com.brh.stepcheck.dagger.FitComponent

class FitApp : Application() {

    override fun onCreate() {
        super.onCreate()
        ctx = this
    }

    companion object {

        lateinit var ctx : Context
            private set

        val appComponent : FitComponent by lazy {
            DaggerFitComponent.builder()
                .fitAPIModule(FitAPIModule(ctx))
                .build()
        }

    }
}