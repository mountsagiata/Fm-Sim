package com.mountsa.fmsimulation.utils

import android.content.Context
import android.content.res.Configuration
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocaleManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val preferences = context.getSharedPreferences("locale_settings", Context.MODE_PRIVATE)
    private val _language = MutableStateFlow(preferences.getString(KEY_LANGUAGE, "system") ?: "system")
    val language: StateFlow<String> = _language

    init {
        applyLanguage(_language.value)
    }

    fun setLanguage(languageTag: String) {
        if (languageTag !in SUPPORTED_LANGUAGES) return
        _language.value = languageTag
        preferences.edit().putString(KEY_LANGUAGE, languageTag).apply()
        applyLanguage(languageTag)
    }

    private fun applyLanguage(languageTag: String) {
        val locale = if (languageTag == "system") Locale.getDefault() else Locale.forLanguageTag(languageTag)
        Locale.setDefault(locale)
        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)
        context.resources.updateConfiguration(configuration, context.resources.displayMetrics)
    }

    companion object {
        val SUPPORTED_LANGUAGES = setOf("system", "en", "id", "pt", "ja")
        private const val KEY_LANGUAGE = "language"
    }
}
