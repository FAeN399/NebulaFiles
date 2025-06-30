package com.nebula.files.di

import com.nebula.files.data.provider.TermuxFileProvider
import com.nebula.files.data.repository.FileOperationRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideTermuxFileProvider(): TermuxFileProvider = TermuxFileProvider()
    
    @Provides
    @Singleton
    fun provideFileOperationRepository(): FileOperationRepository = FileOperationRepository()
}