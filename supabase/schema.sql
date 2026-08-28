-- ==============================================================================
-- PRODUCTION SUPABASE SCHEMA & RLS POLICIES FOR LIVE WALLPAPER PLATFORM
-- ==============================================================================

-- 1. Enable required extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- 2. Create Enums
DO $$ BEGIN
    CREATE TYPE wallpaper_type AS ENUM ('STATIC', 'LIVE');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE access_type AS ENUM ('FREE', 'PREMIUM');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE content_status AS ENUM ('ACTIVE', 'INACTIVE', 'UNDER_REVIEW', 'REJECTED');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE admin_role AS ENUM ('SUPER_ADMIN', 'ADMIN', 'CONTENT_MANAGER', 'MODERATOR', 'SUPPORT');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE live_experience_type AS ENUM ('NORMAL', 'TRANSITION');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE sub_plan_type AS ENUM ('premium_3_days', 'premium_7_days', 'premium_monthly', 'premium_yearly', 'none');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

-- 3. Categories Table
CREATE TABLE IF NOT EXISTS categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name TEXT NOT NULL UNIQUE,
    slug TEXT NOT NULL UNIQUE,
    description TEXT,
    icon_url TEXT,
    thumbnail_url TEXT,
    is_active BOOLEAN NOT NULL DEFAULT true,
    sort_order INTEGER NOT NULL DEFAULT 0,
    wallpaper_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- 4. Tags Table
