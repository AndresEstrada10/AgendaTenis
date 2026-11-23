-- ========================================================================
-- Schema de Base de Datos y Automatización para AgendaTenis
-- Base de datos: PostgreSQL en Supabase
-- Zona horaria de referencia: America/Guayaquil (UTC-5)
-- ========================================================================

-- Extensiones requeridas para generación de UUIDs, Cron y peticiones HTTP
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS pg_cron;
CREATE EXTENSION IF NOT EXISTS pg_net;

-- ------------------------------------------------------------------------
-- 1. Tabla: jugadores
-- Cada jugador tiene su propio correo y cuota mensual individual.
-- Se elimina definitivamente el concepto de Planes.
-- Tanto el COACH como el ADMIN pueden gestionar esta información.
-- ------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.jugadores (
    id TEXT PRIMARY KEY,
    nombre TEXT NOT NULL,
    telefono TEXT NOT NULL,
    correo TEXT NOT NULL,
    fecha_inicio DATE NOT NULL,
    valor_mensual NUMERIC(10, 2) NOT NULL CHECK (valor_mensual > 0),
    coach_id TEXT NOT NULL,
    estado TEXT NOT NULL DEFAULT 'ACTIVO' CHECK (estado IN ('ACTIVO', 'INACTIVO')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT (NOW() AT TIME ZONE 'America/Guayaquil'),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT (NOW() AT TIME ZONE 'America/Guayaquil')
);

CREATE INDEX IF NOT EXISTS idx_jugadores_coach_estado ON public.jugadores (coach_id, estado);
CREATE INDEX IF NOT EXISTS idx_jugadores_correo ON public.jugadores (correo);

-- ------------------------------------------------------------------------
-- 2. Tabla: pagos
-- Registros de pago históricos realizados por los jugadores.
-- ------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.pagos (
    id TEXT PRIMARY KEY,
    jugador_id TEXT NOT NULL REFERENCES public.jugadores(id) ON DELETE RESTRICT,
    fecha DATE NOT NULL,
    monto NUMERIC(10, 2) NOT NULL CHECK (monto > 0),
    observacion TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT (NOW() AT TIME ZONE 'America/Guayaquil')
);

CREATE INDEX IF NOT EXISTS idx_pagos_jugador_fecha ON public.pagos (jugador_id, fecha ASC);

-- ------------------------------------------------------------------------
-- 3. Tabla: configuraciones
-- Configuraciones por usuario (días de anticipación para recordatorios: 1-7, default 3)
-- ------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.configuraciones (
    usuario_id TEXT PRIMARY KEY,
    dias_anticipacion INT NOT NULL DEFAULT 3 CHECK (dias_anticipacion BETWEEN 1 AND 7)
);

-- ------------------------------------------------------------------------
-- 4. Tabla: recordatorios_envios (Auditoría, Idempotencia y Prevención de Duplicados)
-- Registra cada notificación enviada o intentada.
-- La restricción UNIQUE (jugador_id, ciclo_fecha_pago, tipo_recordatorio)
-- garantiza matemáticamente que NO se envíen correos duplicados para el mismo ciclo.
-- ------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.recordatorios_envios (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    jugador_id TEXT NOT NULL REFERENCES public.jugadores(id) ON DELETE CASCADE,
    tipo_recordatorio TEXT NOT NULL CHECK (tipo_recordatorio IN ('PROXIMO', 'HOY')),
    ciclo_fecha_pago DATE NOT NULL,
    correo_destinatario TEXT NOT NULL,
    valor_mensual NUMERIC(10, 2) NOT NULL,
    asunto TEXT NOT NULL,
    mensaje TEXT NOT NULL,
    estado TEXT NOT NULL DEFAULT 'PENDIENTE' CHECK (estado IN ('PENDIENTE', 'ENVIADO', 'FALLIDO')),
    proveedor TEXT NOT NULL DEFAULT 'resend',
    proveedor_id TEXT, -- ID de confirmación retornado por Resend
    error_mensaje TEXT,
    intentos INT NOT NULL DEFAULT 1,
    creado_en TIMESTAMPTZ NOT NULL DEFAULT (NOW() AT TIME ZONE 'America/Guayaquil'),
    enviado_en TIMESTAMPTZ,
    CONSTRAINT uq_recordatorio_ciclo UNIQUE (jugador_id, ciclo_fecha_pago, tipo_recordatorio)
);

CREATE INDEX IF NOT EXISTS idx_recordatorios_envios_estado ON public.recordatorios_envios (estado);
CREATE INDEX IF NOT EXISTS idx_recordatorios_envios_ciclo ON public.recordatorios_envios (jugador_id, ciclo_fecha_pago);

-- ------------------------------------------------------------------------
-- 5. Función de cálculo de fechas de pago (RN-03, RN-04, RN-05)
-- Ancla: primer pago o fecha_inicio.
-- Proxima fecha: fecha_ancla + nro_pagos meses.
-- ------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION public.calcular_proxima_fecha_pago(p_jugador_id TEXT)
RETURNS TABLE (
    fecha_ancla DATE,
    nro_pagos INT,
    proxima_fecha DATE
)
LANGUAGE plpgsql
STABLE
AS $$
DECLARE
    v_fecha_inicio DATE;
    v_primer_pago DATE;
    v_cant_pagos INT;
    v_ancla DATE;
    v_proxima DATE;
BEGIN
    SELECT fecha_inicio INTO v_fecha_inicio
    FROM public.jugadores
    WHERE id = p_jugador_id;

    IF v_fecha_inicio IS NULL THEN
        RETURN;
    END IF;

    SELECT MIN(fecha), COUNT(*)
    INTO v_primer_pago, v_cant_pagos
    FROM public.pagos
    WHERE jugador_id = p_jugador_id;

    v_ancla := COALESCE(v_primer_pago, v_fecha_inicio);
    -- Suma de meses preservando fin de mes según reglas estándar de calendario
    v_proxima := (v_ancla + (COALESCE(v_cant_pagos, 0) || ' months')::INTERVAL)::DATE;

    RETURN QUERY SELECT v_ancla, COALESCE(v_cant_pagos, 0), v_proxima;
END;
$$;

-- ------------------------------------------------------------------------
-- 6. Vista / Función: jugadores_pendientes_recordatorio
-- Evalúa las fechas en la zona horaria America/Guayaquil.
-- Excluye envíos que ya han sido confirmados como ENVIADO por el proveedor.
-- ------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION public.obtener_jugadores_para_recordatorio()
RETURNS TABLE (
    jugador_id TEXT,
    nombre TEXT,
    correo TEXT,
    valor_mensual NUMERIC(10, 2),
    coach_id TEXT,
    fecha_ancla DATE,
    proxima_fecha DATE,
    dias_restantes INT,
    tipo_recordatorio TEXT,
    asunto TEXT,
    motivo TEXT
)
LANGUAGE plpgsql
STABLE
AS $$
DECLARE
    v_hoy DATE;
BEGIN
    -- Fecha actual en zona horaria America/Guayaquil
    v_hoy := (NOW() AT TIME ZONE 'America/Guayaquil')::DATE;

    RETURN QUERY
    WITH datos_calculados AS (
        SELECT
            j.id AS j_id,
            j.nombre AS j_nombre,
            j.correo AS j_correo,
            j.valor_mensual AS j_valor,
            j.coach_id AS j_coach,
            c.fecha_ancla AS j_ancla,
            c.proxima_fecha AS j_proxima,
            (c.proxima_fecha - v_hoy) AS j_dias_restantes,
            COALESCE(cfg.dias_anticipacion, 3) AS j_dias_anticipacion
        FROM public.jugadores j
        CROSS JOIN LATERAL public.calcular_proxima_fecha_pago(j.id) c
        LEFT JOIN public.configuraciones cfg ON cfg.usuario_id = j.coach_id
        WHERE j.estado = 'ACTIVO'
    ),
    candidatos AS (
        SELECT
            d.j_id,
            d.j_nombre,
            d.j_correo,
            d.j_valor,
            d.j_coach,
            d.j_ancla,
            d.j_proxima,
            d.j_dias_restantes,
            CASE
                WHEN d.j_dias_restantes = 0 THEN 'HOY'
                WHEN d.j_dias_restantes > 0 AND d.j_dias_restantes <= d.j_dias_anticipacion THEN 'PROXIMO'
                ELSE NULL
            END AS tipo_rec,
            CASE
                WHEN d.j_dias_restantes = 0 THEN 'Tu cuota mensual de tenis vence hoy - AgendaTenis'
                WHEN d.j_dias_restantes > 0 AND d.j_dias_restantes <= d.j_dias_anticipacion THEN 'Próximo vencimiento de tu cuota de tenis - AgendaTenis'
                ELSE NULL
            END AS asunto_rec,
            CASE
                WHEN d.j_dias_restantes = 0 THEN 'Hoy es tu fecha mensual de pago.'
                WHEN d.j_dias_restantes > 0 AND d.j_dias_restantes <= d.j_dias_anticipacion THEN 'Se aproxima tu fecha mensual de pago (en ' || d.j_dias_restantes || ' día(s)).'
                ELSE NULL
            END AS motivo_rec
        FROM datos_calculados d
    )
    SELECT
        c.j_id,
        c.j_nombre,
        c.j_correo,
        c.j_valor,
        c.j_coach,
        c.j_ancla,
        c.j_proxima,
        c.j_dias_restantes,
        c.tipo_rec,
        c.asunto_rec,
        c.motivo_rec
    FROM candidatos c
    WHERE c.tipo_rec IS NOT NULL
      -- Prevenir duplicados: verificar que no exista ya un registro ENVIADO para este ciclo y tipo
      AND NOT EXISTS (
          SELECT 1
          FROM public.recordatorios_envios re
          WHERE re.jugador_id = c.j_id
            AND re.ciclo_fecha_pago = c.j_proxima
            AND re.tipo_recordatorio = c.tipo_rec
            AND re.estado = 'ENVIADO'
      );
END;
$$;

-- ------------------------------------------------------------------------
-- 7. Funciones para gestionar el ciclo de vida del envío seguro
-- ------------------------------------------------------------------------

-- Registrar o actualizar intento
CREATE OR REPLACE FUNCTION public.registrar_intento_envio(
    p_jugador_id TEXT,
    p_tipo TEXT,
    p_ciclo DATE,
    p_correo TEXT,
    p_valor NUMERIC,
    p_asunto TEXT,
    p_mensaje TEXT
)
RETURNS UUID
LANGUAGE plpgsql
AS $$
DECLARE
    v_id UUID;
BEGIN
    INSERT INTO public.recordatorios_envios (
        jugador_id,
        tipo_recordatorio,
        ciclo_fecha_pago,
        correo_destinatario,
        valor_mensual,
        asunto,
        mensaje,
        estado,
        intentos
    ) VALUES (
        p_jugador_id,
        p_tipo,
        p_ciclo,
        p_correo,
        p_valor,
        p_asunto,
        p_mensaje,
        'PENDIENTE',
        1
    )
    ON CONFLICT (jugador_id, ciclo_fecha_pago, tipo_recordatorio)
    DO UPDATE SET
        intentos = recordatorios_envios.intentos + 1,
        creado_en = (NOW() AT TIME ZONE 'America/Guayaquil'),
        estado = 'PENDIENTE',
        error_mensaje = NULL
    RETURNING id INTO v_id;

    RETURN v_id;
END;
$$;

-- Confirmar éxito solo cuando el proveedor Resend ha aceptado y devuelto ID
CREATE OR REPLACE FUNCTION public.confirmar_envio_exitoso(
    p_envio_id UUID,
    p_proveedor_id TEXT
)
RETURNS VOID
LANGUAGE plpgsql
AS $$
BEGIN
    UPDATE public.recordatorios_envios
    SET estado = 'ENVIADO',
        proveedor_id = p_proveedor_id,
        enviado_en = (NOW() AT TIME ZONE 'America/Guayaquil'),
        error_mensaje = NULL
    WHERE id = p_envio_id;
END;
$$;

-- Marcar fallido para reintentos posteriores sin marcar como falso positivo
CREATE OR REPLACE FUNCTION public.marcar_envio_fallido(
    p_envio_id UUID,
    p_error TEXT
)
RETURNS VOID
LANGUAGE plpgsql
AS $$
BEGIN
    UPDATE public.recordatorios_envios
    SET estado = 'FALLIDO',
        error_mensaje = p_error
    WHERE id = p_envio_id;
END;
$$;

-- ------------------------------------------------------------------------
-- 8. Programación automática con Supabase Cron (pg_cron)
-- Se ejecuta todos los días a las 08:00 AM hora de Guayaquil (13:00 UTC)
-- Funciona independientemente de que la app Android esté cerrada o abierta.
-- ------------------------------------------------------------------------
-- Desprogramar si ya existía para evitar duplicidad de cron
DO $$
BEGIN
    PERFORM cron.unschedule('revisar-y-enviar-recordatorios-diarios');
EXCEPTION WHEN OTHERS THEN
    NULL;
END;
$$;

SELECT cron.schedule(
    'revisar-y-enviar-recordatorios-diarios',
    '0 13 * * *', -- 13:00 UTC = 08:00 AM America/Guayaquil (UTC-5)
    $$
    SELECT net.http_post(
        url := current_setting('app.settings.supabase_url', true) || '/functions/v1/enviar-recordatorios',
        headers := jsonb_build_object(
            'Content-Type', 'application/json',
            'Authorization', 'Bearer ' || current_setting('app.settings.service_role_key', true)
        ),
        body := '{"origen": "cron_diario"}'::jsonb
    );
    $$
);
