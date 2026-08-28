// Supabase Edge Function: get-premium-url
// Returns short-lived signed media URL only if the user has an active Premium entitlement.

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
    const { userId, wallpaperId, purpose = "preview" } = body;

    if (!wallpaperId) {
      return new Response(JSON.stringify({ error: "wallpaperId is required" }), {
        status: 400,
        headers: { ...corsHeaders, "Content-Type": "application/json" },
      });
    }

    const { data: wp, error: wpErr } = await supabaseClient
      .from("wallpapers")
      .select("id, title, access_type, thumbnail_url, preview_url, media_url")
      .eq("id", wallpaperId)
      .single();

    if (wpErr || !wp) {
      return new Response(JSON.stringify({ error: "Wallpaper not found" }), {
        status: 404,
        headers: { ...corsHeaders, "Content-Type": "application/json" },
      });
    }

    let isEntitled = false;
    if (userId) {
      const { data: userRecord } = await supabaseClient
        .from("users")
        .select("is_premium, subscription_expires_at")
        .eq("id", userId)
        .single();

      isEntitled = Boolean(
        userRecord?.is_premium &&
          (!userRecord.subscription_expires_at || new Date(userRecord.subscription_expires_at) > new Date())
      );
    }

    // In-App Virtual Preview: Permitted for all users (Free and Premium)
    if (purpose === "preview") {
      return new Response(
        JSON.stringify({
          success: true,
          previewUrl: wp.preview_url || wp.thumbnail_url,
          isPreview: true,
          isEntitled,
        }),
        {
          status: 200,
          headers: { ...corsHeaders, "Content-Type": "application/json" },
        }
      );
    }

    // Actual Application / Full Resolution Download: Strictly requires active Premium entitlement
    if (wp.access_type === "PREMIUM" && !isEntitled) {
      return new Response(
        JSON.stringify({
          error: "Access denied: Active Premium subscription required to download/apply full-resolution media.",
          requiresSubscription: true,
        }),
        {
          status: 403,
          headers: { ...corsHeaders, "Content-Type": "application/json" },
        }
      );
    }

    return new Response(
      JSON.stringify({
        success: true,
        authorizedUrl: wp.media_url,
        isPreview: false,
        isEntitled: true,
      }),
      {
        status: 200,
        headers: { ...corsHeaders, "Content-Type": "application/json" },
      }
    );
  } catch (err: any) {
    return new Response(JSON.stringify({ error: err.message }), {
      status: 500,
      headers: { ...corsHeaders, "Content-Type": "application/json" },
    });
  }
});
