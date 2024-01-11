package com.ruchitech.quicklinkcaller.helper

import android.content.Context
import android.content.SharedPreferences
import com.ruchitech.quicklinkcaller.ui.screens.settings.AllCallerIdOptions
import javax.inject.Inject

class AppPreference
@Inject
constructor(
    private val context: Context
) {


    private fun getSharedPreference(preferenceFile: String): SharedPreferences {
        return context.getSharedPreferences(preferenceFile, Context.MODE_PRIVATE)
    }

    private fun getSharedPreferenceEditor(
        preferenceFile: String,
    ): SharedPreferences.Editor {
        return context.getSharedPreferences(preferenceFile, Context.MODE_PRIVATE).edit()
    }

    fun clearData() {
        getSharedPreferenceEditor(PREFERENCE_KEY).clear().commit()
    }

    var lastHearBeatTime: Long
        get() = getSharedPreference(PREFERENCE_KEY).getLong(LAST_EXECUTION_TIME_KEY, 0)
        set(value) = getSharedPreferenceEditor(PREFERENCE_KEY).putLong(
            LAST_EXECUTION_TIME_KEY,
            value
        ).apply()
    var lastCase44TriggerTime: Long
        get() = getSharedPreference(PREFERENCE_KEY).getLong(LAST_CASE_44_TRIGGER_TIME, 0)
        set(value) = getSharedPreferenceEditor(PREFERENCE_KEY).putLong(
            LAST_CASE_44_TRIGGER_TIME,
            value
        ).apply()


    var isInitialCallLogDone: Boolean
        get() = getSharedPreference(PREFERENCE_KEY).getBoolean(INITIAL_CALL_LOGS_DONE, false)
        set(value) = getSharedPreferenceEditor(PREFERENCE_KEY).putBoolean(
            INITIAL_CALL_LOGS_DONE,
            value
        ).apply()

/*    var updateLastInitializationTimestamp: Long
        get() = getSharedPreference(PREFERENCE_KEY).getLong(UpdateLastInitializationTimestamp, 0L)
        set(value) = getSharedPreferenceEditor(PREFERENCE_KEY).putLong(
            UpdateLastInitializationTimestamp,
            value
        ).apply()*/

    var shouldForeground: Boolean
        get() = getSharedPreference(PREFERENCE_KEY).getBoolean(SHOULD_FOREGROUND, false)
        set(value) = getSharedPreferenceEditor(PREFERENCE_KEY).putBoolean(
            SHOULD_FOREGROUND,
            value
        ).apply()

    var isFirstOpen: Boolean
        get() = getSharedPreference(PREFERENCE_KEY).getBoolean(IS_FIRST_OPEN, true)
        set(value) = getSharedPreferenceEditor(PREFERENCE_KEY).putBoolean(
            IS_FIRST_OPEN,
            value
        ).apply()

    // SharedPreferences extension functions
    fun SharedPreferences.getCallerIdOptions(key: String): Set<AllCallerIdOptions> {
        val optionsString = getString(key, null)
        return optionsString?.split(",")?.map { AllCallerIdOptions.valueOf(it) }?.toSet() ?: emptySet()
    }

    fun SharedPreferences.Editor.putCallerIdOptions(key: String, options: Set<AllCallerIdOptions>) {
        val optionsString = options.joinToString(",") { it.name }
        putString(key, optionsString).commit()
    }

    // Usage in your ViewModel or wherever you need to store/retrieve the options
    var callerIdOptions: Set<AllCallerIdOptions>
        get() = getSharedPreference(PREFERENCE_KEY).getCallerIdOptions(CALLER_ID_OPTIONS)
        set(value) = getSharedPreferenceEditor(PREFERENCE_KEY).putCallerIdOptions(CALLER_ID_OPTIONS, value)


    companion object {
        const val PREFERENCE_KEY = "turn_out_pref"
        private const val LAST_EXECUTION_TIME_KEY = "LAST_EXECUTION_TIME_KEY "
        private const val LAST_CASE_44_TRIGGER_TIME = "lastCase44TriggerTime "
        private const val INITIAL_CALL_LOGS_DONE = "INITIAL_CALL_LOGS_DONE "
        private const val UpdateLastInitializationTimestamp = "updateLastInitializationTimestamp "
        private const val SHOULD_FOREGROUND = "startforegroundservice "
        private const val CALLER_ID_OPTIONS = "callerIdOptions "
        private const val IS_FIRST_OPEN = "isFirstOpen "
    }

}