package de.expatunity.core.network

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface MobileApi {
    @GET("complexes")
    suspend fun complexes(@Query("locale") locale: String, @Query("page") page: Int, @Query("pageSize") pageSize: Int = 20): ComplexListDto

    @GET("complexes/{id}")
    suspend fun complex(@Path("id") id: String, @Query("locale") locale: String): ComplexDetailDto
}