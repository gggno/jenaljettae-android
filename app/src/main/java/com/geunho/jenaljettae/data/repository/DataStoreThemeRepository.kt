package com.geunho.jenaljettae.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.geunho.jenaljettae.domain.model.AppThemeMode
import com.geunho.jenaljettae.domain.repository.ThemeRepository
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore(name = "jenaljettae_settings")

// MARK: - DataStore Theme Repository | 화면 테마 저장
class DataStoreThemeRepository(context: Context) : ThemeRepository {
    private val dataStore = context.applicationContext.settingsDataStore
    private val themeKey = stringPreferencesKey("appThemeMode")

    // MARK: - Selected Theme | 저장된 테마를 Flow로 제공
    override val selectedTheme: Flow<AppThemeMode> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            val saved = preferences[themeKey]
            AppThemeMode.entries.firstOrNull { it.name == saved } ?: AppThemeMode.SYSTEM
        }

    // MARK: - Save Theme | 테마 선택값 영구 저장
    override suspend fun saveTheme(theme: AppThemeMode) {
        dataStore.edit { preferences -> preferences[themeKey] = theme.name }
    }
}
