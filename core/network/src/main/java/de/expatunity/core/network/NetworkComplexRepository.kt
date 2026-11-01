package de.expatunity.core.network

import de.expatunity.core.domain.Apartment
import de.expatunity.core.domain.AppException
import de.expatunity.core.domain.ComplexDetail
import de.expatunity.core.domain.ComplexRepository
import de.expatunity.core.domain.ComplexSummary
import de.expatunity.core.domain.ListingStatus
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

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

class NetworkComplexRepository @Inject constructor(private val api: MobileApi) : ComplexRepository {
    override suspend fun getComplexes(locale: String, page: Int) = try {
        api.complexes(locale, page).items.map { it.toDomain() }
    } catch (e: CancellationException) { throw e
    } catch (e: Exception) { throw AppException(e.toErrorType())
    }

    override suspend fun getComplex(id: String, locale: String) = try {
        api.complex(id, locale).toDomain()
    } catch (e: CancellationException) { throw e
    } catch (e: Exception) { throw AppException(e.toErrorType())
    }
}
