package de.expatunity.core.network

import de.expatunity.core.database.ApartmentEntity
import de.expatunity.core.database.AppDatabase
import de.expatunity.core.database.CachedComplexDetail
import de.expatunity.core.database.ComplexDao
import de.expatunity.core.database.ComplexDetailEntity
import de.expatunity.core.database.ComplexImageEntity
import de.expatunity.core.domain.Apartment
import de.expatunity.core.domain.AppException
import de.expatunity.core.domain.ComplexDetail
import de.expatunity.core.domain.ComplexRepository
import de.expatunity.core.domain.ComplexSummary
import de.expatunity.core.domain.ListingStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

private fun String.toStatus() =
    runCatching { ListingStatus.valueOf(this) }.getOrDefault(ListingStatus.UNKNOWN)

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

class NetworkComplexRepository @Inject constructor(
    private val api: MobileApi,
    private val dao: ComplexDao,
    private val database: AppDatabase,
) : ComplexRepository {

    override fun observeComplexes(locale: String) =
        dao.observe(locale).map { list -> list.map { it.toDomain() } }

    override fun observeComplex(
        id: String,
        locale: String,
    ): Flow<ComplexDetail?> = database.invalidationTracker
        .createFlow(
            "complex_details",
            "complex_images",
            "complex_apartments",
            emitInitialState = true,
        )
        .map {
            database.complexDetailDao()
                .getCached(id, locale)
                ?.toDomain()
        }

    override suspend fun refreshComplex(id: String, locale: String) {
        val fresh = try {
            api.complex(id, locale).toDomain()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw AppException(e.toErrorType())
        }

        database.complexDetailDao().replaceAll(
            CachedComplexDetail(
                detail = ComplexDetailEntity(
                    id = fresh.id,
                    locale = locale,
                    name = fresh.name,
                    city = fresh.city,
                    address = fresh.address,
                    status = fresh.status.name,
                    description = fresh.description,
                    fallbackUsed = fresh.fallbackUsed,
                ),
                images = fresh.images.mapIndexed { position, url ->
                    ComplexImageEntity(
                        complexId = fresh.id,
                        locale = locale,
                        position = position,
                        url = url,
                    )
                },
                apartments = fresh.apartments.map { apartment ->
                    ApartmentEntity(
                        complexId = fresh.id,
                        locale = locale,
                        id = apartment.id,
                        title = apartment.title,
                        areaSqm = apartment.areaSqm,
                        price = apartment.price,
                        expectedRentMonthly = apartment.expectedRentMonthly,
                        status = apartment.status.name,
                    )
                },
            )
        )
    }

    override suspend fun refreshComplexes(locale: String) {
        val fresh = try {
            api.complexes(locale, 1).items.map { it.toDomain() }
        } catch (e: CancellationException) { throw e
        } catch (e: Exception) { throw AppException(e.toErrorType()) }
        dao.replaceAll(locale, fresh.map { it.toEntity(locale) })
    }
}
