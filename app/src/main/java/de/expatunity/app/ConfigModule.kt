package de.expatunity.app

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.expatunity.core.network.BaseUrl

@Module
@InstallIn(SingletonComponent::class)
object ConfigModule {
    @Provides
    @BaseUrl
    fun baseUrl(): String = BuildConfig.BASE_URL
}