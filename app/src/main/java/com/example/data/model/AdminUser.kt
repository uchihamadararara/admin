package com.example.data.model

enum class AdminRole(val displayName: String, val level: Int) {
    SUPER_ADMIN("Super Admin", 100),
    ADMIN("Admin", 80),
    CONTENT_MANAGER("Content Manager", 60),
    MODERATOR("Moderator", 40),
    SUPPORT("Support", 20);

    companion object {
        fun fromString(value: String?): AdminRole {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: SUPPORT
        }
    }
}

data class AdminUser(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val role: AdminRole = AdminRole.SUPPORT,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val createdBy: String = ""
)
