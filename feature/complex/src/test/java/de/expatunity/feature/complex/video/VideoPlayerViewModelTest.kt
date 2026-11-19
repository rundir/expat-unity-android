package de.expatunity.feature.complex.video

import androidx.media3.common.Player
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test

class VideoPlayerViewModelTest {

    private val player = mockk<Player>(relaxed = true)
    private val viewModel = VideoPlayerViewModel(
        factory = { player },
        mediaItemFactory = { mockk(relaxed = true) },
    )

    @Test
    fun `load prepares and plays the media`() {
        viewModel.load("https://example.com/video.mp4")

        verify {
            player.setMediaItem(any())
            player.prepare()
            player.playWhenReady = true
        }
    }

    @Test
    fun `load with same url does not reload`() {
        viewModel.load("https://example.com/video.mp4")
        viewModel.load("https://example.com/video.mp4")

        verify(exactly = 1) { player.prepare() }
    }

    @Test
    fun `stop pauses and start resumes when it was playing`() {
        every { player.isPlaying } returns true

        viewModel.onStop()
        viewModel.onStart()

        verify { player.pause() }
        verify { player.play() }
    }

    @Test
    fun `start does not resume when it was paused`() {
        every { player.isPlaying } returns false

        viewModel.onStop()
        viewModel.onStart()

        verify(exactly = 0) { player.play() }
    }
}