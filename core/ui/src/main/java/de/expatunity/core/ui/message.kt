package de.expatunity.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import de.expatunity.core.domain.ErrorType

@Composable
fun ErrorType.message(): String = stringResource(
    when (this) {
        ErrorType.Network -> R.string.error_network
        ErrorType.Server -> R.string.error_server
        ErrorType.Unknown -> R.string.error_unknown
    }
)