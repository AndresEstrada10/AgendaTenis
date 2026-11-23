package com.example.agendatenis.data.repository

import com.example.agendatenis.model.TemaApp
import kotlinx.coroutines.flow.Flow

/** Preferencia del dispositivo, independiente de cuentas y sesiones. null sigue al sistema. */
interface ThemeRepository {
    val tema: Flow<TemaApp?>
    suspend fun guardarTema(tema: TemaApp?)
}
