package de.expatunity.core.domain

import kotlinx.coroutines.flow.Flow

interface ComplexRepository {
    fun observeComplexes(locale: String): Flow<List<ComplexSummary>>
    suspend fun getComplex(id: String, locale: String): ComplexDetail
    suspend fun refreshComplexes(locale: String)
}

class AppException(val type: ErrorType) : Exception()