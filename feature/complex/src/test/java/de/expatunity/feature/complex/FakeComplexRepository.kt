package de.expatunity.feature.complex

import de.expatunity.core.domain.ComplexDetail
import de.expatunity.core.domain.ComplexRepository
import de.expatunity.core.domain.ComplexSummary

class FakeComplexRepository : ComplexRepository {
    var detail: ComplexDetail? = null
    var error: Throwable? = null
    override suspend fun getComplexes(locale: String, page: Int) = emptyList<ComplexSummary>()
    override suspend fun getComplex(id: String, locale: String): ComplexDetail {
        error?.let { throw it }
        return detail!!
    }
}