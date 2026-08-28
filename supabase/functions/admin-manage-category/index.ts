// Supabase Edge Function: admin-manage-category
// Handles Category and Tag CRUD with safe dependency checks to prevent orphaned wallpaper references.

import { serve } from "https://deno.land/std@0.177.0/http/server.ts";
import { createClient } from "https://esm.sh/@supabase/supabase-js@2.39.0";

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
};

serve(async (req: Request) => {
  if (req.method === "OPTIONS") return new Response("ok", { headers: corsHeaders });

  try {
    const authHeader = req.headers.get("Authorization");
    const supabaseUrl = Deno.env.get("SUPABASE_URL") ?? "";
    const supabaseServiceKey = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY") ?? "";
    const adminClient = createClient(supabaseUrl, supabaseServiceKey);

    const body = await req.json();
    const { action, target, id, data } = body; // target: 'CATEGORY' | 'TAG'

    if (target === "CATEGORY") {
      if (action === "CREATE") {
        const { data: created, error } = await adminClient
          .from("categories")
          .insert({
            name: data.name,
            slug: data.slug || data.name.toLowerCase().replace(/\s+/g, "-"),
            description: data.description || "",
            icon_url: data.icon_url || null,
            thumbnail_url: data.thumbnail_url || null,
            is_active: data.is_active !== undefined ? data.is_active : true,
            sort_order: data.sort_order || 0,
          })
          .select()
          .single();

        if (error) throw error;
        return new Response(JSON.stringify({ success: true, category: created }), {
          status: 201,
          headers: { ...corsHeaders, "Content-Type": "application/json" },
        });
      }

      if (action === "UPDATE") {
        const { data: updated, error } = await adminClient
          .from("categories")
          .update(data)
          .eq("id", id)
          .select()
          .single();

        if (error) throw error;
        return new Response(JSON.stringify({ success: true, category: updated }), {
          status: 200,
          headers: { ...corsHeaders, "Content-Type": "application/json" },
        });
      }

      if (action === "DELETE") {
        // Check if wallpapers reference this category
        const { count } = await adminClient
          .from("wallpapers")
          .select("*", { count: "exact", head: true })
          .eq("category_id", id);

        if (count && count > 0) {
          // Deactivate instead of hard delete to preserve wallpaper integrity
          await adminClient.from("categories").update({ is_active: false }).eq("id", id);
          return new Response(
            JSON.stringify({
              success: true,
              message: `Category has ${count} linked wallpapers. Safely deactivated instead of deleted.`,
            }),
            { status: 200, headers: { ...corsHeaders, "Content-Type": "application/json" } }
          );
        }

        const { error } = await adminClient.from("categories").delete().eq("id", id);
        if (error) throw error;
        return new Response(JSON.stringify({ success: true, message: "Category deleted" }), {
          status: 200,
          headers: { ...corsHeaders, "Content-Type": "application/json" },
        });
      }
    }

    if (target === "TAG") {
      if (action === "CREATE") {
        const { data: created, error } = await adminClient.from("tags").insert(data).select().single();
        if (error) throw error;
        return new Response(JSON.stringify({ success: true, tag: created }), {
          status: 201,
          headers: { ...corsHeaders, "Content-Type": "application/json" },
        });
      }
      if (action === "DELETE") {
        await adminClient.from("tags").delete().eq("id", id);
        return new Response(JSON.stringify({ success: true, message: "Tag deleted" }), {
          status: 200,
          headers: { ...corsHeaders, "Content-Type": "application/json" },
        });
      }
    }

    return new Response(JSON.stringify({ error: "Invalid target or action" }), {
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
