package de.expatunity.feature.complex

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@UnstableApi
@HiltViewModel
class VideoPlayerViewModel @Inject constructor(
    @ApplicationContext context: Context,
) : ViewModel() {

    val player: ExoPlayer = ExoPlayer.Builder(
        context,
        DefaultRenderersFactory(context).setEnableDecoderFallback(true),
    ).build()

    private var currentUrl: String? = null

    private var resumeOnStart = false

    fun load(url: String) {
        if (url == currentUrl) return   // при повороте не перезагружаем
        currentUrl = url
        player.setMediaItem(MediaItem.fromUri(url))
        player.prepare()
        player.playWhenReady = true
    }

    override fun onCleared() {
        player.release()
    }

    fun onStart() {
        if (resumeOnStart) player.play()
    }

    fun onStop() {
        resumeOnStart = player.isPlaying
        player.pause()
    }
}