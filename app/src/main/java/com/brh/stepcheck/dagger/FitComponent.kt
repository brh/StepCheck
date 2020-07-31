package com.brh.stepcheck.dagger

import com.brh.stepcheck.ui.main.MainViewModel
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = arrayOf(FitAPIModule::class))
interface FitComponent {
    fun inject(mvm: MainViewModel)
}