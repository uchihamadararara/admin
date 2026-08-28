package com.example.data.model

data class Category(
    val id: String = "",
    val name: String = "",
    val slug: String = "",
    val description: String = "",
    val iconUrl: String = "",
    val sortOrder: Int = 0,
    val isActive: Boolean = true,
    val wallpapersCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

data class Tag(
    val id: String = "",
    val name: String = "",
    val slug: String = "",
    val usageCount: Int = 0,
    val isActive: Boolean = true
)
