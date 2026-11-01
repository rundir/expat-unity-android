package de.expatunity.core.domain

interface ComplexRepository {
    suspend fun getComplexes(locale: String, page: Int = 1): List<ComplexSummary>
    suspend fun getComplex(id: String, locale: String): ComplexDetail
}

class AppException(val type: ErrorType) : Exception()