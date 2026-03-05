package com.firsov.substation.di

import android.content.Context
import androidx.room.Room
import com.firsov.substation.data.local.AppDatabase
import com.firsov.substation.data.local.CellDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "substation.db").build()

    @Provides
    fun provideCellDao(db: AppDatabase): CellDao = db.cellDao()
}
