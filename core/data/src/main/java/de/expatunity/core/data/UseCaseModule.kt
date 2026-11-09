package de.expatunity.core.data

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.expatunity.core.domain.ComplexRepository
import de.expatunity.core.domain.FavoritesRepository
import de.expatunity.core.domain.usecase.ObserveCatalogUseCase
import de.expatunity.core.domain.usecase.ObserveComplexUseCase
import de.expatunity.core.domain.usecase.ObserveComplexesUseCase
import de.expatunity.core.domain.usecase.ObserveFavoriteIdsUseCase
import de.expatunity.core.domain.usecase.RefreshComplexUseCase
import de.expatunity.core.domain.usecase.RefreshComplexesUseCase
import de.expatunity.core.domain.usecase.ToggleFavoriteUseCase

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {
    @Provides
    fun toggleFavoriteUseCase(
        repository: FavoritesRepository,
    ): ToggleFavoriteUseCase = ToggleFavoriteUseCase(repository)
    @Provides
    fun provideObserveFavoriteIds(repo: FavoritesRepository) = ObserveFavoriteIdsUseCase(repo)
    @Provides
    fun provideObserveComplexes(repo: ComplexRepository) = ObserveComplexesUseCase(repo)
    @Provides
    fun provideRefreshComplexes(repo: ComplexRepository) = RefreshComplexesUseCase(repo)
    @Provides
    fun provideObserveComplex(repo: ComplexRepository) = ObserveComplexUseCase(repo)
    @Provides
    fun provideRefreshComplex(repo: ComplexRepository) = RefreshComplexUseCase(repo)
    @Provides
    fun provideObserveCatalog(
        observeComplexes: ObserveComplexesUseCase,
        observeFavoriteIds: ObserveFavoriteIdsUseCase,
    ) = ObserveCatalogUseCase(observeComplexes, observeFavoriteIds)
}