package de.expatunity.core.domain.usecase

import app.cash.turbine.test
import de.expatunity.core.domain.ComplexRepository
import de.expatunity.core.domain.ComplexSummary
import de.expatunity.core.domain.FavoritesRepository
import de.expatunity.core.domain.ListingStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ObserveCatalogUseCaseTest {

    private val complexes = MutableStateFlow(
        listOf(
            summary("1", "Isar Park"),
            summary("2", "Isar Residenz"),
            summary("3", "Berg Haus"),
        )
    )


    private val favIds = MutableStateFlow(setOf("2", "3"))
    private val filter = MutableStateFlow(CatalogFilter())

    private val useCase = ObserveCatalogUseCase(
        ObserveComplexesUseCase(object : ComplexRepository {
            override fun observeComplexes(locale: String) = complexes
            override suspend fun refreshComplexes(locale: String) = Unit
            override fun observeComplex(id: String, locale: String) = flowOf(null)
            override suspend fun refreshComplex(id: String, locale: String) = Unit
        }),
        ObserveFavoriteIdsUseCase(object : FavoritesRepository {
            override fun observeFavoriteIds() = favIds
            override suspend fun toggleFavorite(complexId: String) = Unit
        }),
    )

    @Test
    fun `no filter returns all`() = runTest {
        useCase("en", filter).test {
            val catalog = awaitItem()
            assertEquals(listOf("1", "2", "3"), catalog.items.map { it.id })
            assertTrue(catalog.hasAnyComplexes)
        }
    }

    @Test
    fun `query and favorites combine`() = runTest {
        filter.value = CatalogFilter(query = "isar", favoritesOnly = true)
        useCase("en", filter).test {
            assertEquals(listOf("2"), awaitItem().items.map { it.id })
        }
    }

    @Test
    fun `filtered empty still has complexes`() = runTest {
        filter.value = CatalogFilter(query = "zzz")
        useCase("en", filter).test {
            val catalog = awaitItem()
            assertTrue(catalog.items.isEmpty())
            assertTrue(catalog.hasAnyComplexes)
        }
    }

    private fun summary(id: String, name: String) = ComplexSummary(
        id,
        name,
        "city",
        ListingStatus.UNKNOWN,
        100.0,
        "coverImageUrl",
        7,
        null
    )

}