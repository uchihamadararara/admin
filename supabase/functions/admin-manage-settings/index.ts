// Supabase Edge Function: admin-manage-settings
// Manages platform remote configurations and app announcements safely.

import { serve } from "https://deno.land/std@0.177.0/http/server.ts";
import { createClient } from "https://esm.sh/@supabase/supabase-js@2.39.0";

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
};

serve(async (req: Request) => {
  if (req.method === "OPTIONS") return new Response("ok", { headers: corsHeaders });

  try {
    const supabaseUrl = Deno.env.get("SUPABASE_URL") ?? "";
    const supabaseServiceKey = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY") ?? "";
    const adminClient = createClient(supabaseUrl, supabaseServiceKey);

    const body = await req.json();
    const { action, key, value, description, announcement, adminEmail } = body;

    if (action === "SET_CONFIG") {
      // Security rule: Premium entitlement & SSV Reward verification cannot be bypassed remotely
      if (key === "BYPASS_PREMIUM_CHECK" || key === "BYPASS_REWARD_SSV") {
        return new Response(
          JSON.stringify({ error: "Forbidden: Security authorization checks cannot be remotely bypassed." }),
          { status: 403, headers: { ...corsHeaders, "Content-Type": "application/json" } }
        );
      }

      const { data: config, error } = await adminClient
        .from("app_configurations")
        .upsert({
          key,
          value,
          description,
          updated_at: new Date().toISOString(),
        })
        .select()
        .single();

      if (error) throw error;

      await adminClient.from("admin_audit_logs").insert({
        admin_email: adminEmail || "admin",
        action: "UPDATE_APP_CONFIG",
        target_type: "CONFIG",
        target_id: key,
        details: { value },
        status: "SUCCESS",
      });

      return new Response(JSON.stringify({ success: true, config }), {
        status: 200,
        headers: { ...corsHeaders, "Content-Type": "application/json" },
      });
    }

    if (action === "CREATE_ANNOUNCEMENT") {
      const { data: created, error } = await adminClient
        .from("app_announcements")
        .insert(announcement)
        .select()
        .single();

      if (error) throw error;

      return new Response(JSON.stringify({ success: true, announcement: created }), {
        status: 201,
        headers: { ...corsHeaders, "Content-Type": "application/json" },
      });
    }

    return new Response(JSON.stringify({ error: "Invalid action" }), {
      status: 400,
      headers: { ...corsHeaders, "Content-Type": "application/json" },
    });
  } catch (err: any) {
    return new Response(JSON.stringify({ error: err.message }), {
      status: 500,
      headers: { ...corsHeaders, "Content-Type": "application/json" },
    });
  }
});
