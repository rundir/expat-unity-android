package de.expatunity.core.network

import de.expatunity.core.domain.ErrorType

fun Throwable.toErrorType(): ErrorType = when (this) {
    is java.io.IOException -> ErrorType.Network
    is retrofit2.HttpException -> ErrorType.Server
    else -> ErrorType.Unknown
}