package de.expatunity.core.testing

import de.expatunity.core.domain.LocaleProvider

class FakeLocaleProvider: LocaleProvider {
    var locale = "de"
    override fun apiLocale(): String {
        return locale
    }
}