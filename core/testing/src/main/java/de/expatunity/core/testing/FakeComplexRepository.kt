package de.expatunity.core.testing

import de.expatunity.core.domain.ComplexDetail
import de.expatunity.core.domain.ComplexRepository
import de.expatunity.core.domain.ComplexSummary
import de.expatunity.core.domain.ListingStatus

class FakeComplexRepository: ComplexRepository {
    
    var listOfSummary: List<ComplexSummary> = emptyList()
    var detail: ComplexDetail? = null

    var error: Throwable? = null
    
    override suspend fun getComplexes(locale: String, page: Int): List<ComplexSummary> {
        error?.let { throw it }
        return complexSummary()
    }

    private fun complexSummary(): List<ComplexSummary> {
        return listOfSummary
    }

    override suspend fun getComplex(id: String, locale: String): ComplexDetail {
        error?.let { throw it }
        return detail!!
    }

}
