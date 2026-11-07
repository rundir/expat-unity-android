package de.expatunity.core.testing

import de.expatunity.core.domain.ComplexDetail
import de.expatunity.core.domain.ComplexRepository
import de.expatunity.core.domain.ComplexSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeComplexRepository : ComplexRepository {
    val complexes = MutableStateFlow<List<ComplexSummary>>(emptyList())
    val cachedDetail = MutableStateFlow<ComplexDetail?>(null)
    var detail: ComplexDetail? = null // следующий успешный ответ сети
    var error: Throwable? = null
    var refreshCalls = 0
    var detailRefreshCalls = 0

    override fun observeComplexes(locale: String): Flow<List<ComplexSummary>> =
        complexes

    override suspend fun refreshComplexes(locale: String) {
        refreshCalls++
        error?.let { throw it }
    }

    override fun observeComplex(
        id: String,
        locale: String,
    ): Flow<ComplexDetail?> = cachedDetail

    override suspend fun refreshComplex(id: String, locale: String) {
        detailRefreshCalls++
        error?.let { throw it }
        cachedDetail.value = checkNotNull(detail)
    }
}