CREATE TABLE IF NOT EXISTS tags (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name TEXT NOT NULL UNIQUE,
    is_active BOOLEAN NOT NULL DEFAULT true,
    usage_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- 5. Wallpapers Table (Core Engine)
CREATE TABLE IF NOT EXISTS wallpapers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title TEXT NOT NULL,
    description TEXT,
    type wallpaper_type NOT NULL DEFAULT 'STATIC',
    live_experience_type live_experience_type NOT NULL DEFAULT 'NORMAL',
    access_type access_type NOT NULL DEFAULT 'FREE',
    is_premium BOOLEAN GENERATED ALWAYS AS (access_type = 'PREMIUM') STORED,
    status content_status NOT NULL DEFAULT 'ACTIVE',
    category_id UUID REFERENCES categories(id) ON DELETE SET NULL,
    category TEXT,
    tags TEXT[] DEFAULT '{}',
    is_featured BOOLEAN NOT NULL DEFAULT false,
    is_trending BOOLEAN NOT NULL DEFAULT false,
    is_new BOOLEAN NOT NULL DEFAULT true,
    sort_order INTEGER NOT NULL DEFAULT 0,
    
    -- Media and R2 Storage Paths
    thumbnail_url TEXT NOT NULL,
    preview_url TEXT NOT NULL,
    media_url TEXT NOT NULL,
    file_size_bytes BIGINT NOT NULL DEFAULT 0,
    width INTEGER NOT NULL DEFAULT 1080,
    height INTEGER NOT NULL DEFAULT 1920,
    duration_seconds REAL DEFAULT 0.0,
    fps INTEGER DEFAULT 60,
    aspect_ratio TEXT DEFAULT '9:16',
    
    -- Sound Metadata (Per-Wallpaper Content Level)
    sound_available BOOLEAN NOT NULL DEFAULT false,
    sound_metadata JSONB DEFAULT '{"hasAudioTrack": false, "sampleRate": 44100, "codec": "aac", "defaultVolume": 1.0}'::jsonb,
    
    -- Charging Experience Metadata
    charging_animation_available BOOLEAN NOT NULL DEFAULT false,
    charging_animation_id TEXT,
    charging_animation_type TEXT DEFAULT 'BATTERY_PULSE',
    charging_animation_asset TEXT,
    charging_animation_preview TEXT,
    charging_transition_duration_ms INTEGER DEFAULT 300,
    
    -- Transition Experience Metadata
    transition_available BOOLEAN NOT NULL DEFAULT false,
    transition_type TEXT DEFAULT 'FADE',
    transition_asset TEXT,
    transition_source_state TEXT DEFAULT 'HOME',
    transition_target_state TEXT DEFAULT 'CHARGING',
    transition_duration_ms INTEGER DEFAULT 400,
    
    -- Telemetry Counters
    views_count BIGINT NOT NULL DEFAULT 0,
    previews_count BIGINT NOT NULL DEFAULT 0,
    applies_count BIGINT NOT NULL DEFAULT 0,
    favorites_count BIGINT NOT NULL DEFAULT 0,
    
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- 5b. Wallpaper Assets Table (Multi-Slot Offline Bundles)
CREATE TABLE IF NOT EXISTS wallpaper_assets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    wallpaper_id UUID NOT NULL REFERENCES wallpapers(id) ON DELETE CASCADE,
    slot_type TEXT NOT NULL, -- 'PRIMARY', 'HOME', 'LOCK', 'LOCK_TO_HOME', 'HOME_TO_LOCK', 'HOME_TO_CHARGING', 'LOCK_TO_CHARGING', 'CHARGING_LOOP', 'CHARGING_RETURN'
    storage_key TEXT NOT NULL,
    media_url TEXT NOT NULL,
    mime_type TEXT NOT NULL,
    width INTEGER NOT NULL DEFAULT 1080,
    height INTEGER NOT NULL DEFAULT 1920,
    duration_ms INTEGER NOT NULL DEFAULT 0,
    fps INTEGER NOT NULL DEFAULT 60,
    has_audio BOOLEAN NOT NULL DEFAULT false,
    audio_codec TEXT,
    audio_channels INTEGER,
    file_size_bytes BIGINT NOT NULL DEFAULT 0,
    sha256 TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- 6. Users Table
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY,
    email TEXT,
    account_status TEXT NOT NULL DEFAULT 'ACTIVE',
    is_premium BOOLEAN NOT NULL DEFAULT false,
    subscription_plan sub_plan_type NOT NULL DEFAULT 'none',
    subscription_expires_at TIMESTAMPTZ,
    current_applied_wallpaper_id UUID REFERENCES wallpapers(id) ON DELETE SET NULL,
    oem_brand TEXT DEFAULT 'Unknown',
    app_version TEXT DEFAULT '1.0.0',
    last_active_at TIMESTAMPTZ DEFAULT now(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- 7. Subscriptions & Google Play RTDN Events
CREATE TABLE IF NOT EXISTS google_play_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    order_id TEXT,
    purchase_token_hash TEXT,
    product_id TEXT NOT NULL DEFAULT 'premium_pass',
    base_plan_id TEXT NOT NULL,
    event_type TEXT NOT NULL, -- 'PURCHASE', 'RENEWAL', 'CANCELLATION', 'EXPIRATION', 'RECOVERY', 'RTDN_NOTIFICATION'
    processing_status TEXT NOT NULL DEFAULT 'SUCCESS', -- 'SUCCESS', 'FAILED', 'PENDING'
    failure_reason TEXT,
    event_time TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- 8. Rewarded Ad Events & Server-Side Verification (SSV)
CREATE TABLE IF NOT EXISTS reward_ad_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    wallpaper_id UUID REFERENCES wallpapers(id) ON DELETE SET NULL,
    placement_id TEXT NOT NULL DEFAULT 'rewarded_wallpaper_apply',
    ssv_transaction_id TEXT,
    verification_status TEXT NOT NULL DEFAULT 'VERIFIED',
    verification_failure_reason TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- 9. Moderation Reports Table
CREATE TABLE IF NOT EXISTS moderation_reports (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    wallpaper_id UUID REFERENCES wallpapers(id) ON DELETE CASCADE,
    reported_by_user_id UUID,
    reason TEXT NOT NULL,
    status TEXT NOT NULL DEFAULT 'OPEN', -- 'OPEN', 'IN_REVIEW', 'RESOLVED', 'DISMISSED'
    moderator_notes TEXT,
    action_taken TEXT NOT NULL DEFAULT 'NONE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- 10. Admin Users & Permissions Table
CREATE TABLE IF NOT EXISTS admin_users (
    id UUID PRIMARY KEY,
    email TEXT NOT NULL UNIQUE,
    name TEXT NOT NULL,
    role admin_role NOT NULL DEFAULT 'CONTENT_MANAGER',
    is_active BOOLEAN NOT NULL DEFAULT true,
    last_login_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- 11. Admin Audit Logs Table
CREATE TABLE IF NOT EXISTS admin_audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    admin_id UUID,
    admin_email TEXT NOT NULL,
    action TEXT NOT NULL,
    target_type TEXT NOT NULL,
    target_id TEXT,
    details JSONB,
    ip_address TEXT,
    status TEXT NOT NULL DEFAULT 'SUCCESS',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- 12. Media Assets Registry (Cloudflare R2 Synchronizer)
CREATE TABLE IF NOT EXISTS media_assets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    r2_object_key TEXT NOT NULL UNIQUE,
    filename TEXT NOT NULL,
    mime_type TEXT NOT NULL,
    size_bytes BIGINT NOT NULL DEFAULT 0,
    asset_type TEXT NOT NULL, -- 'IMAGE', 'VIDEO', 'CHARGING_ANIMATION', 'TRANSITION_ASSET', 'THUMBNAIL'
    has_audio BOOLEAN NOT NULL DEFAULT false,
    linked_wallpaper_id UUID REFERENCES wallpapers(id) ON DELETE SET NULL,
    is_orphaned BOOLEAN NOT NULL DEFAULT false,
    upload_status TEXT NOT NULL DEFAULT 'COMPLETED',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- 13. App Configuration Table
CREATE TABLE IF NOT EXISTS app_configurations (
    key TEXT PRIMARY KEY,
    value JSONB NOT NULL,
    description TEXT,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_by UUID
);

-- 14. App Announcements Table
CREATE TABLE IF NOT EXISTS app_announcements (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title TEXT NOT NULL,
    message TEXT NOT NULL,
    start_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    end_date TIMESTAMPTZ NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT true,
    priority INTEGER NOT NULL DEFAULT 0,
    target_audience TEXT NOT NULL DEFAULT 'ALL',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ==============================================================================
-- ROW LEVEL SECURITY (RLS) POLICIES
-- ==============================================================================

ALTER TABLE wallpapers ENABLE ROW LEVEL SECURITY;
ALTER TABLE wallpaper_assets ENABLE ROW LEVEL SECURITY;
ALTER TABLE categories ENABLE ROW LEVEL SECURITY;
ALTER TABLE tags ENABLE ROW LEVEL SECURITY;
ALTER TABLE users ENABLE ROW LEVEL SECURITY;
ALTER TABLE google_play_events ENABLE ROW LEVEL SECURITY;
ALTER TABLE reward_ad_events ENABLE ROW LEVEL SECURITY;
ALTER TABLE moderation_reports ENABLE ROW LEVEL SECURITY;
ALTER TABLE admin_users ENABLE ROW LEVEL SECURITY;
ALTER TABLE admin_audit_logs ENABLE ROW LEVEL SECURITY;
ALTER TABLE media_assets ENABLE ROW LEVEL SECURITY;
ALTER TABLE app_configurations ENABLE ROW LEVEL SECURITY;
ALTER TABLE app_announcements ENABLE ROW LEVEL SECURITY;

-- Helper Function to Check Admin Role Server-Side
CREATE OR REPLACE FUNCTION get_current_admin_role()
RETURNS admin_role AS $$
    SELECT role FROM admin_users 
    WHERE id = auth.uid() AND is_active = true 
    LIMIT 1;
$$ LANGUAGE sql SECURITY DEFINER;

CREATE OR REPLACE FUNCTION is_admin_or_super()
RETURNS BOOLEAN AS $$
    SELECT EXISTS (
        SELECT 1 FROM admin_users 
        WHERE id = auth.uid() 
          AND is_active = true 
          AND role IN ('SUPER_ADMIN', 'ADMIN')
    );
$$ LANGUAGE sql SECURITY DEFINER;

-- Wallpapers: Public can read active content; Admins & Content Managers can full manage
CREATE POLICY "Public Read Active Wallpapers" ON wallpapers
    FOR SELECT USING (status = 'ACTIVE' OR is_admin_or_super() OR (SELECT get_current_admin_role()) IN ('CONTENT_MANAGER', 'MODERATOR'));

CREATE POLICY "Admin Manage Wallpapers" ON wallpapers
    FOR ALL USING (is_admin_or_super() OR (SELECT get_current_admin_role()) = 'CONTENT_MANAGER');

-- Wallpaper Assets (Multi-slot offline bundles)
CREATE POLICY "Public Read Wallpaper Assets" ON wallpaper_assets
    FOR SELECT USING (true);

CREATE POLICY "Admin Manage Wallpaper Assets" ON wallpaper_assets
    FOR ALL USING (is_admin_or_super() OR (SELECT get_current_admin_role()) = 'CONTENT_MANAGER');

-- Categories & Tags: Public can read active; Admins can manage
CREATE POLICY "Public Read Categories" ON categories FOR SELECT USING (is_active = true OR is_admin_or_super() OR (SELECT get_current_admin_role()) = 'CONTENT_MANAGER');
CREATE POLICY "Admin Manage Categories" ON categories FOR ALL USING (is_admin_or_super() OR (SELECT get_current_admin_role()) = 'CONTENT_MANAGER');

CREATE POLICY "Public Read Tags" ON tags FOR SELECT USING (is_active = true OR is_admin_or_super() OR (SELECT get_current_admin_role()) = 'CONTENT_MANAGER');
CREATE POLICY "Admin Manage Tags" ON tags FOR ALL USING (is_admin_or_super() OR (SELECT get_current_admin_role()) = 'CONTENT_MANAGER');

-- Users & Subscriptions: Self-read; Admin / Support can inspect
CREATE POLICY "Self or Admin Read Users" ON users
    FOR SELECT USING (id = auth.uid() OR is_admin_or_super() OR (SELECT get_current_admin_role()) = 'SUPPORT');

CREATE POLICY "Admin Update Users" ON users
    FOR UPDATE USING (is_admin_or_super());

CREATE POLICY "Admin Read Play Events" ON google_play_events
    FOR SELECT USING (is_admin_or_super() OR (SELECT get_current_admin_role()) = 'SUPPORT');

CREATE POLICY "Admin Read Reward Events" ON reward_ad_events
    FOR SELECT USING (is_admin_or_super() OR (SELECT get_current_admin_role()) = 'SUPPORT');

-- Moderation: Moderator & Admin only
CREATE POLICY "Moderator Manage Reports" ON moderation_reports
    FOR ALL USING (is_admin_or_super() OR (SELECT get_current_admin_role()) = 'MODERATOR');

-- Admin Audit Logs: Super Admin and Admin read-only, trigger writes
CREATE POLICY "Admin Read Audit Logs" ON admin_audit_logs
    FOR SELECT USING (is_admin_or_super());

CREATE POLICY "Super Admin Manage Admins" ON admin_users
    FOR ALL USING ((SELECT get_current_admin_role()) = 'SUPER_ADMIN');

CREATE POLICY "Admin Read Admin Users" ON admin_users
    FOR SELECT USING (is_admin_or_super());

-- Media Assets: Admins and Content Managers
CREATE POLICY "Admin Manage Media Assets" ON media_assets
    FOR ALL USING (is_admin_or_super() OR (SELECT get_current_admin_role()) = 'CONTENT_MANAGER');

-- App Config & Announcements: Public read active; Super Admin & Admin manage
CREATE POLICY "Public Read Config" ON app_configurations FOR SELECT USING (true);
CREATE POLICY "Admin Manage Config" ON app_configurations FOR ALL USING (is_admin_or_super());

CREATE POLICY "Public Read Announcements" ON app_announcements FOR SELECT USING (is_active = true);
CREATE POLICY "Admin Manage Announcements" ON app_announcements FOR ALL USING (is_admin_or_super());

-- Indexes for Fast Filtering & Search
CREATE INDEX IF NOT EXISTS idx_wallpapers_status_type ON wallpapers(status, type);
CREATE INDEX IF NOT EXISTS idx_wallpapers_category ON wallpapers(category_id);
CREATE INDEX IF NOT EXISTS idx_wallpapers_is_featured ON wallpapers(is_featured);
CREATE INDEX IF NOT EXISTS idx_wallpapers_is_trending ON wallpapers(is_trending);
CREATE INDEX IF NOT EXISTS idx_wallpapers_is_new ON wallpapers(is_new);
CREATE INDEX IF NOT EXISTS idx_wallpapers_access_type ON wallpapers(access_type);
CREATE INDEX IF NOT EXISTS idx_users_subscription ON users(subscription_plan, is_premium);
CREATE INDEX IF NOT EXISTS idx_admin_audit_logs_created ON admin_audit_logs(created_at DESC);
