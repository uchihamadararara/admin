-- ============================================================================
-- SAFE BACKWARD-COMPATIBLE ADMIN & RBAC SCHEMA MIGRATION (CORRECTED)
-- Preserves existing: users, wallpapers, google_play_events, advanced_config JSONB
-- ============================================================================

-- 1. SAFE ENUM TYPES
DO $$ BEGIN CREATE TYPE public.admin_role AS ENUM ('SUPER_ADMIN', 'ADMIN', 'CONTENT_MANAGER', 'MODERATOR', 'SUPPORT'); EXCEPTION WHEN duplicate_object THEN null; END $$;
DO $$ BEGIN CREATE TYPE public.wallpaper_status_type AS ENUM ('DRAFT', 'READY_FOR_REVIEW', 'PUBLISHED', 'INACTIVE', 'ARCHIVED'); EXCEPTION WHEN duplicate_object THEN null; END $$;
DO $$ BEGIN CREATE TYPE public.content_type_enum AS ENUM ('STATIC', 'LIVE'); EXCEPTION WHEN duplicate_object THEN null; END $$;
DO $$ BEGIN CREATE TYPE public.live_experience_enum AS ENUM ('NORMAL', 'TRANSITION'); EXCEPTION WHEN duplicate_object THEN null; END $$;
DO $$ BEGIN CREATE TYPE public.tier_type AS ENUM ('FREE', 'PREMIUM', 'VIP'); EXCEPTION WHEN duplicate_object THEN null; END $$;

-- 2. ADMIN USERS & RBAC TABLE
CREATE TABLE IF NOT EXISTS public.admin_users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID UNIQUE REFERENCES auth.users(id) ON DELETE CASCADE,
    email TEXT NOT NULL,
    display_name TEXT,
    role public.admin_role NOT NULL DEFAULT 'CONTENT_MANAGER',
    is_active BOOLEAN NOT NULL DEFAULT true,
    last_login_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- 3. TAXONOMY TABLES
