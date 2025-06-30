package com.nebula.files.di

import com.nebula.files.data.provider.TermuxFileProvider
import com.nebula.files.data.repository.FileOperationRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.scopes.ServiceScoped
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    
    // Ensure repositories are available for injection
    // Even though they have @Inject constructors, sometimes
    // explicit providers help with service injection
    
    @Provides
    @Singleton
    fun provideFileOperationRepository(): FileOperationRepository {
        return FileOperationRepository()
    }
    
    @Provides
    @Singleton
    fun provideTermuxFileProvider(): TermuxFileProvider {
        return TermuxFileProvider()
    }
}

@Module
@InstallIn(ServiceComponent::class)
object ServiceModule {
    // Service-scoped dependencies if needed
}