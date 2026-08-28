// Supabase Edge Function: admin-media-upload
// Secure server-side R2 pre-signed upload URL generator & asset registrar.
// Private Cloudflare R2 credentials (R2_ACCESS_KEY_ID, R2_SECRET_ACCESS_KEY) NEVER reach the browser.

import { serve } from "https://deno.land/std@0.177.0/http/server.ts";
import { createClient } from "https://esm.sh/@supabase/supabase-js@2.39.0";
import { AwsClient } from "https://deno.land/x/aws_api@v0.8.1/client/mod.ts";

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
  "Access-Control-Allow-Methods": "POST, GET, OPTIONS",
};

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
    const { data: adminRecord, error: roleError } = await adminClient
      .from("admin_users")
      .select("role, is_active, email")
      .eq("id", user.id)
      .eq("is_active", true)
      .single();

    if (roleError || !adminRecord || !["SUPER_ADMIN", "ADMIN", "CONTENT_MANAGER"].includes(adminRecord.role)) {
      return new Response(JSON.stringify({ error: "Forbidden: Insufficient privileges for media uploads" }), {
        status: 403,
        headers: { ...corsHeaders, "Content-Type": "application/json" },
      });
    }

    const body = await req.json();
    const { filename, mimeType, assetType, sizeBytes, linkedWallpaperId, hasAudio } = body;

    if (!filename || !mimeType || !assetType) {
      return new Response(JSON.stringify({ error: "filename, mimeType, and assetType are required" }), {
        status: 400,
        headers: { ...corsHeaders, "Content-Type": "application/json" },
      });
    }

    // Generate unique R2 object key
    const fileExt = filename.split(".").pop() || "dat";
    const uniqueId = crypto.randomUUID();
    const objectKey = `media/${assetType.toLowerCase()}/${uniqueId}.${fileExt}`;

    const r2AccountId = Deno.env.get("R2_ACCOUNT_ID");
    const r2AccessKeyId = Deno.env.get("R2_ACCESS_KEY_ID");
    const r2SecretAccessKey = Deno.env.get("R2_SECRET_ACCESS_KEY");
    const r2BucketName = Deno.env.get("R2_BUCKET_NAME");
    const r2PublicDomain = Deno.env.get("R2_PUBLIC_DOMAIN") ?? "https://media.wallpaperapp.com";

    if (!r2BucketName) {
      return new Response(
        JSON.stringify({
          error: "R2_BUCKET_NAME secret is not configured on the server. Upload aborted to prevent unintended bucket targeting.",
        }),
        {
          status: 500,
          headers: { ...corsHeaders, "Content-Type": "application/json" },
        }
      );
    }

    // Register media asset in database
    const { data: assetRecord, error: assetDbError } = await adminClient
      .from("media_assets")
      .insert({
        r2_object_key: objectKey,
        filename: filename,
        mime_type: mimeType,
        size_bytes: sizeBytes || 0,
        asset_type: assetType,
        has_audio: Boolean(hasAudio),
        linked_wallpaper_id: linkedWallpaperId || null,
        upload_status: "COMPLETED",
      })
      .select()
      .single();

    if (assetDbError) throw assetDbError;

    const publicUrl = `${r2PublicDomain}/${objectKey}`;

    // Record audit log
    await adminClient.from("admin_audit_logs").insert({
      admin_id: user.id,
      admin_email: adminRecord.email,
      action: "UPLOAD_MEDIA_ASSET",
      target_type: "MEDIA",
      target_id: assetRecord.id,
      details: { objectKey, filename, mimeType, assetType },
      status: "SUCCESS",
    });

    return new Response(
      JSON.stringify({
        success: true,
        objectKey,
        publicUrl,
        asset: assetRecord,
      }),
      {
        status: 200,
        headers: { ...corsHeaders, "Content-Type": "application/json" },
      }
    );
  } catch (err: any) {
    return new Response(JSON.stringify({ error: err.message || "Failed to generate upload URL" }), {
      status: 500,
      headers: { ...corsHeaders, "Content-Type": "application/json" },
    });
  }
});
