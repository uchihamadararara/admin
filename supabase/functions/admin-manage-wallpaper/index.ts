// ====================================================================
// Supabase Edge Function: admin-manage-wallpaper
// Validates wallpaper configuration, saves advanced_config, audits changes
// ====================================================================

import { serve } from "https://deno.land/std@0.168.0/http/server.ts";
import { createClient } from "https://esm.sh/@supabase/supabase-js@2.38.4";

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
  "Access-Control-Allow-Methods": "POST, OPTIONS",
};

serve(async (req) => {
  if (req.method === "OPTIONS") {
    return new Response("ok", { headers: corsHeaders });
  }

  try {
    const authHeader = req.headers.get("Authorization");
    if (!authHeader) {
      return new Response(JSON.stringify({ error: "Missing Authorization header" }), {
        status: 401,
        headers: { ...corsHeaders, "Content-Type": "application/json" },
      });
    }

    const supabaseUrl = Deno.env.get("SUPABASE_URL") ?? "";
    const supabaseServiceKey = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY") ?? "";
    const supabaseAnonKey = Deno.env.get("SUPABASE_ANON_KEY") ?? "";

    // User client to verify identity
    const userClient = createClient(supabaseUrl, supabaseAnonKey, {
      global: { headers: { Authorization: authHeader } },
    });

    const { data: { user }, error: userError } = await userClient.auth.getUser();
    if (userError || !user) {
      return new Response(JSON.stringify({ error: "Unauthorized user" }), {
        status: 401,
        headers: { ...corsHeaders, "Content-Type": "application/json" },
      });
    }

    // Admin role check
    const { data: adminUser, error: roleError } = await userClient
      .from("admin_users")
      .select("role, is_active, email")
      .eq("user_id", user.id)
      .single();

    if (roleError || !adminUser || !adminUser.is_active) {
      return new Response(JSON.stringify({ error: "Forbidden: Not an active admin" }), {
        status: 403,
        headers: { ...corsHeaders, "Content-Type": "application/json" },
      });
    }

    const body = await req.json();
    const { action, wallpaper } = body; // action: 'CREATE', 'UPDATE', 'STATUS_CHANGE', 'ARCHIVE'

    if (!wallpaper) {
      return new Response(JSON.stringify({ error: "wallpaper data is required" }), {
        status: 400,
        headers: { ...corsHeaders, "Content-Type": "application/json" },
      });
    }

    // Role check for specific action
    if (action === "STATUS_CHANGE" && adminUser.role === "MODERATOR") {
      // Moderator can only toggle status/deactivate
    } else if (!["SUPER_ADMIN", "ADMIN", "CONTENT_MANAGER"].includes(adminUser.role)) {
      return new Response(JSON.stringify({ error: "Insufficient permissions" }), {
        status: 403,
        headers: { ...corsHeaders, "Content-Type": "application/json" },
      });
    }

    // Validation logic for Content-Driven Live Wallpaper
    const validationErrors: string[] = [];
    const validationWarnings: string[] = [];

    if (!wallpaper.title || wallpaper.title.trim().length === 0) {
      validationErrors.push("Title is required");
    }

    if (wallpaper.content_type === "LIVE") {
      if (wallpaper.live_experience_type === "NORMAL") {
        if (!wallpaper.advanced_config?.primary?.url && !wallpaper.thumbnail_url) {
          validationErrors.push("NORMAL Live Wallpaper requires a primary video asset.");
        }
      } else if (wallpaper.live_experience_type === "TRANSITION") {
        if (!wallpaper.advanced_config?.home?.url) {
          validationErrors.push("TRANSITION Live Wallpaper requires at least a Home video asset.");
        }
        // Check transition targets
        if (wallpaper.advanced_config?.lock_to_home?.url && !wallpaper.advanced_config?.home?.url) {
          validationErrors.push("Lock->Home transition configured but Home video is missing.");
        }
        if (wallpaper.advanced_config?.home_to_lock?.url && !wallpaper.advanced_config?.lock?.url) {
          validationWarnings.push("Home->Lock transition configured without distinct Lock video; will fallback cleanly.");
        }
      }
    }

    // If publishing, hard validation must pass
    if (wallpaper.status === "PUBLISHED" && validationErrors.length > 0) {
      return new Response(
        JSON.stringify({
          error: "Cannot publish invalid wallpaper",
          details: validationErrors,
        }),
        {
          status: 422,
          headers: { ...corsHeaders, "Content-Type": "application/json" },
        }
      );
    }

    // Service role client for privileged mutations and audit logging
    const adminServiceClient = createClient(supabaseUrl, supabaseServiceKey);

    let savedWallpaper = null;
    let oldState = null;

    if (wallpaper.id) {
      const { data: existing } = await adminServiceClient
        .from("wallpapers")
        .select("*")
        .eq("id", wallpaper.id)
        .single();
      oldState = existing;
    }

    const payloadToSave = {
      title: wallpaper.title,
      description: wallpaper.description || "",
      content_type: wallpaper.content_type || "LIVE",
      live_experience_type: wallpaper.live_experience_type || "NORMAL",
      category_id: wallpaper.category_id || null,
      tags: wallpaper.tags || [],
      is_premium: Boolean(wallpaper.is_premium),
      is_featured: Boolean(wallpaper.is_featured),
      is_trending: Boolean(wallpaper.is_trending),
      is_new: Boolean(wallpaper.is_new),
      sort_order: Number(wallpaper.sort_order) || 0,
      status: wallpaper.status || "DRAFT",
      thumbnail_url: wallpaper.thumbnail_url || null,
      advanced_config: wallpaper.advanced_config || {},
      updated_at: new Date().toISOString(),
    };

    if (wallpaper.id) {
      const { data, error: updateError } = await adminServiceClient
        .from("wallpapers")
        .update(payloadToSave)
        .eq("id", wallpaper.id)
        .select()
        .single();

      if (updateError) throw updateError;
      savedWallpaper = data;
    } else {
      const { data, error: insertError } = await adminServiceClient
        .from("wallpapers")
        .insert({
          ...payloadToSave,
          created_by: user.id,
          created_at: new Date().toISOString(),
        })
        .select()
        .single();

      if (insertError) throw insertError;
      savedWallpaper = data;
    }

    // Immutable Audit Log
    await adminServiceClient.from("admin_audit_logs").insert({
      admin_user_id: user.id,
      admin_email: adminUser.email,
      admin_role: adminUser.role,
      action: action || (wallpaper.id ? "UPDATE_WALLPAPER" : "CREATE_WALLPAPER"),
      entity_type: "WALLPAPER",
      entity_id: savedWallpaper.id,
      old_state: oldState,
      new_state: savedWallpaper,
      metadata: {
        validation_warnings: validationWarnings,
        client_ip: req.headers.get("x-forwarded-for") || "unknown",
      },
      created_at: new Date().toISOString(),
    });

    return new Response(
      JSON.stringify({
        success: true,
        wallpaper: savedWallpaper,
        warnings: validationWarnings,
      }),
      {
        headers: { ...corsHeaders, "Content-Type": "application/json" },
        status: 200,
      }
    );
  } catch (err) {
    return new Response(JSON.stringify({ error: (err as Error).message }), {
      headers: { ...corsHeaders, "Content-Type": "application/json" },
      status: 500,
    });
  }
});
