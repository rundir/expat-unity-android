package de.expatunity.feature.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.expatunity.core.ui.shimmer
import de.expatunity.core.ui.theme.ExpatUnityTheme

@Composable
fun ComplexCardPlaceholder(modifier: Modifier = Modifier) {
    Card(modifier.fillMaxWidth()) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(180.dp)
                .shimmer()
        )
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                Modifier
                    .fillMaxWidth(0.6f)
                    .height(20.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmer()
            )
            Box(
                Modifier
                    .fillMaxWidth(0.4f)
                    .height(14.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmer()
            )
        }
    }
}
@Preview(showBackground = true)
@Composable
private fun ComplexCardPlaceholderPreview() {
    ExpatUnityTheme {
        ComplexCardPlaceholder(Modifier.padding(16.dp))
    }
}