package de.expatunity.feature.complex.video

import androidx.lifecycle.ViewModel
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import dagger.hilt.android.lifecycle.HiltViewModel
import de.expatunity.feature.complex.di.MediaItemFactory
import de.expatunity.feature.complex.di.PlayerFactory
import javax.inject.Inject

@UnstableApi
@HiltViewModel
class VideoPlayerViewModel @Inject constructor(
    factory: PlayerFactory,
    private val mediaItemFactory: MediaItemFactory
) : ViewModel() {

    val player: Player = factory.create()

    private var currentUrl: String? = null

    private var resumeOnStart = false

    fun load(url: String) {
        if (url == currentUrl) return   // при повороте не перезагружаем
        currentUrl = url
        player.setMediaItem(mediaItemFactory.create(url))
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