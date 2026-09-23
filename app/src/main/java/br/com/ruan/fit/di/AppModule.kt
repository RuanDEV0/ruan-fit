package br.com.ruan.fit.di

import android.content.Context
import androidx.room.Room
import br.com.ruan.fit.data.FitDao
import br.com.ruan.fit.data.FitDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module @InstallIn(SingletonComponent::class)
object AppModule {
    @Provides @Singleton fun database(@ApplicationContext context: Context): FitDatabase =
        Room.databaseBuilder(context, FitDatabase::class.java, "ruan-fit.db").build()
    @Provides fun dao(database: FitDatabase): FitDao = database.dao()
}
