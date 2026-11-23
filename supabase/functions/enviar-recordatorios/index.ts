// ========================================================================
// Supabase Edge Function: enviar-recordatorios
// Runtime: Deno (TypeScript)
// Propósito: Envío automático de correos por aproximación y llegada de fecha de pago
// Proveedor de correo: Resend
// Zona horaria de cálculo: America/Guayaquil (UTC-5)
// ========================================================================

import { serve } from "https://deno.land/std@0.168.0/http/server.ts";
import { createClient } from "https://esm.sh/@supabase/supabase-js@2.39.8";

interface JugadorRecordatorio {
  jugador_id: string;
  nombre: string;
  correo: string;
  valor_mensual: number;
  coach_id: string;
  fecha_ancla: string;
  proxima_fecha: string;
  dias_restantes: number;
  tipo_recordatorio: "PROXIMO" | "HOY";
  asunto: string;
  motivo: string;
}

interface ResendSuccessResponse {
  id: string;
}

interface ResendErrorResponse {
  statusCode?: number;
  message?: string;
  name?: string;
}

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
};

serve(async (req: Request) => {
  // Manejo de CORS Preflight
  if (req.method === "OPTIONS") {
    return new Response("ok", { headers: corsHeaders });
  }

  try {
    const supabaseUrl = Deno.env.get("SUPABASE_URL");
    const supabaseServiceKey = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY");
    const resendApiKey = Deno.env.get("RESEND_API_KEY");
    const senderEmail = Deno.env.get("RESEND_FROM_EMAIL") || "AgendaTenis <onboarding@resend.dev>";

    if (!supabaseUrl || !supabaseServiceKey) {
      return new Response(
        JSON.stringify({ error: "Variables de entorno de Supabase no configuradas" }),
        { status: 500, headers: { ...corsHeaders, "Content-Type": "application/json" } }
      );
    }

    if (!resendApiKey) {
      return new Response(
        JSON.stringify({
          error: "RESEND_API_KEY no configurada. Configure la clave en Supabase Secrets.",
        }),
        { status: 500, headers: { ...corsHeaders, "Content-Type": "application/json" } }
      );
    }

    // Cliente Supabase con privilegios administrativos (service_role)
    const supabase = createClient(supabaseUrl, supabaseServiceKey);

    // 1. Obtener los candidatos a recordatorio que no hayan sido enviados en este ciclo
    const { data: candidatos, error: rpcError } = await supabase.rpc(
      "obtener_jugadores_para_recordatorio"
    );

    if (rpcError) {
      console.error("Error al ejecutar obtener_jugadores_para_recordatorio:", rpcError);
      return new Response(
        JSON.stringify({ error: "Error al calcular recordatorios en base de datos", detalle: rpcError.message }),
        { status: 500, headers: { ...corsHeaders, "Content-Type": "application/json" } }
      );
    }

    const listaCandidatos: JugadorRecordatorio[] = candidatos || [];
    console.log(`Evaluando ${listaCandidatos.length} candidato(s) para envío de recordatorios en America/Guayaquil...`);

    const resultados = [];
    let exitosos = 0;
    let fallidos = 0;

    for (const candidato of listaCandidatos) {
      const valorFormateado = Number(candidato.valor_mensual).toFixed(2);

      // Contenido del correo en texto plano y HTML
      const textoPlano = `Hola ${candidato.nombre},\n\n` +
        `Te recordamos el estado de tu cuota mensual en AgendaTenis.\n\n` +
        `Motivo: ${candidato.motivo}\n` +
        `Fecha de vencimiento: ${candidato.proxima_fecha}\n` +
        `Valor mensual individual: $${valorFormateado}\n\n` +
        `Por favor, acércate a tu coach o realiza tu pago para mantener tu entrenamiento al día.\n\n` +
        `Saludos,\nEquipo AgendaTenis`;

      const htmlBody = `
        <!DOCTYPE html>
        <html>
        <head>
          <meta charset="utf-8">
          <style>
            body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; background-color: #F8F9FA; margin: 0; padding: 20px; }
            .container { max-width: 560px; margin: 0 auto; background: #ffffff; border-radius: 12px; overflow: hidden; border: 1px solid #E2E8F0; }
            .header { background-color: #2E7D32; padding: 24px; text-align: center; color: #ffffff; }
            .header h1 { margin: 0; font-size: 24px; font-weight: 700; }
            .content { padding: 28px 24px; color: #1E293B; line-height: 1.6; }
            .badge { display: inline-block; padding: 6px 12px; border-radius: 6px; font-size: 13px; font-weight: bold; margin-bottom: 16px; }
            .badge-hoy { background-color: #FEE2E2; color: #DC2626; border: 1px solid #F87171; }
            .badge-prox { background-color: #FEF3C7; color: #D97706; border: 1px solid #FCD34D; }
            .card-info { background-color: #F1F5F9; border-radius: 8px; padding: 16px; margin: 20px 0; border-left: 4px solid #2E7D32; }
            .info-row { display: flex; justify-content: space-between; margin-bottom: 8px; font-size: 15px; }
            .info-label { color: #64748B; font-weight: 500; }
            .info-val { font-weight: 700; color: #0F172A; }
            .cuota-destacada { font-size: 22px; color: #2E7D32; font-weight: 800; }
            .footer { padding: 18px 24px; background-color: #F8FAFC; text-align: center; font-size: 12px; color: #94A3B8; border-top: 1px solid #E2E8F0; }
          </style>
        </head>
        <body>
          <div class="container">
            <div class="header">
              <h1>AgendaTenis</h1>
              <p style="margin: 4px 0 0 0; opacity: 0.9; font-size: 14px;">Gestión de Clases y Entrenamientos</p>
            </div>
            <div class="content">
              <span class="badge ${candidato.tipo_recordatorio === 'HOY' ? 'badge-hoy' : 'badge-prox'}">
                ${candidato.tipo_recordatorio === 'HOY' ? 'VENCE HOY' : 'PRÓXIMO VENCIMIENTO'}
              </span>
              <p style="font-size: 16px;">Estimado/a <strong>${candidato.nombre}</strong>,</p>
              <p>${candidato.motivo}</p>

              <div class="card-info">
                <div class="info-row">
                  <span class="info-label">Jugador:</span>
                  <span class="info-val">${candidato.nombre}</span>
                </div>
                <div class="info-row">
                  <span class="info-label">Fecha de vencimiento:</span>
                  <span class="info-val">${candidato.proxima_fecha}</span>
                </div>
                <div class="info-row" style="margin-top: 12px; border-top: 1px dashed #CBD5E1; padding-top: 10px;">
                  <span class="info-label" style="align-self: center;">Valor mensual individual:</span>
                  <span class="cuota-destacada">$${valorFormateado}</span>
                </div>
              </div>

              <p style="font-size: 14px; color: #475569;">
                Por favor, coordina el registro de tu pago con tu coach responsable para mantener tu cupo y horarios asignados.
              </p>
            </div>
            <div class="footer">
              AgendaTenis &bull; Notificación generada automáticamente
            </div>
          </div>
        </body>
        </html>
      `;

      // 2. Registrar intento en estado PENDIENTE (Idempotencia y trazabilidad)
      const { data: envioId, error: regError } = await supabase.rpc("registrar_intento_envio", {
        p_jugador_id: candidato.jugador_id,
        p_tipo: candidato.tipo_recordatorio,
        p_ciclo: candidato.proxima_fecha,
        p_correo: candidato.correo,
        p_valor: candidato.valor_mensual,
        p_asunto: candidato.asunto,
        p_mensaje: textoPlano,
      });

      if (regError) {
        console.error(`Error al registrar intento para ${candidato.correo}:`, regError);
        continue;
      }

      // 3. Enviar correo mediante la API REST de Resend
      try {
        const resendResponse = await fetch("https://api.resend.com/emails", {
          method: "POST",
          headers: {
            "Authorization": `Bearer ${resendApiKey}`,
            "Content-Type": "application/json",
          },
          body: JSON.stringify({
            from: senderEmail,
            to: [candidato.correo],
            subject: candidato.asunto,
            html: htmlBody,
            text: textoPlano,
          }),
        });

        const resendData = await resendResponse.json();

        // 4. Verificación estricta: NO marcar como enviado si el proveedor no confirma aceptación
        if (resendResponse.ok && resendData?.id) {
          // Confirmación positiva de Resend
          await supabase.rpc("confirmar_envio_exitoso", {
            p_envio_id: envioId,
            p_proveedor_id: resendData.id,
          });

          exitosos++;
          resultados.push({
            jugador: candidato.nombre,
            correo: candidato.correo,
            tipo: candidato.tipo_recordatorio,
            estado: "ENVIADO",
            proveedor_id: resendData.id,
          });
        } else {
          // Rechazo o error de Resend
          const errorMsg = (resendData as ResendErrorResponse)?.message || `HTTP ${resendResponse.status}`;
          console.error(`Fallo de entrega de Resend para ${candidato.correo}:`, errorMsg);

          await supabase.rpc("marcar_envio_fallido", {
            p_envio_id: envioId,
            p_error: errorMsg,
          });

          fallidos++;
          resultados.push({
            jugador: candidato.nombre,
            correo: candidato.correo,
            tipo: candidato.tipo_recordatorio,
            estado: "FALLIDO",
            error: errorMsg,
          });
        }
      } catch (fetchError: any) {
        console.error(`Excepción de red al contactar Resend para ${candidato.correo}:`, fetchError);
        await supabase.rpc("marcar_envio_fallido", {
          p_envio_id: envioId,
          p_error: fetchError.message || "Excepción de red",
        });

        fallidos++;
        resultados.push({
          jugador: candidato.nombre,
          correo: candidato.correo,
          tipo: candidato.tipo_recordatorio,
          estado: "FALLIDO",
          error: fetchError.message,
        });
      }
    }

    return new Response(
      JSON.stringify({
        status: "ok",
        evaluados: listaCandidatos.length,
        enviados: exitosos,
        fallidos: fallidos,
        zona_horaria: "America/Guayaquil",
        detalles: resultados,
      }),
      { status: 200, headers: { ...corsHeaders, "Content-Type": "application/json" } }
    );
  } catch (err: any) {
    console.error("Error no controlado en Edge Function:", err);
    return new Response(
      JSON.stringify({ error: "Error interno en el procesamiento", detalle: err.message }),
      { status: 500, headers: { ...corsHeaders, "Content-Type": "application/json" } }
    );
  }
});
