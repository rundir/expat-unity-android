package de.expatunity.core.network

import de.expatunity.core.domain.Apartment
import de.expatunity.core.domain.ComplexDetail
import de.expatunity.core.domain.ComplexRepository
import de.expatunity.core.domain.ComplexSummary
import de.expatunity.core.domain.ListingStatus
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

private fun String.toStatus() = runCatching { ListingStatus.valueOf(this) }.getOrDefault(ListingStatus.UNKNOWN)

fun ComplexSummaryDto.toDomain() =
    ComplexSummary(id, name, city, status.toStatus(), priceFrom, coverImageUrl, unitsAvailable)
fun UnitDto.toDomain() =
    Apartment(id, title, areaSqm, price, expectedRentMonthly, status.toStatus())
fun ComplexDetailDto.toDomain() = ComplexDetail(
    id,
    name,
    city,
    address,
    status.toStatus(),
    description,
    images,
    units.map { it.toDomain() },
    fallbackUsed
)

class NetworkComplexRepository(private val api: MobileApi) : ComplexRepository {
    override suspend fun getComplexes(locale: String, page: Int) = api.complexes(locale, page).items.map { it.toDomain() }
    override suspend fun getComplex(id: String, locale: String) = api.complex(id, locale).toDomain()
}

object NetworkModule {
    private val json = Json { ignoreUnknownKeys = true; explicitNulls = false }

    val api: MobileApi by lazy {
        val client = OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
            })
            .build()
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(MobileApi::class.java)
    }

    val complexRepository: ComplexRepository by lazy { NetworkComplexRepository(api) }
}