package com.example.customerspendingdashboard.data.di

import com.example.customerspendingdashboard.core.common.DefaultDispatcherProvider
import com.example.customerspendingdashboard.core.common.DispatcherProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Hilt binding for the app's [DispatcherProvider]. */
@Module
@InstallIn(SingletonComponent::class)
object DispatcherModule {
    // Binds the real coroutine dispatchers used across the app.
    @Provides
    @Singleton
    fun provideDispatcherProvider(): DispatcherProvider = DefaultDispatcherProvider()
}
