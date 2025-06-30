package com.nebula.files.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    // Empty for now - repositories use @Inject constructors
    // This module is kept for future non-injectable dependencies
}