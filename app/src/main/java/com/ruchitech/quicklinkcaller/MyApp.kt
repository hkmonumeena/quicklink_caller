package com.ruchitech.quicklinkcaller

// MyApp.kt
import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.ruchitech.quicklinkcaller.contactutills.CallLogHelper
import com.ruchitech.quicklinkcaller.helper.AppPreference
import com.ruchitech.quicklinkcaller.persistence.recievers.TriggerReceiver
import com.ruchitech.quicklinkcaller.room.DbRepository
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MyApp : Application() {
    lateinit var launcherActivity: MainActivity
    @Inject
    lateinit var pref: AppPreference
    @Inject
    lateinit var appPreference: AppPreference
    @Inject
    lateinit var dbRepository: DbRepository
    @Inject
    lateinit var callLogHelper: CallLogHelper
    companion object{
        lateinit var instance: MyApp
            private set
    }
    override fun onCreate() {
        super.onCreate()
        instance = this
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        TriggerReceiver.register(this)
    }
}