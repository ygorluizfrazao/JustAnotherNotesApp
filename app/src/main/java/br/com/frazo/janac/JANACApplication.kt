package br.com.frazo.janac

import android.app.Application
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import dagger.hilt.android.HiltAndroidApp


@HiltAndroidApp
class JANACApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        if(BuildConfig.DEBUG)
            StrictMode.enableDefaults()
    }

}