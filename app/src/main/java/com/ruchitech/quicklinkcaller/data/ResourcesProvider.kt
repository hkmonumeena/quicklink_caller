package com.ruchitech.quicklinkcaller.data

import android.content.Context
import androidx.annotation.StringRes
import com.ruchitech.quicklinkcaller.MainActivity
import com.ruchitech.quicklinkcaller.MyApp
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ResourcesProvider @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    var appContext: Context = context
    fun getString(@StringRes stringResId: Int): String {
        return context.getString(stringResId)
    }
    val launcherActivityContext: MainActivity?
        get() = (context as MyApp).launcherActivity

    fun getContext(): Context {
        return context
    }
}