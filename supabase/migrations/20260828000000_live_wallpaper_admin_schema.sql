-- ====================================================================
-- LIVE WALLPAPER PLATFORM - COMPLETE PRODUCTION SCHEMA & RLS MIGRATION
-- ====================================================================

-- 1. ENUMS & EXTENSIONS
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

DO $$ BEGIN
    CREATE TYPE admin_role_type AS ENUM ('SUPER_ADMIN', 'ADMIN', 'CONTENT_MANAGER', 'MODERATOR', 'SUPPORT');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE wallpaper_content_type AS ENUM ('STATIC', 'LIVE');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE live_experience_type_enum AS ENUM ('NORMAL', 'TRANSITION');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE wallpaper_status_enum AS ENUM ('DRAFT', 'READY_FOR_REVIEW', 'PUBLISHED', 'INACTIVE', 'ARCHIVED');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE subscription_tier_enum AS ENUM ('vip_3days', 'vip_7days', 'vip_14days', 'vip_1month', 'vip_lifetime');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE subscription_status_enum AS ENUM ('ACTIVE', 'EXPIRED', 'CANCELED', 'ON_HOLD', 'GRACE_PERIOD', 'INACTIVE');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE report_status_enum AS ENUM ('OPEN', 'IN_REVIEW', 'RESOLVED', 'DISMISSED');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

