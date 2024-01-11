package com.ruchitech.quicklinkcaller.room

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject

@Module
@InstallIn(SingletonComponent::class)
class DbRepository @Inject constructor(
    databaseDao: DatabaseDao
) {
    val dataDao = databaseDao.dataDao()
    val callLogDao = databaseDao.callLogs()
    val contact = databaseDao.contact()
    val callerIDOptions = databaseDao.callerIDOptions()
    val timestampDao = databaseDao.timestampDao()
}