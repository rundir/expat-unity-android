package de.expatunity.core.data

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.expatunity.core.domain.ComplexRepository
import de.expatunity.core.domain.FavoritesRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindRepo(impl: DefaultComplexRepository): ComplexRepository

    @Binds
    @Singleton
    abstract fun bindFavoritesRepository(
        impl: DefaultFavoritesRepository,
    ): FavoritesRepository
}