-- 2. ADMIN USERS & RBAC TABLE
CREATE TABLE IF NOT EXISTS public.admin_users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL UNIQUE REFERENCES auth.users(id) ON DELETE CASCADE,
    email TEXT NOT NULL,
    role admin_role_type NOT NULL DEFAULT 'CONTENT_MANAGER',
    is_active BOOLEAN NOT NULL DEFAULT true,
    invited_by UUID REFERENCES auth.users(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Helper function to check admin role
CREATE OR REPLACE FUNCTION public.get_current_admin_role()
RETURNS admin_role_type AS $$
    SELECT role FROM public.admin_users
    WHERE user_id = auth.uid() AND is_active = true;
$$ LANGUAGE sql STABLE SECURITY DEFINER;

CREATE OR REPLACE FUNCTION public.is_super_admin()
RETURNS BOOLEAN AS $$
    SELECT EXISTS (
        SELECT 1 FROM public.admin_users
        WHERE user_id = auth.uid() AND role = 'SUPER_ADMIN' AND is_active = true
    );
$$ LANGUAGE sql STABLE SECURITY DEFINER;

CREATE OR REPLACE FUNCTION public.is_admin_or_super()
RETURNS BOOLEAN AS $$
    SELECT EXISTS (
        SELECT 1 FROM public.admin_users
        WHERE user_id = auth.uid() AND role IN ('SUPER_ADMIN', 'ADMIN') AND is_active = true
    );
$$ LANGUAGE sql STABLE SECURITY DEFINER;

CREATE OR REPLACE FUNCTION public.has_admin_access()
RETURNS BOOLEAN AS $$
    SELECT EXISTS (
        SELECT 1 FROM public.admin_users
        WHERE user_id = auth.uid() AND is_active = true
    );
$$ LANGUAGE sql STABLE SECURITY DEFINER;

-- 3. CATEGORIES TABLE
CREATE TABLE IF NOT EXISTS public.categories (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name TEXT NOT NULL,
    slug TEXT NOT NULL UNIQUE,
    icon_url TEXT,
    cover_url TEXT,
    sort_order INTEGER NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 4. TAGS TABLE
CREATE TABLE IF NOT EXISTS public.tags (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name TEXT NOT NULL UNIQUE,
    slug TEXT NOT NULL UNIQUE,
    usage_count INTEGER NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 5. WALLPAPERS TABLE
CREATE TABLE IF NOT EXISTS public.wallpapers (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    title TEXT NOT NULL,
    description TEXT,
    content_type wallpaper_content_type NOT NULL DEFAULT 'LIVE',
    live_experience_type live_experience_type_enum NOT NULL DEFAULT 'NORMAL',
    category_id UUID REFERENCES public.categories(id) ON DELETE SET NULL,
    tags TEXT[] NOT NULL DEFAULT '{}',
    is_premium BOOLEAN NOT NULL DEFAULT false,
    is_featured BOOLEAN NOT NULL DEFAULT false,
    is_trending BOOLEAN NOT NULL DEFAULT false,
    is_new BOOLEAN NOT NULL DEFAULT true,
    sort_order INTEGER NOT NULL DEFAULT 0,
    status wallpaper_status_enum NOT NULL DEFAULT 'DRAFT',
    thumbnail_url TEXT,
    advanced_config JSONB NOT NULL DEFAULT '{}'::jsonb,
    download_count INTEGER NOT NULL DEFAULT 0,
    view_count INTEGER NOT NULL DEFAULT 0,
    favorite_count INTEGER NOT NULL DEFAULT 0,
    created_by UUID REFERENCES auth.users(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_wallpapers_status ON public.wallpapers(status);
CREATE INDEX IF NOT EXISTS idx_wallpapers_category ON public.wallpapers(category_id);
CREATE INDEX IF NOT EXISTS idx_wallpapers_content_type ON public.wallpapers(content_type);
CREATE INDEX IF NOT EXISTS idx_wallpapers_live_experience_type ON public.wallpapers(live_experience_type);

-- 6. MEDIA ASSETS (R2 storage index & metadata)
CREATE TABLE IF NOT EXISTS public.media_assets (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    storage_key TEXT NOT NULL UNIQUE,
    public_url TEXT,
    slot TEXT,
    wallpaper_id UUID REFERENCES public.wallpapers(id) ON DELETE SET NULL,
    mime_type TEXT NOT NULL,
    width INTEGER,
    height INTEGER,
    duration_seconds NUMERIC(8,2),
    fps NUMERIC(6,2),
    has_audio BOOLEAN NOT NULL DEFAULT false,
    audio_codec TEXT,
    audio_channels INTEGER,
    file_size_bytes BIGINT NOT NULL DEFAULT 0,
    checksum_sha256 TEXT,
    is_linked BOOLEAN NOT NULL DEFAULT false,
    created_by UUID REFERENCES auth.users(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_media_assets_wallpaper ON public.media_assets(wallpaper_id);
CREATE INDEX IF NOT EXISTS idx_media_assets_is_linked ON public.media_assets(is_linked);

-- 7. USER PROFILES & SUBSCRIPTION DATA
CREATE TABLE IF NOT EXISTS public.user_profiles (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    email TEXT,
    display_name TEXT,
    subscription_tier subscription_tier_enum,
    subscription_status subscription_status_enum NOT NULL DEFAULT 'INACTIVE',
    subscription_expires_at TIMESTAMPTZ,
    current_applied_wallpaper_id UUID REFERENCES public.wallpapers(id) ON DELETE SET NULL,
    app_version TEXT,
    device_model TEXT,
    android_api_level INTEGER,
    status TEXT NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    last_active_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 8. GOOGLE PLAY BILLING EVENTS (Authoritative log)
CREATE TABLE IF NOT EXISTS public.billing_events (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES auth.users(id) ON DELETE CASCADE,
    product_id TEXT NOT NULL,
    order_id TEXT,
    purchase_token_hash TEXT NOT NULL,
    event_type TEXT NOT NULL,
    verification_status TEXT NOT NULL,
    verified_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    payload JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_billing_events_user ON public.billing_events(user_id);

-- 9. ADMOB SSV EVENTS
CREATE TABLE IF NOT EXISTS public.admob_ssv_events (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES auth.users(id) ON DELETE SET NULL,
    reward_type TEXT,
    reward_amount NUMERIC(10,2),
    wallpaper_id UUID REFERENCES public.wallpapers(id) ON DELETE SET NULL,
    signature_verified BOOLEAN NOT NULL DEFAULT false,
    raw_query_params JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 10. MODERATION REPORTS
CREATE TABLE IF NOT EXISTS public.moderation_reports (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    reporter_user_id UUID REFERENCES auth.users(id) ON DELETE SET NULL,
    wallpaper_id UUID NOT NULL REFERENCES public.wallpapers(id) ON DELETE CASCADE,
    reason TEXT NOT NULL,
    comments TEXT,
    status report_status_enum NOT NULL DEFAULT 'OPEN',
    reviewer_id UUID REFERENCES auth.users(id),
    resolution_note TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_moderation_status ON public.moderation_reports(status);

-- 11. APP REMOTE CONFIGURATION
CREATE TABLE IF NOT EXISTS public.app_configuration (
    id TEXT PRIMARY KEY DEFAULT 'global_config',
    min_supported_version TEXT NOT NULL DEFAULT '1.0.0',
    latest_version TEXT NOT NULL DEFAULT '1.0.0',
    maintenance_mode BOOLEAN NOT NULL DEFAULT false,
    maintenance_message TEXT,
    featured_wallpaper_ids UUID[] NOT NULL DEFAULT '{}',
    updated_by UUID REFERENCES auth.users(id),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 12. ANNOUNCEMENTS
CREATE TABLE IF NOT EXISTS public.announcements (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    title TEXT NOT NULL,
    body TEXT NOT NULL,
    action_url TEXT,
    target_audience TEXT NOT NULL DEFAULT 'ALL',
    starts_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    expires_at TIMESTAMPTZ,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_by UUID REFERENCES auth.users(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 13. IMMUTABLE ADMIN AUDIT LOGS
CREATE TABLE IF NOT EXISTS public.admin_audit_logs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    admin_user_id UUID NOT NULL REFERENCES auth.users(id),
    admin_email TEXT NOT NULL,
    admin_role admin_role_type NOT NULL,
    action TEXT NOT NULL,
    entity_type TEXT NOT NULL,
    entity_id TEXT,
    old_state JSONB,
    new_state JSONB,
    metadata JSONB NOT NULL DEFAULT '{}'::jsonb,
    ip_address TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_audit_created_at ON public.admin_audit_logs(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_audit_admin ON public.admin_audit_logs(admin_user_id);
CREATE INDEX IF NOT EXISTS idx_audit_entity ON public.admin_audit_logs(entity_type, entity_id);

-- Disable UPDATE / DELETE on audit logs to guarantee immutability
CREATE OR REPLACE FUNCTION public.prevent_audit_log_mutation()
RETURNS TRIGGER AS $$
BEGIN
    RAISE EXCEPTION 'Audit log entries are immutable and cannot be updated or deleted.';
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_prevent_audit_mutation ON public.admin_audit_logs;
CREATE TRIGGER trg_prevent_audit_mutation
BEFORE UPDATE OR DELETE ON public.admin_audit_logs
FOR EACH ROW EXECUTE FUNCTION public.prevent_audit_log_mutation();

-- ====================================================================
-- ROW LEVEL SECURITY (RLS) POLICIES
-- ====================================================================

ALTER TABLE public.admin_users ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.categories ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.tags ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.wallpapers ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.media_assets ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.user_profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.billing_events ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.admob_ssv_events ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.moderation_reports ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.app_configuration ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.announcements ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.admin_audit_logs ENABLE ROW LEVEL SECURITY;

-- 1. admin_users
CREATE POLICY "Admins can view admin_users list"
ON public.admin_users FOR SELECT
USING (public.has_admin_access());

CREATE POLICY "Super Admins can manage admin_users"
ON public.admin_users FOR ALL
USING (public.is_super_admin());

-- 2. categories & tags
CREATE POLICY "Public can view active categories"
ON public.categories FOR SELECT
USING (is_active = true OR public.has_admin_access());

CREATE POLICY "Content Managers and Admins can manage categories"
ON public.categories FOR ALL
USING (
    public.get_current_admin_role() IN ('SUPER_ADMIN', 'ADMIN', 'CONTENT_MANAGER')
);

CREATE POLICY "Public can view active tags"
ON public.tags FOR SELECT
USING (is_active = true OR public.has_admin_access());

CREATE POLICY "Content Managers and Admins can manage tags"
ON public.tags FOR ALL
USING (
    public.get_current_admin_role() IN ('SUPER_ADMIN', 'ADMIN', 'CONTENT_MANAGER')
);

-- 3. wallpapers
CREATE POLICY "Public can view published wallpapers"
ON public.wallpapers FOR SELECT
USING (status = 'PUBLISHED' OR public.has_admin_access());

CREATE POLICY "Content Managers and Admins can insert/update wallpapers"
ON public.wallpapers FOR ALL
USING (
    public.get_current_admin_role() IN ('SUPER_ADMIN', 'ADMIN', 'CONTENT_MANAGER')
);

CREATE POLICY "Moderators can update wallpaper status"
ON public.wallpapers FOR UPDATE
USING (
    public.get_current_admin_role() = 'MODERATOR'
);

-- 4. media_assets
CREATE POLICY "Public view linked media assets"
ON public.media_assets FOR SELECT
USING (is_linked = true OR public.has_admin_access());

CREATE POLICY "Admins and Content Managers manage media assets"
ON public.media_assets FOR ALL
USING (
    public.get_current_admin_role() IN ('SUPER_ADMIN', 'ADMIN', 'CONTENT_MANAGER')
);

-- 5. user_profiles
CREATE POLICY "Users can view and update their own profile"
ON public.user_profiles FOR ALL
USING (auth.uid() = id);

CREATE POLICY "Admins and Support can view user profiles"
ON public.user_profiles FOR SELECT
USING (public.has_admin_access());

CREATE POLICY "Admins can update user profiles"
ON public.user_profiles FOR UPDATE
USING (public.is_admin_or_super());

-- 6. billing_events & admob_ssv_events
CREATE POLICY "Users can view own billing events"
ON public.billing_events FOR SELECT
USING (auth.uid() = user_id OR public.has_admin_access());

CREATE POLICY "Admins and Support view admob events"
ON public.admob_ssv_events FOR SELECT
USING (public.has_admin_access());

-- 7. moderation_reports
CREATE POLICY "Users can create reports"
ON public.moderation_reports FOR INSERT
WITH CHECK (auth.uid() = reporter_user_id);

CREATE POLICY "Admins and Moderators manage reports"
ON public.moderation_reports FOR ALL
USING (
    public.get_current_admin_role() IN ('SUPER_ADMIN', 'ADMIN', 'MODERATOR')
);

-- 8. app_configuration & announcements
CREATE POLICY "Public view app_configuration"
ON public.app_configuration FOR SELECT
USING (true);

CREATE POLICY "Admins manage app_configuration"
ON public.app_configuration FOR ALL
USING (public.is_admin_or_super());

CREATE POLICY "Public view active announcements"
ON public.announcements FOR SELECT
USING (is_active = true OR public.has_admin_access());

CREATE POLICY "Admins manage announcements"
ON public.announcements FOR ALL
USING (public.is_admin_or_super());

-- 9. admin_audit_logs
CREATE POLICY "Admins can view audit logs"
ON public.admin_audit_logs FOR SELECT
USING (public.has_admin_access());

CREATE POLICY "Authenticated admins can insert audit logs"
ON public.admin_audit_logs FOR INSERT
WITH CHECK (public.has_admin_access());
