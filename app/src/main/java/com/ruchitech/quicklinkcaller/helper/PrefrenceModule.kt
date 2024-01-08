package com.ruchitech.quicklinkcaller.helper

import android.content.Context
import com.ruchitech.quicklinkcaller.contactutills.CallLogHelper
import com.ruchitech.quicklinkcaller.room.DbRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class PreferenceModule {
    @Singleton
    @Provides
    fun provideAppPreference(
        @ApplicationContext context: Context
    ): AppPreference = AppPreference(context)

    @Singleton
    @Provides
    fun provideCallLogs(
        @ApplicationContext context: Context,
        dbRepository: DbRepository,
        appPreference: AppPreference
    ): CallLogHelper = CallLogHelper(context, dbRepository, appPreference)


}