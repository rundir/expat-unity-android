package de.expatunity.core.testing

import de.expatunity.core.domain.ComplexDetail
import de.expatunity.core.domain.ComplexRepository
import de.expatunity.core.domain.ComplexSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeComplexRepository : ComplexRepository {

    val complexes = MutableStateFlow<List<ComplexSummary>>(emptyList())
    var detail: ComplexDetail? = null
    var error: Throwable? = null
    var refreshCalls = 0

    override fun observeComplexes(locale: String): Flow<List<ComplexSummary>> = complexes

    override suspend fun refreshComplexes(locale: String) {
        refreshCalls++
        error?.let { throw it }
    }

    override suspend fun getComplex(id: String, locale: String): ComplexDetail {
        error?.let { throw it }
        return checkNotNull(detail)
    }
}