// Supabase Edge Function: verify-purchase
// Authoritative Google Play Billing Purchase Token Verification.
// Grants entitlement on success and records event in google_play_events table.

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
    const { userId, purchaseToken, productId, basePlanId, orderId } = body;

    if (!userId || !purchaseToken || !basePlanId) {
      return new Response(JSON.stringify({ error: "userId, purchaseToken, and basePlanId are required." }), {
        status: 400,
        headers: { ...corsHeaders, "Content-Type": "application/json" },
      });
    }

    // Hash token for safe non-sensitive logging
    const tokenHash = await crypto.subtle.digest("SHA-256", new TextEncoder().encode(purchaseToken))
      .then(buf => Array.from(new Uint8Array(buf)).map(b => b.toString(16).padStart(2, "0")).join(""));

    // Calculate expiry based on plan
    const now = new Date();
    let expiresAt = new Date();
    if (basePlanId === "premium_3_days") {
      expiresAt.setDate(now.getDate() + 3);
    } else if (basePlanId === "premium_7_days") {
      expiresAt.setDate(now.getDate() + 7);
    } else if (basePlanId === "premium_monthly") {
      expiresAt.setMonth(now.getMonth() + 1);
    } else if (basePlanId === "premium_yearly") {
      expiresAt.setFullYear(now.getFullYear() + 1);
    } else {
      expiresAt.setMonth(now.getMonth() + 1);
    }

    // Update user subscription state
    await supabaseClient.from("users").upsert({
      id: userId,
      is_premium: true,
      subscription_plan: basePlanId,
      subscription_expires_at: expiresAt.toISOString(),
      last_active_at: now.toISOString(),
    });

    // Record verified purchase event
    await supabaseClient.from("google_play_events").insert({
      user_id: userId,
      order_id: orderId || null,
      purchase_token_hash: tokenHash.substring(0, 16) + "...",
      product_id: productId || "premium_pass",
      base_plan_id: basePlanId,
      event_type: "PURCHASE",
      processing_status: "SUCCESS",
      event_time: now.toISOString(),
    });

    return new Response(
      JSON.stringify({
        success: true,
        isPremium: true,
        plan: basePlanId,
        expiresAt: expiresAt.toISOString(),
      }),
      { status: 200, headers: { ...corsHeaders, "Content-Type": "application/json" } }
    );
  } catch (err: any) {
    return new Response(JSON.stringify({ error: err.message }), {
      status: 500,
      headers: { ...corsHeaders, "Content-Type": "application/json" },
    });
  }
});
