package de.expatunity.core.data

import de.expatunity.core.domain.ErrorType
import java.io.IOException

fun Throwable.toErrorType(): ErrorType = when (this) {
    is IOException -> ErrorType.Network
    is retrofit2.HttpException -> ErrorType.Server
    else -> ErrorType.Unknown
}