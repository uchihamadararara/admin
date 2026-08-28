package com.example.data.repository

import com.example.data.model.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class AdminRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    // ==========================================
    // AUDIT LOGS (Append-only)
    // ==========================================
    suspend fun logAudit(
        adminUser: AdminUser,
        action: String,
        entity: String,
        entityId: String,
        details: String
    ) {
        try {
            val logId = UUID.randomUUID().toString()
            val logData = mapOf(
                "id" to logId,
                "adminUid" to adminUser.uid,
                "adminEmail" to adminUser.email,
                "role" to adminUser.role.name,
                "action" to action,
                "entity" to entity,
                "entityId" to entityId,
                "details" to details,
                "timestamp" to System.currentTimeMillis()
            )
            firestore.collection("admin_audit_logs").document(logId).set(logData).await()
        } catch (_: Exception) {
            // Fail gracefully without crashing the console
        }
    }

    fun observeAuditLogs(): Flow<List<AuditLog>> = callbackFlow {
        val listener = firestore.collection("admin_audit_logs")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(100)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    AuditLog(
                        id = doc.getString("id") ?: doc.id,
                        adminUid = doc.getString("adminUid") ?: "",
                        adminEmail = doc.getString("adminEmail") ?: "",
                        role = doc.getString("role") ?: "",
                        action = doc.getString("action") ?: "",
                        entity = doc.getString("entity") ?: "",
                        entityId = doc.getString("entityId") ?: "",
                        details = doc.getString("details") ?: "",
                        timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
                    )
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    // ==========================================
    // WALLPAPERS CRUD
    // ==========================================
    fun observeWallpapers(): Flow<List<Wallpaper>> = callbackFlow {
        val listener = firestore.collection("wallpapers")
            .orderBy("updatedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    docToWallpaper(doc.id, doc.data)
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    suspend fun getWallpaperById(id: String): Wallpaper? {
        val doc = firestore.collection("wallpapers").document(id).get().await()
        if (!doc.exists()) return null
        return docToWallpaper(doc.id, doc.data)
    }

    suspend fun saveWallpaper(wallpaper: Wallpaper, adminUser: AdminUser): Result<String> {
        return try {
            val id = wallpaper.id.ifBlank { UUID.randomUUID().toString() }
            val now = System.currentTimeMillis()
            val isNew = wallpaper.id.isBlank()

            val map = mutableMapOf<String, Any?>(
                "id" to id,
                "title" to wallpaper.title.trim(),
                "description" to wallpaper.description.trim(),
                "contentType" to wallpaper.contentType.name,
                "liveExperienceType" to wallpaper.liveExperienceType?.name,
                "accessType" to wallpaper.accessType.name,
                "status" to wallpaper.status.name,
                "categoryId" to wallpaper.categoryId,
                "tags" to wallpaper.tags,
                "isFeatured" to wallpaper.isFeatured,
                "isTrending" to wallpaper.isTrending,
                "isNew" to wallpaper.isNew,
                "sortOrder" to wallpaper.sortOrder,
                "thumbnailUrl" to wallpaper.thumbnailUrl.trim(),
                "previewUrl" to wallpaper.previewUrl.trim(),
                "primaryMediaUrl" to wallpaper.primaryMediaUrl.trim(),
                "hasAudio" to wallpaper.hasAudio,
                "audioCodec" to wallpaper.audioCodec,
                "durationMs" to wallpaper.durationMs,
                "width" to wallpaper.width,
                "height" to wallpaper.height,
                "fps" to wallpaper.fps,
                "fileSizeBytes" to wallpaper.fileSizeBytes,
                "viewsCount" to wallpaper.viewsCount,
                "appliesCount" to wallpaper.appliesCount,
                "favoritesCount" to wallpaper.favoritesCount,
                "advancedConfig" to wallpaper.advancedConfig.toMap(),
                "updatedAt" to now,
                "updatedBy" to adminUser.email
            )

            if (isNew) {
                map["createdAt"] = now
                map["createdBy"] = adminUser.email
            }

            firestore.collection("wallpapers").document(id).set(map).await()

            logAudit(
                adminUser = adminUser,
                action = if (isNew) "CREATE_WALLPAPER" else "UPDATE_WALLPAPER",
                entity = "WALLPAPER",
                entityId = id,
                details = "${wallpaper.title} (${wallpaper.contentType.name}${wallpaper.liveExperienceType?.let { " - $it" } ?: ""})"
            )

            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateWallpaperStatus(
        id: String,
        newStatus: WallpaperStatus,
        adminUser: AdminUser
    ): Result<Unit> {
        return try {
            firestore.collection("wallpapers").document(id).update(
                mapOf(
                    "status" to newStatus.name,
                    "updatedAt" to System.currentTimeMillis(),
                    "updatedBy" to adminUser.email
                )
            ).await()

            logAudit(
                adminUser = adminUser,
                action = "SET_STATUS_${newStatus.name}",
                entity = "WALLPAPER",
                entityId = id,
                details = "Status changed to ${newStatus.name}"
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteWallpaper(id: String, title: String, adminUser: AdminUser): Result<Unit> {
        return try {
            firestore.collection("wallpapers").document(id).delete().await()
            logAudit(
                adminUser = adminUser,
                action = "DELETE_WALLPAPER",
                entity = "WALLPAPER",
                entityId = id,
                details = "Deleted wallpaper: $title"
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun docToWallpaper(id: String, data: Map<String, Any?>?): Wallpaper? {
        if (data == null) return null
        return Wallpaper(
            id = id,
            title = data["title"] as? String ?: "Untitled",
            description = data["description"] as? String ?: "",
            contentType = ContentType.fromString(data["contentType"] as? String),
            liveExperienceType = LiveExperienceType.fromString(data["liveExperienceType"] as? String),
            accessType = AccessType.fromString(data["accessType"] as? String),
            status = WallpaperStatus.fromString(data["status"] as? String),
            categoryId = data["categoryId"] as? String ?: "",
            tags = (data["tags"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
            isFeatured = data["isFeatured"] as? Boolean ?: false,
            isTrending = data["isTrending"] as? Boolean ?: false,
            isNew = data["isNew"] as? Boolean ?: false,
            sortOrder = (data["sortOrder"] as? Number)?.toInt() ?: 0,
            thumbnailUrl = data["thumbnailUrl"] as? String ?: "",
            previewUrl = data["previewUrl"] as? String ?: "",
            primaryMediaUrl = data["primaryMediaUrl"] as? String ?: "",
            hasAudio = data["hasAudio"] as? Boolean ?: false,
            audioCodec = data["audioCodec"] as? String,
            durationMs = (data["durationMs"] as? Number)?.toLong(),
            width = (data["width"] as? Number)?.toInt(),
            height = (data["height"] as? Number)?.toInt(),
            fps = (data["fps"] as? Number)?.toInt(),
            fileSizeBytes = (data["fileSizeBytes"] as? Number)?.toLong(),
            viewsCount = (data["viewsCount"] as? Number)?.toLong() ?: 0L,
            appliesCount = (data["appliesCount"] as? Number)?.toLong() ?: 0L,
            favoritesCount = (data["favoritesCount"] as? Number)?.toLong() ?: 0L,
            advancedConfig = AdvancedConfig.fromMap(data["advancedConfig"] as? Map<String, Any?>),
            createdAt = (data["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
            updatedAt = (data["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
            createdBy = data["createdBy"] as? String ?: "",
            updatedBy = data["updatedBy"] as? String ?: ""
        )
    }

    // ==========================================
    // MEDIA LIBRARY (R2 URL Registry)
    // ==========================================
    fun observeMediaAssets(): Flow<List<MediaAsset>> = callbackFlow {
        val listener = firestore.collection("media_assets")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    MediaAsset(
                        id = doc.id,
                        url = doc.getString("url") ?: "",
                        title = doc.getString("title") ?: "",
                        mimeType = doc.getString("mimeType") ?: "video/mp4",
                        width = (doc.get("width") as? Number)?.toInt(),
                        height = (doc.get("height") as? Number)?.toInt(),
                        durationMs = (doc.get("durationMs") as? Number)?.toLong(),
                        fps = (doc.get("fps") as? Number)?.toInt(),
                        hasAudio = doc.getBoolean("hasAudio") ?: false,
                        audioCodec = doc.getString("audioCodec"),
                        fileSizeBytes = (doc.get("fileSizeBytes") as? Number)?.toLong(),
                        sha256 = doc.getString("sha256"),
                        linkedWallpaperId = doc.getString("linkedWallpaperId"),
                        slot = doc.getString("slot"),
                        createdAt = (doc.get("createdAt") as? Number)?.toLong() ?: System.currentTimeMillis()
                    )
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    suspend fun saveMediaAsset(asset: MediaAsset, adminUser: AdminUser): Result<String> {
        return try {
            val id = asset.id.ifBlank { UUID.randomUUID().toString() }
            val map = mapOf(
                "id" to id,
                "url" to asset.url.trim(),
                "title" to asset.title.trim(),
                "mimeType" to asset.mimeType,
                "width" to asset.width,
                "height" to asset.height,
                "durationMs" to asset.durationMs,
                "fps" to asset.fps,
                "hasAudio" to asset.hasAudio,
                "audioCodec" to asset.audioCodec,
                "fileSizeBytes" to asset.fileSizeBytes,
                "sha256" to asset.sha256,
                "linkedWallpaperId" to asset.linkedWallpaperId,
                "slot" to asset.slot,
                "createdAt" to asset.createdAt
            )
            firestore.collection("media_assets").document(id).set(map).await()
            logAudit(
                adminUser = adminUser,
                action = "REGISTER_MEDIA_URL",
                entity = "MEDIA_ASSET",
                entityId = id,
                details = "${asset.title} (${asset.url})"
            )
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteMediaAsset(id: String, url: String, adminUser: AdminUser): Result<Unit> {
        return try {
            firestore.collection("media_assets").document(id).delete().await()
            logAudit(
                adminUser = adminUser,
                action = "REMOVE_MEDIA_REGISTRY",
                entity = "MEDIA_ASSET",
                entityId = id,
                details = "Removed registry reference: $url"
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================================
    // CATEGORIES & TAGS
    // ==========================================
    fun observeCategories(): Flow<List<Category>> = callbackFlow {
        val listener = firestore.collection("categories")
            .orderBy("sortOrder", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    Category(
                        id = doc.id,
                        name = doc.getString("name") ?: "",
                        slug = doc.getString("slug") ?: "",
                        description = doc.getString("description") ?: "",
                        iconUrl = doc.getString("iconUrl") ?: "",
                        sortOrder = (doc.get("sortOrder") as? Number)?.toInt() ?: 0,
                        isActive = doc.getBoolean("isActive") ?: true,
                        wallpapersCount = (doc.get("wallpapersCount") as? Number)?.toInt() ?: 0,
                        createdAt = (doc.get("createdAt") as? Number)?.toLong() ?: System.currentTimeMillis()
                    )
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    suspend fun saveCategory(category: Category, adminUser: AdminUser): Result<String> {
        return try {
            val id = category.id.ifBlank { UUID.randomUUID().toString() }
            val map = mapOf(
                "id" to id,
                "name" to category.name.trim(),
                "slug" to category.slug.trim(),
                "description" to category.description.trim(),
                "iconUrl" to category.iconUrl.trim(),
                "sortOrder" to category.sortOrder,
                "isActive" to category.isActive,
                "wallpapersCount" to category.wallpapersCount,
                "createdAt" to category.createdAt
            )
            firestore.collection("categories").document(id).set(map).await()
            logAudit(
                adminUser = adminUser,
                action = if (category.id.isBlank()) "CREATE_CATEGORY" else "UPDATE_CATEGORY",
                entity = "CATEGORY",
                entityId = id,
                details = category.name
            )
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteCategory(id: String, name: String, adminUser: AdminUser): Result<Unit> {
        return try {
            firestore.collection("categories").document(id).delete().await()
            logAudit(
                adminUser = adminUser,
                action = "DELETE_CATEGORY",
                entity = "CATEGORY",
                entityId = id,
                details = "Deleted: $name"
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun observeTags(): Flow<List<Tag>> = callbackFlow {
        val listener = firestore.collection("tags")
            .orderBy("name", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    Tag(
                        id = doc.id,
                        name = doc.getString("name") ?: "",
                        slug = doc.getString("slug") ?: "",
                        usageCount = (doc.get("usageCount") as? Number)?.toInt() ?: 0,
                        isActive = doc.getBoolean("isActive") ?: true
                    )
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    suspend fun saveTag(tag: Tag, adminUser: AdminUser): Result<String> {
        return try {
            val id = tag.id.ifBlank { UUID.randomUUID().toString() }
            val map = mapOf(
                "id" to id,
                "name" to tag.name.trim(),
                "slug" to tag.slug.trim(),
                "usageCount" to tag.usageCount,
                "isActive" to tag.isActive
            )
            firestore.collection("tags").document(id).set(map).await()
            logAudit(
                adminUser = adminUser,
                action = if (tag.id.isBlank()) "CREATE_TAG" else "UPDATE_TAG",
                entity = "TAG",
                entityId = id,
                details = tag.name
            )
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteTag(id: String, name: String, adminUser: AdminUser): Result<Unit> {
        return try {
            firestore.collection("tags").document(id).delete().await()
            logAudit(
                adminUser = adminUser,
                action = "DELETE_TAG",
                entity = "TAG",
                entityId = id,
                details = "Deleted tag: $name"
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================================
    // USERS (View & Status Moderation)
    // ==========================================
    fun observeUsers(): Flow<List<UserProfile>> = callbackFlow {
        val listener = firestore.collection("users")
            .limit(200)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    UserProfile(
                        uid = doc.id,
                        email = doc.getString("email") ?: "",
                        displayName = doc.getString("displayName") ?: "",
                        subscriptionTier = SubscriptionTier.fromString(doc.getString("subscriptionTier")),
                        subscriptionStatus = SubscriptionStatus.fromString(doc.getString("subscriptionStatus")),
                        subscriptionExpiry = (doc.get("subscriptionExpiry") as? Number)?.toLong(),
                        activeSku = doc.getString("activeSku"),
                        currentAppliedWallpaperId = doc.getString("currentAppliedWallpaperId"),
                        currentAppliedWallpaperTitle = doc.getString("currentAppliedWallpaperTitle"),
                        isAppliedWallpaperPremium = doc.getBoolean("isAppliedWallpaperPremium") ?: false,
                        appliedWallpaperSoundEnabled = doc.getBoolean("appliedWallpaperSoundEnabled") ?: false,
                        accountStatus = AccountStatus.fromString(doc.getString("accountStatus")),
                        createdAt = (doc.get("createdAt") as? Number)?.toLong() ?: System.currentTimeMillis(),
                        lastActiveAt = (doc.get("lastActiveAt") as? Number)?.toLong()
                    )
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    suspend fun updateUserAccountStatus(
        uid: String,
        newStatus: AccountStatus,
        adminUser: AdminUser
    ): Result<Unit> {
        return try {
            firestore.collection("users").document(uid).update("accountStatus", newStatus.name).await()
            logAudit(
                adminUser = adminUser,
                action = "SET_USER_STATUS_${newStatus.name}",
                entity = "USER",
                entityId = uid,
                details = "User account status set to ${newStatus.name}"
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================================
    // BILLING & ADMOB SSV (Read-only monitoring)
    // ==========================================
    fun observeBillingEvents(): Flow<List<BillingEvent>> = callbackFlow {
        val listener = firestore.collection("billing_events")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(100)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    BillingEvent(
                        id = doc.id,
                        uid = doc.getString("uid") ?: "",
                        sku = doc.getString("sku") ?: "",
                        orderId = doc.getString("orderId") ?: "",
                        purchaseToken = doc.getString("purchaseToken") ?: "",
                        purchaseTime = (doc.get("purchaseTime") as? Number)?.toLong() ?: System.currentTimeMillis(),
                        verificationState = VerificationState.fromString(doc.getString("verificationState")),
                        amount = doc.getString("amount") ?: "",
                        currency = doc.getString("currency") ?: "USD",
                        createdAt = (doc.get("createdAt") as? Number)?.toLong() ?: System.currentTimeMillis()
                    )
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    fun observeSSVEvents(): Flow<List<AdMobSSVEvent>> = callbackFlow {
        val listener = firestore.collection("admob_ssv_events")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(100)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    AdMobSSVEvent(
                        id = doc.id,
                        uid = doc.getString("uid") ?: "",
                        rewardType = doc.getString("rewardType") ?: "",
                        rewardAmount = (doc.get("rewardAmount") as? Number)?.toInt() ?: 0,
                        customData = doc.getString("customData"),
                        verificationState = VerificationState.fromString(doc.getString("verificationState")),
                        timestamp = (doc.get("timestamp") as? Number)?.toLong() ?: System.currentTimeMillis()
                    )
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    // ==========================================
    // MODERATION
    // ==========================================
    fun observeModerationReports(): Flow<List<ModerationReport>> = callbackFlow {
        val listener = firestore.collection("moderation_reports")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    ModerationReport(
                        id = doc.id,
                        reporterUid = doc.getString("reporterUid") ?: "",
                        reporterEmail = doc.getString("reporterEmail"),
                        targetType = doc.getString("targetType") ?: "WALLPAPER",
                        targetId = doc.getString("targetId") ?: "",
                        targetTitle = doc.getString("targetTitle"),
                        reason = doc.getString("reason") ?: "",
                        comments = doc.getString("comments") ?: "",
                        status = ReportStatus.fromString(doc.getString("status")),
                        reviewedBy = doc.getString("reviewedBy"),
                        resolutionNotes = doc.getString("resolutionNotes"),
                        createdAt = (doc.get("createdAt") as? Number)?.toLong() ?: System.currentTimeMillis(),
                        updatedAt = (doc.get("updatedAt") as? Number)?.toLong() ?: System.currentTimeMillis()
                    )
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    suspend fun resolveReport(
        reportId: String,
        status: ReportStatus,
        notes: String,
        adminUser: AdminUser
    ): Result<Unit> {
        return try {
            firestore.collection("moderation_reports").document(reportId).update(
                mapOf(
                    "status" to status.name,
                    "reviewedBy" to adminUser.email,
                    "resolutionNotes" to notes.trim(),
                    "updatedAt" to System.currentTimeMillis()
                )
            ).await()

            logAudit(
                adminUser = adminUser,
                action = "MODERATION_${status.name}",
                entity = "REPORT",
                entityId = reportId,
                details = "Report status set to ${status.name}. Notes: $notes"
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================================
    // ANNOUNCEMENTS
    // ==========================================
    fun observeAnnouncements(): Flow<List<Announcement>> = callbackFlow {
        val listener = firestore.collection("announcements")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    Announcement(
                        id = doc.id,
                        title = doc.getString("title") ?: "",
                        message = doc.getString("message") ?: "",
                        actionUrl = doc.getString("actionUrl"),
                        targetAudience = TargetAudience.fromString(doc.getString("targetAudience")),
                        isActive = doc.getBoolean("isActive") ?: true,
                        startTime = (doc.get("startTime") as? Number)?.toLong(),
                        expiryTime = (doc.get("expiryTime") as? Number)?.toLong(),
                        createdAt = (doc.get("createdAt") as? Number)?.toLong() ?: System.currentTimeMillis(),
                        updatedAt = (doc.get("updatedAt") as? Number)?.toLong() ?: System.currentTimeMillis()
                    )
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    suspend fun saveAnnouncement(announcement: Announcement, adminUser: AdminUser): Result<String> {
        return try {
            val id = announcement.id.ifBlank { UUID.randomUUID().toString() }
            val map = mapOf(
                "id" to id,
                "title" to announcement.title.trim(),
                "message" to announcement.message.trim(),
                "actionUrl" to announcement.actionUrl?.trim(),
                "targetAudience" to announcement.targetAudience.name,
                "isActive" to announcement.isActive,
                "startTime" to announcement.startTime,
                "expiryTime" to announcement.expiryTime,
                "createdAt" to announcement.createdAt,
                "updatedAt" to System.currentTimeMillis()
            )
            firestore.collection("announcements").document(id).set(map).await()
            logAudit(
                adminUser = adminUser,
                action = if (announcement.id.isBlank()) "CREATE_ANNOUNCEMENT" else "UPDATE_ANNOUNCEMENT",
                entity = "ANNOUNCEMENT",
                entityId = id,
                details = announcement.title
            )
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteAnnouncement(id: String, title: String, adminUser: AdminUser): Result<Unit> {
        return try {
            firestore.collection("announcements").document(id).delete().await()
            logAudit(
                adminUser = adminUser,
                action = "DELETE_ANNOUNCEMENT",
                entity = "ANNOUNCEMENT",
                entityId = id,
                details = "Deleted: $title"
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================================
    // APP CONFIGURATION
    // ==========================================
    fun observeAppConfig(): Flow<AppConfig> = callbackFlow {
        val listener = firestore.collection("app_configuration").document("global")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || !snapshot.exists()) {
                    trySend(AppConfig())
                    return@addSnapshotListener
                }
                val config = AppConfig(
                    maintenanceMode = snapshot.getBoolean("maintenanceMode") ?: false,
                    maintenanceMessage = snapshot.getString("maintenanceMessage") ?: "System maintenance in progress.",
                    minSupportedVersionCode = (snapshot.get("minSupportedVersionCode") as? Number)?.toInt() ?: 1,
                    latestVersionCode = (snapshot.get("latestVersionCode") as? Number)?.toInt() ?: 1,
                    interstitialAdIntervalMinutes = (snapshot.get("interstitialAdIntervalMinutes") as? Number)?.toInt() ?: 15,
                    rewardedAdDailyLimit = (snapshot.get("rewardedAdDailyLimit") as? Number)?.toInt() ?: 10,
                    enableTransitionWallpapers = snapshot.getBoolean("enableTransitionWallpapers") ?: true,
                    enableChargingExperience = snapshot.getBoolean("enableChargingExperience") ?: true,
                    updatedAt = (snapshot.get("updatedAt") as? Number)?.toLong() ?: System.currentTimeMillis(),
                    updatedBy = snapshot.getString("updatedBy") ?: "system"
                )
                trySend(config)
            }
        awaitClose { listener.remove() }
    }

    suspend fun saveAppConfig(config: AppConfig, adminUser: AdminUser): Result<Unit> {
        return try {
            val map = mapOf(
                "maintenanceMode" to config.maintenanceMode,
                "maintenanceMessage" to config.maintenanceMessage.trim(),
                "minSupportedVersionCode" to config.minSupportedVersionCode,
                "latestVersionCode" to config.latestVersionCode,
                "interstitialAdIntervalMinutes" to config.interstitialAdIntervalMinutes,
                "rewardedAdDailyLimit" to config.rewardedAdDailyLimit,
                "enableTransitionWallpapers" to config.enableTransitionWallpapers,
                "enableChargingExperience" to config.enableChargingExperience,
                "updatedAt" to System.currentTimeMillis(),
                "updatedBy" to adminUser.email
            )
            firestore.collection("app_configuration").document("global").set(map).await()
            logAudit(
                adminUser = adminUser,
                action = "UPDATE_APP_CONFIG",
                entity = "APP_CONFIG",
                entityId = "global",
                details = "Maintenance: ${config.maintenanceMode}, MinVer: ${config.minSupportedVersionCode}, LatestVer: ${config.latestVersionCode}"
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================================
    // ADMIN USERS MANAGEMENT (Super Admin only)
    // ==========================================
    fun observeAdminUsers(): Flow<List<AdminUser>> = callbackFlow {
        val listener = firestore.collection("admin_users")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    AdminUser(
                        uid = doc.id,
                        email = doc.getString("email") ?: "",
                        displayName = doc.getString("displayName") ?: "",
                        role = AdminRole.fromString(doc.getString("role")),
                        isActive = doc.getBoolean("isActive") ?: true,
                        createdAt = (doc.get("createdAt") as? Number)?.toLong() ?: System.currentTimeMillis(),
                        updatedAt = (doc.get("updatedAt") as? Number)?.toLong() ?: System.currentTimeMillis(),
                        createdBy = doc.getString("createdBy") ?: "system"
                    )
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    suspend fun saveAdminUser(targetUser: AdminUser, actor: AdminUser): Result<Unit> {
        return try {
            val map = mapOf(
                "uid" to targetUser.uid,
                "email" to targetUser.email.trim(),
                "displayName" to targetUser.displayName.trim(),
                "role" to targetUser.role.name,
                "isActive" to targetUser.isActive,
                "createdAt" to targetUser.createdAt,
                "updatedAt" to System.currentTimeMillis(),
                "createdBy" to targetUser.createdBy.ifBlank { actor.email }
            )
            firestore.collection("admin_users").document(targetUser.uid).set(map).await()
            logAudit(
                adminUser = actor,
                action = "SET_ADMIN_ROLE_${targetUser.role.name}",
                entity = "ADMIN_USER",
                entityId = targetUser.uid,
                details = "Updated ${targetUser.email} to ${targetUser.role.name} (Active: ${targetUser.isActive})"
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
