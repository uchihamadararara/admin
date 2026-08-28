// ====================================================================
// Supabase Edge Function: admin-media-presign
// Generates secure temporary presigned R2 PUT URLs for direct uploads
// ====================================================================

import { serve } from "https://deno.land/std@0.168.0/http/server.ts";
import { createClient } from "https://esm.sh/@supabase/supabase-js@2.38.4";
import { S3Client, PutObjectCommand } from "https://esm.sh/@aws-sdk/client-s3@3.454.0";
import { getSignedUrl } from "https://esm.sh/@aws-sdk/s3-request-presigner@3.454.0";

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
  "Access-Control-Allow-Methods": "POST, OPTIONS",
};

const R2_ACCOUNT_ID = Deno.env.get("R2_ACCOUNT_ID") || "";
const R2_ACCESS_KEY_ID = Deno.env.get("R2_ACCESS_KEY_ID") || "";
const R2_SECRET_ACCESS_KEY = Deno.env.get("R2_SECRET_ACCESS_KEY") || "";
const R2_BUCKET_NAME = Deno.env.get("R2_BUCKET_NAME") || "live-wallpapers";
const R2_PUBLIC_BASE_URL = Deno.env.get("R2_PUBLIC_BASE_URL") || "https://pub-f6c5306834354d76808cad8fbb4e44cd.r2.dev";

const s3Client = new S3Client({
  region: "auto",
  endpoint: `https://${R2_ACCOUNT_ID}.r2.cloudflarestorage.com`,
  credentials: {
    accessKeyId: R2_ACCESS_KEY_ID,
    secretAccessKey: R2_SECRET_ACCESS_KEY,
  },
});

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
    const supabaseAnonKey = Deno.env.get("SUPABASE_ANON_KEY") ?? "";
    const supabase = createClient(supabaseUrl, supabaseAnonKey, {
      global: { headers: { Authorization: authHeader } },
    });

    const { data: { user }, error: userError } = await supabase.auth.getUser();
    if (userError || !user) {
      return new Response(JSON.stringify({ error: "Unauthorized user" }), {
        status: 401,
        headers: { ...corsHeaders, "Content-Type": "application/json" },
      });
    }

    // RBAC validation
    const { data: adminUser, error: roleError } = await supabase
      .from("admin_users")
      .select("role, is_active")
      .eq("user_id", user.id)
      .single();

    if (roleError || !adminUser || !adminUser.is_active) {
      return new Response(JSON.stringify({ error: "Forbidden: Not an active admin" }), {
        status: 403,
        headers: { ...corsHeaders, "Content-Type": "application/json" },
      });
    }

    const allowedRoles = ["SUPER_ADMIN", "ADMIN", "CONTENT_MANAGER"];
    if (!allowedRoles.includes(adminUser.role)) {
      return new Response(JSON.stringify({ error: "Forbidden: Insufficient permissions for media upload" }), {
        status: 403,
        headers: { ...corsHeaders, "Content-Type": "application/json" },
      });
    }

    const body = await req.json();
    const { filename, contentType, slot, wallpaperId } = body;

    if (!filename || !contentType || !slot) {
      return new Response(JSON.stringify({ error: "filename, contentType, and slot are required" }), {
        status: 400,
        headers: { ...corsHeaders, "Content-Type": "application/json" },
      });
    }

    const cleanSlot = slot.toLowerCase().replace(/[^a-z0-9_]/g, "");
    const ext = filename.split(".").pop() || "mp4";
    const targetWallpaperId = wallpaperId || "staging";
    const storageKey = `wallpapers/${targetWallpaperId}/${cleanSlot}_${Date.now()}.${ext}`;

    const command = new PutObjectCommand({
      Bucket: R2_BUCKET_NAME,
      Key: storageKey,
      ContentType: contentType,
    });

    const uploadUrl = await getSignedUrl(s3Client, command, { expiresIn: 3600 });
    const publicUrl = `${R2_PUBLIC_BASE_URL}/${storageKey}`;

    return new Response(
      JSON.stringify({
        uploadUrl,
        publicUrl,
        storageKey,
        slot: cleanSlot,
        expiresIn: 3600,
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
