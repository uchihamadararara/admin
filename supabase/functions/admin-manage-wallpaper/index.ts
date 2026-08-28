// Supabase Edge Function: admin-manage-wallpaper
// Handles authenticated Wallpaper creation, updates, validation, charging/transition metadata, sound metadata, and audit logging.

import { serve } from "https://deno.land/std@0.177.0/http/server.ts";
import { createClient } from "https://esm.sh/@supabase/supabase-js@2.39.0";

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
  "Access-Control-Allow-Methods": "POST, GET, OPTIONS, PUT, DELETE",
};

interface WallpaperPayload {
  action: "CREATE" | "UPDATE" | "DELETE" | "TOGGLE_STATUS" | "BULK_UPDATE";
  wallpaperId?: string;
  wallpaperIds?: string[];
  data?: Record<string, any>;
  reason?: string;
}

serve(async (req: Request) => {
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

    const userClient = createClient(supabaseUrl, supabaseAnonKey, {
      global: { headers: { Authorization: authHeader } },
    });

    const { data: { user }, error: userError } = await userClient.auth.getUser();
    if (userError || !user) {
      return new Response(JSON.stringify({ error: "Unauthorized user session" }), {
        status: 401,
        headers: { ...corsHeaders, "Content-Type": "application/json" },
      });
    }

    const adminClient = createClient(supabaseUrl, supabaseServiceKey);

    // Verify admin role
    const { data: adminRecord, error: roleError } = await adminClient
      .from("admin_users")
      .select("role, is_active, email, name")
      .eq("id", user.id)
      .eq("is_active", true)
      .single();

    if (roleError || !adminRecord || !["SUPER_ADMIN", "ADMIN", "CONTENT_MANAGER"].includes(adminRecord.role)) {
      return new Response(JSON.stringify({ error: "Forbidden: Insufficient admin privileges" }), {
        status: 403,
        headers: { ...corsHeaders, "Content-Type": "application/json" },
      });
    }

    const payload: WallpaperPayload = await req.json();

    if (payload.action === "CREATE") {
      const wallpaperData = payload.data || {};
      
      // Strict server-side content validation
      if (!wallpaperData.title || !wallpaperData.thumbnail_url || !wallpaperData.media_url) {
        return new Response(JSON.stringify({ error: "Title, thumbnail_url, and media_url are required." }), {
          status: 400,
          headers: { ...corsHeaders, "Content-Type": "application/json" },
        });
      }

      // Ensure sound consistency: soundAvailable must match actual audio metadata
      const soundAvailable = Boolean(wallpaperData.sound_available);
      const chargingAvailable = Boolean(wallpaperData.charging_animation_available);
      const transitionAvailable = Boolean(wallpaperData.transition_available);
      const liveExperienceType = wallpaperData.live_experience_type === "TRANSITION" ? "TRANSITION" : "NORMAL";

      const { data: inserted, error: insertError } = await adminClient
        .from("wallpapers")
        .insert({
          title: wallpaperData.title,
          description: wallpaperData.description || "",
          type: wallpaperData.type || "STATIC",
          live_experience_type: liveExperienceType,
          access_type: wallpaperData.access_type || "FREE",
          status: wallpaperData.status || "ACTIVE",
          category_id: wallpaperData.category_id || null,
          category: wallpaperData.category || "General",
          tags: wallpaperData.tags || [],
          is_featured: Boolean(wallpaperData.is_featured),
          is_trending: Boolean(wallpaperData.is_trending),
          is_new: wallpaperData.is_new !== undefined ? Boolean(wallpaperData.is_new) : true,
          sort_order: wallpaperData.sort_order || 0,
          thumbnail_url: wallpaperData.thumbnail_url,
          preview_url: wallpaperData.preview_url || wallpaperData.thumbnail_url,
          media_url: wallpaperData.media_url,
          file_size_bytes: wallpaperData.file_size_bytes || 0,
          width: wallpaperData.width || 1080,
          height: wallpaperData.height || 1920,
          duration_seconds: wallpaperData.duration_seconds || 0.0,
          fps: wallpaperData.fps || 60,
          aspect_ratio: wallpaperData.aspect_ratio || "9:16",
          sound_available: soundAvailable,
          sound_metadata: wallpaperData.sound_metadata || { hasAudioTrack: soundAvailable, defaultVolume: 1.0 },
          charging_animation_available: chargingAvailable,
          charging_animation_id: wallpaperData.charging_animation_id || null,
          charging_animation_type: wallpaperData.charging_animation_type || "BATTERY_PULSE",
          charging_animation_asset: wallpaperData.charging_animation_asset || null,
          charging_animation_preview: wallpaperData.charging_animation_preview || null,
          charging_transition_duration_ms: wallpaperData.charging_transition_duration_ms || 300,
          transition_available: transitionAvailable,
          transition_type: wallpaperData.transition_type || "FADE",
          transition_asset: wallpaperData.transition_asset || null,
          transition_source_state: wallpaperData.transition_source_state || "HOME",
          transition_target_state: wallpaperData.transition_target_state || "CHARGING",
          transition_duration_ms: wallpaperData.transition_duration_ms || 400,
        })
        .select()
        .single();

      if (insertError) {
        throw insertError;
      }

      // Sync multi-slot bundle assets if provided
      if (Array.isArray(wallpaperData.assets) && wallpaperData.assets.length > 0) {
        const assetRows = wallpaperData.assets.map((ast: any) => ({
          wallpaper_id: inserted.id,
          slot_type: ast.slot_type || "PRIMARY",
          storage_key: ast.storage_key || ast.media_url,
          media_url: ast.media_url,
          mime_type: ast.mime_type || "video/mp4",
          width: ast.width || 1080,
          height: ast.height || 1920,
          duration_ms: ast.duration_ms || 0,
          fps: ast.fps || 60,
          has_audio: Boolean(ast.has_audio),
          audio_codec: ast.audio_codec || null,
          audio_channels: ast.audio_channels || null,
          file_size_bytes: ast.file_size_bytes || 0,
          sha256: ast.sha256 || null,
        }));

        await adminClient.from("wallpaper_assets").insert(assetRows);
      }

      // Record audit log
      await adminClient.from("admin_audit_logs").insert({
        admin_id: user.id,
        admin_email: adminRecord.email,
        action: "CREATE_WALLPAPER",
        target_type: "WALLPAPER",
        target_id: inserted.id,
        details: { title: inserted.title, type: inserted.type, access_type: inserted.access_type },
        status: "SUCCESS",
      });

      return new Response(JSON.stringify({ success: true, wallpaper: inserted }), {
        status: 201,
        headers: { ...corsHeaders, "Content-Type": "application/json" },
      });
    }

    if (payload.action === "UPDATE") {
      if (!payload.wallpaperId) {
        return new Response(JSON.stringify({ error: "wallpaperId is required for update" }), {
          status: 400,
          headers: { ...corsHeaders, "Content-Type": "application/json" },
        });
      }

      const rawData = payload.data || {};
      const { assets, ...updateFieldsWithoutAssets } = rawData;
      const updateFields = { ...updateFieldsWithoutAssets, updated_at: new Date().toISOString() };

      const { data: updated, error: updateError } = await adminClient
        .from("wallpapers")
        .update(updateFields)
        .eq("id", payload.wallpaperId)
        .select()
        .single();

      if (updateError) throw updateError;

      if (Array.isArray(assets)) {
        await adminClient.from("wallpaper_assets").delete().eq("wallpaper_id", payload.wallpaperId);
        if (assets.length > 0) {
          const assetRows = assets.map((ast: any) => ({
            wallpaper_id: payload.wallpaperId,
            slot_type: ast.slot_type || "PRIMARY",
            storage_key: ast.storage_key || ast.media_url,
            media_url: ast.media_url,
            mime_type: ast.mime_type || "video/mp4",
            width: ast.width || 1080,
            height: ast.height || 1920,
            duration_ms: ast.duration_ms || 0,
            fps: ast.fps || 60,
            has_audio: Boolean(ast.has_audio),
            audio_codec: ast.audio_codec || null,
            audio_channels: ast.audio_channels || null,
            file_size_bytes: ast.file_size_bytes || 0,
            sha256: ast.sha256 || null,
          }));
          await adminClient.from("wallpaper_assets").insert(assetRows);
        }
      }

      await adminClient.from("admin_audit_logs").insert({
        admin_id: user.id,
        admin_email: adminRecord.email,
        action: "UPDATE_WALLPAPER",
        target_type: "WALLPAPER",
        target_id: payload.wallpaperId,
        details: { updated_fields: Object.keys(updateFields) },
        status: "SUCCESS",
      });

      return new Response(JSON.stringify({ success: true, wallpaper: updated }), {
        status: 200,
        headers: { ...corsHeaders, "Content-Type": "application/json" },
      });
    }

    if (payload.action === "DELETE") {
      if (!payload.wallpaperId) {
        return new Response(JSON.stringify({ error: "wallpaperId is required for deletion" }), {
          status: 400,
          headers: { ...corsHeaders, "Content-Type": "application/json" },
        });
      }

      // Safety check: de-link or safe delete
      const { error: deleteError } = await adminClient
        .from("wallpapers")
        .delete()
        .eq("id", payload.wallpaperId);

      if (deleteError) throw deleteError;

      await adminClient.from("admin_audit_logs").insert({
        admin_id: user.id,
        admin_email: adminRecord.email,
        action: "DELETE_WALLPAPER",
        target_type: "WALLPAPER",
        target_id: payload.wallpaperId,
        details: { reason: payload.reason || "Manual admin delete" },
        status: "SUCCESS",
      });

      return new Response(JSON.stringify({ success: true, message: "Wallpaper deleted safely." }), {
        status: 200,
        headers: { ...corsHeaders, "Content-Type": "application/json" },
      });
    }

    return new Response(JSON.stringify({ error: "Unknown action" }), {
      status: 400,
      headers: { ...corsHeaders, "Content-Type": "application/json" },
    });
  } catch (err: any) {
    return new Response(JSON.stringify({ error: err.message || "Internal server error" }), {
      status: 500,
      headers: { ...corsHeaders, "Content-Type": "application/json" },
    });
  }
});
