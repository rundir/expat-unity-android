package de.expatunity.feature.complex

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import de.expatunity.core.ui.shimmer

@Composable
fun ComplexDetailPlaceholder(modifier: Modifier) {
    Column(modifier) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(280.dp)
                .shimmer()
        )
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            listOf(0.7f, 0.5f, 0.9f, 0.8f).forEach { w ->
                Box(
                    Modifier
                        .fillMaxWidth(w)
                        .height(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmer()
                )
            }
        }
    }
}