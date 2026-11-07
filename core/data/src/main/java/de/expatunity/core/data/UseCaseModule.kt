package de.expatunity.core.data

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.expatunity.core.domain.FavoritesRepository
import de.expatunity.core.domain.ToggleFavoriteUseCase

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {
    @Provides
    fun toggleFavoriteUseCase(
        repository: FavoritesRepository,
    ): ToggleFavoriteUseCase = ToggleFavoriteUseCase(repository)
}