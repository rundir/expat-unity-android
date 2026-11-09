package de.expatunity.core.domain.usecase

import de.expatunity.core.domain.ComplexSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

data class CatalogFilter(
    val query: String = "",
    val favoritesOnly: Boolean = false,
)

data class Catalog(
    val items: List<ComplexSummary>,
    val favoriteIds: Set<String>,
    val hasAnyComplexes: Boolean,
)

class ObserveCatalogUseCase(
    private val observeComplexes: ObserveComplexesUseCase,
    private val observeFavoriteIds: ObserveFavoriteIdsUseCase,
) {
    operator fun invoke(locale: String, filter: Flow<CatalogFilter>): Flow<Catalog> =
        combine(observeComplexes(locale), observeFavoriteIds(), filter) { complexes, favIds, f ->
            val visible = complexes
                .filter { f.query.isBlank() || it.name.contains(f.query, ignoreCase = true) }
                .filter { !f.favoritesOnly || it.id in favIds }
            Catalog(visible, favIds, complexes.isNotEmpty())
        }
}