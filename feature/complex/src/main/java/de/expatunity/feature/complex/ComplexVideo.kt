package de.expatunity.feature.complex

import android.util.Log
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.compose.material3.Player as MediaPlayer

@Composable
fun ComplexVideo(videoUrl: String, modifier: Modifier = Modifier) {
    var started by rememberSaveable { mutableStateOf(false) }
    Box(modifier.fillMaxWidth().aspectRatio(16f / 9f).background(Color.Black)) {
        if (started) {
            VideoPlayer(videoUrl)
        } else {
            IconButton(onClick = { started = true }, modifier = Modifier.align(Alignment.Center)) {
                Icon(Icons.Default.PlayArrow, contentDescription = stringResource(R.string.play_video), tint = Color.White)
            }
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
private fun VideoPlayer(videoUrl: String) {
    val context = LocalContext.current
    val player = remember(videoUrl) {
        ExoPlayer.Builder(
            context,
            DefaultRenderersFactory(context).setEnableDecoderFallback(true)
        ).build().apply {
            setMediaItem(MediaItem.fromUri(videoUrl))
            prepare()
            playWhenReady = true
            if (BuildConfig.DEBUG) addListener(object : Player.Listener {
                override fun onPlayerError(error: PlaybackException) {
                    Log.e("Video", "error ${error.errorCodeName}", error)
                }
                override fun onPlaybackStateChanged(state: Int) {
                    Log.d("Video", "state $state")
                }
            })
        }
    }
    DisposableEffect(player) {
        onDispose { player.release() }
    }

    var showControls by remember { mutableStateOf(true) }
    MediaPlayer(
        player = player,
        showControls = showControls,
        modifier = Modifier
            .fillMaxSize()
            .clickable { showControls = !showControls },
    )
}