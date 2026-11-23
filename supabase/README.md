# Infraestructura Backend de Recordatorios Automáticos — AgendaTenis

Esta carpeta contiene la solución completa de base de datos, funciones en la nube y automatización para el envío de recordatorios de pago por correo a los jugadores de **AgendaTenis**.

## Arquitectura

- **Base de Datos (Supabase PostgreSQL):** 
  - Gestión de jugadores con valor mensual individual y correo electrónico.
  - Registro histórico de pagos.
  - Tabla de auditoría e idempotencia `recordatorios_envios` para prevención de correos duplicados.
- **Supabase Cron (`pg_cron`):**
  - Ejecución programada diaria a las `08:00 AM America/Guayaquil` (13:00 UTC).
  - Funciona de forma completamente autónoma, incluso cuando la aplicación móvil está cerrada o en segundo plano.
- **Supabase Edge Function (`enviar-recordatorios`):**
  - Función Deno (TypeScript) que procesa los cálculos de vencimiento según la zona horaria `America/Guayaquil`.
  - Verifica las dos condiciones de recordatorio:
    1. **Aproximación de fecha:** `dias_restantes <= dias_anticipacion` (default 3 días antes).
    2. **Mismo día:** `dias_restantes == 0` (fecha de pago alcanzada).
- **Servicio de Envío (Resend):**
  - Envío transaccional confiable con verificación de aceptación (`HTTP 200/201` + `id`).
  - No se marca ningún correo como `ENVIADO` si Resend no confirma la recepción.

---

## Estructura de Archivos

```
supabase/
├── .env.example                               # Plantilla de variables de entorno (sin credenciales expuestas)
├── README.md                                  # Guía de configuración y despliegue
├── migrations/
│   └── 20261122_initial_schema.sql            # Script SQL: DDL, funciones de fecha, cron y tabla de envíos
└── functions/
    └── enviar-recordatorios/
        ├── deno.json                          # Configuración del compilador Deno
        └── index.ts                           # Código TypeScript de la Edge Function
```

---

## Pasos para Despliegue y Configuración

### 1. Requisitos Previos
1. Cuenta en [Supabase](https://supabase.com).
2. Cuenta en [Resend](https://resend.com) con una API Key (`re_...`) generada.
3. Supabase CLI instalado en tu entorno local:
   ```bash
   npm install -g supabase
   ```

### 2. Inicializar y Vincular el Proyecto Supabase
```bash
cd D:/ULEAM/2026-2-6to/AppMoviles/AgendaTenis
supabase login
supabase link --project-ref tu-project-ref
```

### 3. Aplicar las Migraciones en la Base de Datos
Ejecuta la migración para crear las tablas, índices, funciones de cálculo y programar el cron diario:
```bash
supabase db push
```
*O copia y ejecuta el contenido de `supabase/migrations/20261122_initial_schema.sql` en el SQL Editor de tu panel de Supabase.*

### 4. Configurar Secretos en Supabase Edge Functions
Nunca agregues claves privadas a los archivos del repositorio ni a la app Android. Configúralas con el CLI:
```bash
supabase secrets set RESEND_API_KEY=re_tu_clave_secreta_de_resend
supabase secrets set RESEND_FROM_EMAIL="AgendaTenis <onboarding@resend.dev>"
```

### 5. Desplegar la Edge Function
```bash
supabase functions deploy enviar-recordatorios --no-verify-jwt
```

---

## Verificación y Pruebas Manuales

### Probar la Edge Function vía cURL / HTTP
Puedes invocar la Edge Function para verificar la lógica de cálculo y envío:
```bash
curl -i --location --request POST 'https://tu-project-ref.supabase.co/functions/v1/enviar-recordatorios' \
  --header 'Authorization: Bearer tu-supabase-anon-key' \
  --header 'Content-Type: application/json' \
  --data '{"test": true}'
```

Respuesta esperada:
```json
{
  "status": "ok",
  "evaluados": 2,
  "enviados": 2,
  "fallidos": 0,
  "zona_horaria": "America/Guayaquil",
  "detalles": [
    {
      "jugador": "Angel Veliz",
      "correo": "angel.veliz@ejemplo.com",
      "tipo": "PROXIMO",
      "estado": "ENVIADO",
      "proveedor_id": "49a3999c-0ce1-4ea6-ab68-af69c11dfb6a"
    }
  ]
}
```

### Prevención de Duplicados (Idempotencia)
Si se ejecuta nuevamente la función en el mismo día para el mismo ciclo:
- La consulta SQL y la restricción `UNIQUE (jugador_id, ciclo_fecha_pago, tipo_recordatorio)` detectan que ya existe un registro con `estado = 'ENVIADO'`.
- La función omite el envío a Resend, protegiendo al jugador de recibir mensajes repetidos y ahorrando cuota de envío.
