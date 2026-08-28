// Supabase Edge Function: admin-manage-user
// Safe user management and support diagnostic lookup (without exposing sensitive tokens or Google credentials).

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
    const { action, userId, status, reason, adminEmail } = body;

    if (action === "GET_USER_DETAILS") {
      const { data: user, error: uErr } = await adminClient.from("users").select("*").eq("id", userId).single();
      if (uErr) throw uErr;

      const { data: purchases } = await adminClient
        .from("google_play_events")
        .select("id, product_id, base_plan_id, event_type, processing_status, event_time")
        .eq("user_id", userId)
        .order("event_time", { ascending: false });

      const { data: rewardLogs } = await adminClient
        .from("reward_ad_events")
        .select("id, placement_id, verification_status, created_at")
        .eq("user_id", userId)
        .order("created_at", { ascending: false })
        .limit(10);

      return new Response(
        JSON.stringify({ success: true, user, purchases: purchases || [], rewardLogs: rewardLogs || [] }),
        { status: 200, headers: { ...corsHeaders, "Content-Type": "application/json" } }
      );
    }

    if (action === "UPDATE_STATUS") {
      const { data: updated, error } = await adminClient
        .from("users")
        .update({ account_status: status })
        .eq("id", userId)
        .select()
        .single();

      if (error) throw error;

      await adminClient.from("admin_audit_logs").insert({
        admin_email: adminEmail || "system-admin",
        action: `USER_${status}`,
        target_type: "USER",
        target_id: userId,
        details: { reason },
        status: "SUCCESS",
      });

      return new Response(JSON.stringify({ success: true, user: updated }), {
        status: 200,
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
