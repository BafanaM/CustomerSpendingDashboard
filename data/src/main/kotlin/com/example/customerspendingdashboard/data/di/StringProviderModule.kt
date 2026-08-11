package com.example.customerspendingdashboard.data.di

import android.content.Context
import com.example.customerspendingdashboard.core.common.StringProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Hilt binding for the app's [StringProvider]. */
@Module
@InstallIn(SingletonComponent::class)
object StringProviderModule {
    // Binds the real Context-backed string resolver used across the app.
    @Provides
    @Singleton
    fun provideStringProvider(
        @ApplicationContext context: Context,
    ): StringProvider = AndroidStringProvider(context)
}

private class AndroidStringProvider(
    private val context: Context,
) : StringProvider {
    override fun getString(resId: Int): String = context.getString(resId)
}