CREATE TABLE IF NOT EXISTS public.categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title TEXT NOT NULL UNIQUE,
    slug TEXT NOT NULL UNIQUE,
    description TEXT,
    icon_url TEXT,
    sort_order INT NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS public.tags (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name TEXT NOT NULL UNIQUE,
    slug TEXT NOT NULL UNIQUE,
    usage_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- 4. CLOUDFLARE R2 MEDIA ASSETS REGISTRY
CREATE TABLE IF NOT EXISTS public.media_assets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    storage_key TEXT NOT NULL UNIQUE,
    public_url TEXT NOT NULL,
    mime_type TEXT NOT NULL,
    file_size_bytes BIGINT NOT NULL,
    width INT,
    height INT,
    duration_seconds DOUBLE PRECISION,
    fps INT,
    has_audio BOOLEAN NOT NULL DEFAULT false,
    audio_codec TEXT,
    sha256_hash TEXT NOT NULL,
    slot_type TEXT,
    wallpaper_id UUID,
    is_linked BOOLEAN NOT NULL DEFAULT false,
    created_by UUID REFERENCES auth.users(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- 5. SAFE EXTENSION OF EXISTING WALLPAPERS TABLE (ALL COLUMNS ADDED BEFORE RLS POLICIES)
-- Adds status column with default 'PUBLISHED' so existing production wallpapers remain visible
ALTER TABLE public.wallpapers ADD COLUMN IF NOT EXISTS status TEXT DEFAULT 'PUBLISHED';
ALTER TABLE public.wallpapers ADD COLUMN IF NOT EXISTS content_type TEXT DEFAULT 'LIVE';
ALTER TABLE public.wallpapers ADD COLUMN IF NOT EXISTS live_experience_type TEXT DEFAULT 'NORMAL';
ALTER TABLE public.wallpapers ADD COLUMN IF NOT EXISTS category_id UUID;
ALTER TABLE public.wallpapers ADD COLUMN IF NOT EXISTS tags TEXT[] DEFAULT '{}'::text[];
ALTER TABLE public.wallpapers ADD COLUMN IF NOT EXISTS is_premium BOOLEAN DEFAULT false;
ALTER TABLE public.wallpapers ADD COLUMN IF NOT EXISTS is_featured BOOLEAN DEFAULT false;
ALTER TABLE public.wallpapers ADD COLUMN IF NOT EXISTS is_trending BOOLEAN DEFAULT false;
ALTER TABLE public.wallpapers ADD COLUMN IF NOT EXISTS is_new BOOLEAN DEFAULT false;
ALTER TABLE public.wallpapers ADD COLUMN IF NOT EXISTS sort_order INT DEFAULT 0;
ALTER TABLE public.wallpapers ADD COLUMN IF NOT EXISTS thumbnail_url TEXT;
ALTER TABLE public.wallpapers ADD COLUMN IF NOT EXISTS preview_url TEXT;
ALTER TABLE public.wallpapers ADD COLUMN IF NOT EXISTS advanced_config JSONB DEFAULT '{}'::jsonb;
ALTER TABLE public.wallpapers ADD COLUMN IF NOT EXISTS download_count BIGINT DEFAULT 0;
ALTER TABLE public.wallpapers ADD COLUMN IF NOT EXISTS view_count BIGINT DEFAULT 0;
ALTER TABLE public.wallpapers ADD COLUMN IF NOT EXISTS favorite_count BIGINT DEFAULT 0;
ALTER TABLE public.wallpapers ADD COLUMN IF NOT EXISTS aspect_ratio TEXT DEFAULT '9:16';
ALTER TABLE public.wallpapers ADD COLUMN IF NOT EXISTS duration_seconds DOUBLE PRECISION;
ALTER TABLE public.wallpapers ADD COLUMN IF NOT EXISTS fps INT;
ALTER TABLE public.wallpapers ADD COLUMN IF NOT EXISTS has_audio BOOLEAN DEFAULT false;
ALTER TABLE public.wallpapers ADD COLUMN IF NOT EXISTS audio_codec TEXT;
ALTER TABLE public.wallpapers ADD COLUMN IF NOT EXISTS file_size_bytes BIGINT DEFAULT 0;
ALTER TABLE public.wallpapers ADD COLUMN IF NOT EXISTS sha256_hash TEXT;
ALTER TABLE public.wallpapers ADD COLUMN IF NOT EXISTS blur_hash TEXT;
ALTER TABLE public.wallpapers ADD COLUMN IF NOT EXISTS created_at TIMESTAMPTZ DEFAULT now();
ALTER TABLE public.wallpapers ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ DEFAULT now();

-- Ensure existing rows that might have null status are explicitly set to 'PUBLISHED'
UPDATE public.wallpapers SET status = 'PUBLISHED' WHERE status IS NULL;

-- 6. EXTENDED BILLING & SSV EVENTS
CREATE TABLE IF NOT EXISTS public.billing_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID,
    order_id TEXT,
    purchase_token_hash TEXT,
    product_id TEXT NOT NULL,
    event_type TEXT NOT NULL,
    verification_status TEXT NOT NULL DEFAULT 'PENDING',
    raw_payload JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS public.admob_ssv_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID,
    ad_unit_id TEXT NOT NULL,
    reward_amount INT NOT NULL DEFAULT 1,
    reward_type TEXT NOT NULL DEFAULT 'UNLOCK',
    signature_verified BOOLEAN NOT NULL DEFAULT false,
    raw_query JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- 7. MODERATION, CONFIG & ANNOUNCEMENTS
CREATE TABLE IF NOT EXISTS public.moderation_reports (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    reporter_id UUID,
    target_type TEXT NOT NULL,
    target_id UUID NOT NULL,
    reason TEXT NOT NULL,
    status TEXT NOT NULL DEFAULT 'OPEN',
    admin_notes TEXT,
    reviewed_by UUID REFERENCES auth.users(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS public.app_configuration (
    key TEXT PRIMARY KEY,
    value JSONB NOT NULL,
    description TEXT,
    updated_by UUID REFERENCES auth.users(id),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS public.announcements (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title TEXT NOT NULL,
    message TEXT NOT NULL,
    action_url TEXT,
    target_audience TEXT NOT NULL DEFAULT 'ALL',
    is_active BOOLEAN NOT NULL DEFAULT true,
    starts_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    expires_at TIMESTAMPTZ,
    created_by UUID REFERENCES auth.users(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- 8. IMMUTABLE AUDIT LOGS
CREATE TABLE IF NOT EXISTS public.admin_audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    admin_user_id UUID NOT NULL REFERENCES auth.users(id),
    action TEXT NOT NULL,
    entity_type TEXT NOT NULL,
    entity_id TEXT,
    details JSONB,
    ip_address TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- 9. RBAC HELPER FUNCTIONS (Security Definer)
CREATE OR REPLACE FUNCTION public.is_super_admin()
RETURNS BOOLEAN LANGUAGE sql SECURITY DEFINER STABLE AS $$
    SELECT EXISTS (
        SELECT 1 FROM public.admin_users
        WHERE user_id = auth.uid() AND role = 'SUPER_ADMIN' AND is_active = true
    );
$$;

CREATE OR REPLACE FUNCTION public.is_admin_or_super()
RETURNS BOOLEAN LANGUAGE sql SECURITY DEFINER STABLE AS $$
    SELECT EXISTS (
        SELECT 1 FROM public.admin_users
        WHERE user_id = auth.uid() AND role IN ('SUPER_ADMIN', 'ADMIN') AND is_active = true
    );
$$;

CREATE OR REPLACE FUNCTION public.has_admin_access()
RETURNS BOOLEAN LANGUAGE sql SECURITY DEFINER STABLE AS $$
    SELECT EXISTS (
        SELECT 1 FROM public.admin_users
        WHERE user_id = auth.uid() AND is_active = true
    );
$$;

-- 10. AUDIT LOG IMMUTABILITY TRIGGER
CREATE OR REPLACE FUNCTION public.prevent_audit_log_mutation()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
    RAISE EXCEPTION 'admin_audit_logs is append-only. Mutation is forbidden.';
END;
$$;

DROP TRIGGER IF EXISTS trg_prevent_audit_mutation ON public.admin_audit_logs;
CREATE TRIGGER trg_prevent_audit_mutation
BEFORE UPDATE OR DELETE ON public.admin_audit_logs
FOR EACH ROW EXECUTE FUNCTION public.prevent_audit_log_mutation();

-- 11. ENABLE RLS (Without Breaking Public Access)
ALTER TABLE public.admin_users ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.categories ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.tags ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.media_assets ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.wallpapers ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.billing_events ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.admob_ssv_events ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.moderation_reports ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.app_configuration ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.announcements ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.admin_audit_logs ENABLE ROW LEVEL SECURITY;

-- 12. RLS POLICIES
-- Admin Users
DROP POLICY IF EXISTS "Super admins manage admin users" ON public.admin_users;
CREATE POLICY "Super admins manage admin users" ON public.admin_users FOR ALL USING (public.is_super_admin());

DROP POLICY IF EXISTS "Admins view own profile" ON public.admin_users;
CREATE POLICY "Admins view own profile" ON public.admin_users FOR SELECT USING (auth.uid() = user_id);

-- Wallpapers: Public read includes all PUBLISHED wallpapers or any legacy row
DROP POLICY IF EXISTS "Public view published wallpapers" ON public.wallpapers;
CREATE POLICY "Public view published wallpapers" ON public.wallpapers FOR SELECT USING (status = 'PUBLISHED' OR status IS NULL);

DROP POLICY IF EXISTS "Admins manage wallpapers" ON public.wallpapers;
CREATE POLICY "Admins manage wallpapers" ON public.wallpapers FOR ALL USING (public.has_admin_access());

-- Categories & Tags
DROP POLICY IF EXISTS "Public view active categories" ON public.categories;
CREATE POLICY "Public view active categories" ON public.categories FOR SELECT USING (is_active = true);

DROP POLICY IF EXISTS "Admins manage categories" ON public.categories;
CREATE POLICY "Admins manage categories" ON public.categories FOR ALL USING (public.has_admin_access());

DROP POLICY IF EXISTS "Public view tags" ON public.tags;
CREATE POLICY "Public view tags" ON public.tags FOR SELECT USING (true);

DROP POLICY IF EXISTS "Admins manage tags" ON public.tags;
CREATE POLICY "Admins manage tags" ON public.tags FOR ALL USING (public.has_admin_access());

-- Media Assets
DROP POLICY IF EXISTS "Public view linked assets" ON public.media_assets;
CREATE POLICY "Public view linked assets" ON public.media_assets FOR SELECT USING (is_linked = true);

DROP POLICY IF EXISTS "Admins manage media assets" ON public.media_assets;
CREATE POLICY "Admins manage media assets" ON public.media_assets FOR ALL USING (public.has_admin_access());

-- App Config & Announcements
DROP POLICY IF EXISTS "Public read app configuration" ON public.app_configuration;
CREATE POLICY "Public read app configuration" ON public.app_configuration FOR SELECT USING (true);

DROP POLICY IF EXISTS "Super admins manage app config" ON public.app_configuration;
CREATE POLICY "Super admins manage app config" ON public.app_configuration FOR ALL USING (public.is_super_admin());

DROP POLICY IF EXISTS "Public read active announcements" ON public.announcements;
CREATE POLICY "Public read active announcements" ON public.announcements FOR SELECT USING (is_active = true AND starts_at <= now() AND (expires_at IS NULL OR expires_at > now()));

DROP POLICY IF EXISTS "Admins manage announcements" ON public.announcements;
CREATE POLICY "Admins manage announcements" ON public.announcements FOR ALL USING (public.has_admin_access());

-- Audit Logs
DROP POLICY IF EXISTS "Super admins view audit logs" ON public.admin_audit_logs;
CREATE POLICY "Super admins view audit logs" ON public.admin_audit_logs FOR SELECT USING (public.is_super_admin());

DROP POLICY IF EXISTS "Admins insert audit logs" ON public.admin_audit_logs;
CREATE POLICY "Admins insert audit logs" ON public.admin_audit_logs FOR INSERT WITH CHECK (public.has_admin_access());
