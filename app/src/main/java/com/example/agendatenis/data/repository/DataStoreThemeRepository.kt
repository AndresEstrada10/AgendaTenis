package com.example.agendatenis.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.agendatenis.model.TemaApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.IOException

// Una sola instancia por archivo en todo el proceso.
val Context.themeDataStore: DataStore<Preferences> by preferencesDataStore(name = "apariencia")

class DataStoreThemeRepository(
    private val dataStore: DataStore<Preferences>
) : ThemeRepository {
    private val temaKey = stringPreferencesKey("tema_global")

    override val tema = dataStore.data.catch { error ->
        if (error is IOException) emit(emptyPreferences()) else throw error
    }.map { preferences ->
        TemaApp.entries.firstOrNull { it.name == preferences[temaKey] }
    }

    override suspend fun guardarTema(tema: TemaApp?) = withContext(Dispatchers.IO) {
        dataStore.edit { preferences ->
            if (tema == null) preferences.remove(temaKey)
            else preferences[temaKey] = tema.name
        }
        Unit
    }
}
