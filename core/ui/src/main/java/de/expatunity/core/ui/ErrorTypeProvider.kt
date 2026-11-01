package de.expatunity.core.ui

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import de.expatunity.core.domain.ErrorType

class ErrorTypeProvider : PreviewParameterProvider<ErrorType> {
    override val values = ErrorType.entries.asSequence()
}