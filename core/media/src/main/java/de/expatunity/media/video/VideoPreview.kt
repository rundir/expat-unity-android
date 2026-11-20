package de.expatunity.media.video

import android.content.pm.ActivityInfo
import androidx.activity.compose.LocalActivity
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.media3.common.Player
import androidx.media3.common.util.ExperimentalApi
import androidx.media3.common.util.UnstableApi
import de.expatunity.core.media.R
import androidx.media3.ui.compose.material3.Player as MediaPlayer

@Composable
fun VideoPreview(videoUrl: String, modifier: Modifier = Modifier) {
    var started by rememberSaveable { mutableStateOf(false) }
    Box(
        modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .background(Color.Black)
    ) {
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

    var fullscreen by rememberSaveable { mutableStateOf(false) }

    if (fullscreen) {
        Dialog(
            onDismissRequest = { fullscreen = false },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false,
            ),
        ) {
            ForceLandscape()
            ImmersiveSystemBars()
            PlayerContent(
                player = viewModel.player,
                isFullscreen = true,
                onToggleFullscreen = { fullscreen = false },
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
            )
        }
    } else {
        PlayerContent(
            player = viewModel.player,
            isFullscreen = false,
            onToggleFullscreen = { fullscreen = true },
            modifier = Modifier.fillMaxSize(),
        )
    }

}

@OptIn(ExperimentalApi::class)
@Composable
private fun PlayerContent(
    player: Player,
    isFullscreen: Boolean,
    onToggleFullscreen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showControls by remember { mutableStateOf(true) }
    Box(modifier) {
        MediaPlayer(
            player = player,
            showControls = showControls,
            modifier = Modifier
                .fillMaxSize()
                .clickable { showControls = !showControls },
        )
        if (showControls) {
            IconButton(
                onClick = onToggleFullscreen,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .then(
                        if (isFullscreen) Modifier.windowInsetsPadding(WindowInsets.safeDrawing)
                        else Modifier
                    ),
            ) {
                Icon(
                    painter = painterResource(
                        if (isFullscreen) R.drawable.ic_fullscreen_exit else R.drawable.ic_fullscreen
                    ),
                    contentDescription = stringResource(
                        if (isFullscreen) R.string.exit_fullscreen else R.string.fullscreen
                    ),
                    tint = Color.White,
                )
            }
        }
    }
}

@Composable
private fun ImmersiveSystemBars() {
    val window = (LocalView.current.parent as? DialogWindowProvider)?.window ?: return
    DisposableEffect(window) {
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller.hide(WindowInsetsCompat.Type.systemBars())
        onDispose { controller.show(WindowInsetsCompat.Type.systemBars()) }
    }
}

@Composable
private fun ForceLandscape() {
    val activity = LocalActivity.current ?: return
    DisposableEffect(activity) {
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        onDispose {
            // при повороте активити пересоздаётся — тогда ориентацию не трогаем
            if (!activity.isChangingConfigurations) {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            }
        }
    }
}
