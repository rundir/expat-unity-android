package de.expatunity.feature.complex.video

import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.media3.common.util.UnstableApi
import de.expatunity.feature.complex.R
import androidx.media3.ui.compose.material3.Player as MediaPlayer

@Composable
fun ComplexVideo(videoUrl: String, modifier: Modifier = Modifier) {
    var started by rememberSaveable { mutableStateOf(false) }
    Box(modifier
        .fillMaxWidth()
        .aspectRatio(16f / 9f)
        .background(Color.Black)) {
        if (started) {
            VideoPlayer(videoUrl)
        } else {
            IconButton(onClick = { started = true }, modifier = Modifier.align(Alignment.Center)) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = stringResource(R.string.play_video),
                    tint = Color.White
                )
            }
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
private fun VideoPlayer(
    videoUrl: String,
    viewModel: VideoPlayerViewModel = hiltViewModel(key = "video-$videoUrl"),
) {
    LaunchedEffect(videoUrl) { viewModel.load(videoUrl) }

    LifecycleStartEffect(viewModel) {
        viewModel.onStart()
        onStopOrDispose { viewModel.onStop() }
    }

    var showControls by remember { mutableStateOf(true) }
    MediaPlayer(
        player = viewModel.player,
        showControls = showControls,
        modifier = Modifier.fillMaxSize().clickable { showControls = !showControls },
    )
}