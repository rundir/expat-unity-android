package de.expatunity.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

@Composable
fun ImageGallery(images: List<String>, modifier: Modifier = Modifier) {
    if (images.isEmpty()) return
    val pagerState = rememberPagerState { images.size }
    Box(modifier) {
        val isPreview = LocalInspectionMode.current
        HorizontalPager(state = pagerState) { page ->
            if (isPreview) {
                Box(Modifier.fillMaxWidth().aspectRatio(16f / 10f).background(Color.LightGray))
            } else {
                AsyncImage(
                    model = images[page],
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth().aspectRatio(16f / 10f),
                    placeholder = ColorPainter(Color.LightGray)
                )
            }
        }
        Row(
            Modifier.align(Alignment.BottomCenter).padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            repeat(images.size) { i ->
                val selected = pagerState.currentPage == i
                Box(
                    Modifier.size(if (selected) 8.dp else 6.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = if (selected) 1f else 0.5f))
                )
            }
        }
    }
}