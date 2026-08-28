// Supabase Edge Function: play-rtdn
// Google Cloud Pub/Sub Webhook for Google Play Real-Time Developer Notifications (RTDN).
// Processes subscription renewals, cancellations, grace periods, and expirations without exposing credentials.

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
    const supabaseClient = createClient(supabaseUrl, supabaseServiceKey);

    const body = await req.json();
    const pubsubMessage = body.message;

    if (!pubsubMessage || !pubsubMessage.data) {
      return new Response(JSON.stringify({ error: "Invalid Pub/Sub message payload" }), {
        status: 400,
        headers: { ...corsHeaders, "Content-Type": "application/json" },
      });
    }

    const decodedData = atob(pubsubMessage.data);
    const rtdnPayload = JSON.parse(decodedData);
    const subNotification = rtdnPayload.subscriptionNotification;

    if (subNotification) {
      const notificationType = subNotification.notificationType;
      const purchaseToken = subNotification.purchaseToken;
      const subscriptionId = subNotification.subscriptionId;

      let eventType = "RTDN_NOTIFICATION";
      if (notificationType === 2) eventType = "RENEWAL";
      if (notificationType === 3) eventType = "CANCELLATION";
      if (notificationType === 13) eventType = "EXPIRATION";
      if (notificationType === 6) eventType = "GRACE_PERIOD";

      await supabaseClient.from("google_play_events").insert({
        product_id: subscriptionId || "premium_pass",
        base_plan_id: "rtdn_auto_event",
        event_type: eventType,
        processing_status: "SUCCESS",
        event_time: new Date().toISOString(),
      });
    }

    return new Response(JSON.stringify({ success: true, processed: true }), {
      status: 200,
      headers: { ...corsHeaders, "Content-Type": "application/json" },
    });
  } catch (err: any) {
    return new Response(JSON.stringify({ error: err.message }), {
      status: 500,
      headers: { ...corsHeaders, "Content-Type": "application/json" },
    });
  }
});
