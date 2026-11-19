package de.expatunity.feature.complex.di

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext

fun interface PlayerFactory { fun create(): Player }

@Module
@InstallIn(ViewModelComponent::class)
object PlayerModule {
    @OptIn(UnstableApi::class)
    @Provides
    fun playerFactory(@ApplicationContext context: Context) = PlayerFactory {
        ExoPlayer.Builder(context, DefaultRenderersFactory(context).setEnableDecoderFallback(true)).build()
    }
}