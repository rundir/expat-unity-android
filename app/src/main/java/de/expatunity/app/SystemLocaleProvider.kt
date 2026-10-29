package de.expatunity.app

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.expatunity.core.domain.LocaleProvider
import jakarta.inject.Inject
import java.util.Locale

class SystemLocaleProvider @Inject constructor() : LocaleProvider {
    private val supported = setOf("ru", "de", "en", "uk")
    override fun apiLocale(): String {
        val lang = Locale.getDefault().language
            ?: Locale.getDefault().language
        return if (lang in supported) lang else "de"
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class LocaleModule {
    @Binds
    abstract fun bind(impl: SystemLocaleProvider): LocaleProvider
}