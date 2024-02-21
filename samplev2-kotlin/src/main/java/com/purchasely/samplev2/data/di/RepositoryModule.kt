package com.purchasely.samplev2.data.di

import android.content.Context
import com.purchasely.samplev2.data.repository.PreferencesRepositoryImpl
import com.purchasely.samplev2.domain.preferences.PreferencesRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

/**
 * Hilt module that provided repositories instances.
 */
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    /**
     * Provides instance of [PreferencesRepository].
     */
    @Provides
    fun providePreferencesRepository(@ApplicationContext context: Context): PreferencesRepository =
        PreferencesRepositoryImpl(context)
}