package com.example.util

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "qr_settings")

enum class ThemeOption {
    SYSTEM, LIGHT, DARK
}

class PreferenceManager(private val context: Context) {

    companion object {
        val KEY_THEME = stringPreferencesKey("app_theme")
        val KEY_DEFAULT_EXPORT = stringPreferencesKey("default_export_format")
        val KEY_DEFAULT_ECC = stringPreferencesKey("default_ecc_level")
    }

    val themeFlow: Flow<ThemeOption> = context.dataStore.data.map { prefs ->
        val raw = prefs[KEY_THEME] ?: ThemeOption.SYSTEM.name
        try { ThemeOption.valueOf(raw) } catch (_: Exception) { ThemeOption.SYSTEM }
    }

    val defaultExportFlow: Flow<ExportUtils.ExportFormat> = context.dataStore.data.map { prefs ->
        val raw = prefs[KEY_DEFAULT_EXPORT] ?: ExportUtils.ExportFormat.PNG.name
        try { ExportUtils.ExportFormat.valueOf(raw) } catch (_: Exception) { ExportUtils.ExportFormat.PNG }
    }

    val defaultEccFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_DEFAULT_ECC] ?: "M"
    }

    suspend fun setTheme(option: ThemeOption) {
        context.dataStore.edit { prefs ->
            prefs[KEY_THEME] = option.name
        }
    }

    suspend fun setDefaultExport(format: ExportUtils.ExportFormat) {
        context.dataStore.edit { prefs ->
            prefs[KEY_DEFAULT_EXPORT] = format.name
        }
    }

    suspend fun setDefaultEcc(ecc: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_DEFAULT_ECC] = ecc
        }
    }
}
