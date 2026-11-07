package de.expatunity.core.domain

import kotlinx.coroutines.flow.Flow

interface ComplexRepository {
    fun observeComplexes(locale: String): Flow<List<ComplexSummary>>
    fun observeComplex(id: String, locale: String): Flow<ComplexDetail?>
    suspend fun refreshComplex(id: String, locale: String)
    suspend fun refreshComplexes(locale: String)
}

class AppException(val type: ErrorType) : Exception()