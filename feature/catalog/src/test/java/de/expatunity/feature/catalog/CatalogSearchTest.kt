package de.expatunity.feature.catalog

import app.cash.turbine.test
import de.expatunity.core.domain.LocaleProvider
import de.expatunity.core.testing.FakeComplexRepository
import de.expatunity.core.testing.MainDispatcherRule
import de.expatunity.core.testing.sampleSummary
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CatalogSearchTest {

    @get:Rule
    val main = MainDispatcherRule()
    private val repo = FakeComplexRepository()
    private val locale = object : LocaleProvider { override fun apiLocale() = "de" }

    private fun vm() = CatalogViewModel(repo, locale)

    @Test
    fun `query filters items by name`() = runTest {
        repo.listOfSummary = listOf(
            sampleSummary(id = "1", name = "Isar Park"),
            sampleSummary(id = "2", name = "Berg Residenz"),
        )
        val vm = vm()

        vm.uiState.test {
            // пропускаем Loading, пока не придёт контент
            var item = awaitItem()
            while (item !is CatalogUiState.Content) item = awaitItem()
            assertEquals(2, item.items.size)

            vm.onQueryChange("isar")
            advanceTimeBy(301)

            val filtered = awaitItem() as CatalogUiState.Content
            assertEquals(listOf("1"), filtered.items.map { it.id })
        }
    }

    @Test
    fun `blank query shows all items`() = runTest {
        repo.listOfSummary = listOf(
            sampleSummary(id = "1", name = "Isar Park"),
            sampleSummary(id = "2", name = "Berg Residenz"),
        )
        val vm = vm()

        vm.uiState.test {
            var item = awaitItem()
            while (item !is CatalogUiState.Content) item = awaitItem()

            vm.onQueryChange("isar")
            advanceTimeBy(301)
            awaitItem()

            vm.onQueryChange("")
            advanceTimeBy(301)
            val all = awaitItem() as CatalogUiState.Content
            assertEquals(2, all.items.size)
        }
    }

}