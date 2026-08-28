package com.example.data.repository

import com.example.data.model.AdminRole
import com.example.data.model.AdminUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

sealed class AuthState {
    data object Loading : AuthState()
    data object Unauthenticated : AuthState()
    data class Authenticated(val adminUser: AdminUser) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    val currentFirebaseUser: FirebaseUser?
        get() = auth.currentUser

    fun observeAuthState(): Flow<AuthState> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user == null) {
                trySend(AuthState.Unauthenticated)
            } else {
                // Fetch admin document from admin_users/{uid}
                firestore.collection("admin_users").document(user.uid)
                    .get()
                    .addOnSuccessListener { snapshot ->
                        if (snapshot.exists()) {
                            val isActive = snapshot.getBoolean("isActive") ?: false
                            if (!isActive) {
                                trySend(AuthState.Error("Account is inactive or suspended by Super Admin."))
                            } else {
                                val roleStr = snapshot.getString("role")
                                val role = AdminRole.fromString(roleStr)
                                val adminUser = AdminUser(
                                    uid = user.uid,
                                    email = user.email ?: snapshot.getString("email") ?: "",
                                    displayName = snapshot.getString("displayName") ?: (user.displayName ?: "Admin"),
                                    role = role,
                                    isActive = true,
                                    createdAt = snapshot.getLong("createdAt") ?: System.currentTimeMillis(),
                                    updatedAt = snapshot.getLong("updatedAt") ?: System.currentTimeMillis(),
                                    createdBy = snapshot.getString("createdBy") ?: "system"
                                )
                                trySend(AuthState.Authenticated(adminUser))
                            }
                        } else {
                            trySend(AuthState.Error("Access Denied: UID ${user.uid} is not registered in admin_users."))
                        }
                    }
                    .addOnFailureListener { ex ->
                        trySend(AuthState.Error("Failed to verify admin status: ${ex.localizedMessage}"))
                    }
            }
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    suspend fun signIn(email: String, pass: String): Result<AdminUser> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email.trim(), pass).await()
            val user = authResult.user ?: throw Exception("Authentication returned empty user.")
            
            val snapshot = firestore.collection("admin_users").document(user.uid).get().await()
            if (!snapshot.exists()) {
                auth.signOut()
                return Result.failure(Exception("Access Denied: UID ${user.uid} is not authorized in Firestore admin_users."))
            }

            val isActive = snapshot.getBoolean("isActive") ?: false
            if (!isActive) {
                auth.signOut()
                return Result.failure(Exception("Account is disabled. Contact a Super Admin."))
            }

            val roleStr = snapshot.getString("role")
            val role = AdminRole.fromString(roleStr)
            val adminUser = AdminUser(
                uid = user.uid,
                email = user.email ?: snapshot.getString("email") ?: email,
                displayName = snapshot.getString("displayName") ?: (user.displayName ?: "Admin"),
                role = role,
                isActive = true,
                createdAt = snapshot.getLong("createdAt") ?: System.currentTimeMillis(),
                updatedAt = snapshot.getLong("updatedAt") ?: System.currentTimeMillis(),
                createdBy = snapshot.getString("createdBy") ?: "system"
            )
            Result.success(adminUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun bootstrapSuperAdmin(email: String, pass: String, displayName: String): Result<AdminUser> {
        return try {
            // First check if any admin_users exist in Firestore
            val existingAdmins = firestore.collection("admin_users").limit(1).get().await()
            var user = auth.currentUser

            if (user == null) {
                // Try create or sign in
                try {
                    val authResult = auth.createUserWithEmailAndPassword(email.trim(), pass).await()
                    user = authResult.user
                } catch (_: Exception) {
                    val authResult = auth.signInWithEmailAndPassword(email.trim(), pass).await()
                    user = authResult.user
                }
            }

            if (user == null) {
                return Result.failure(Exception("Failed to obtain authenticated Firebase user."))
            }

            // Only allow bootstrap if either no admins exist yet OR the user is already authenticated
            val adminDoc = firestore.collection("admin_users").document(user.uid).get().await()
            if (!adminDoc.exists() && existingAdmins.isEmpty) {
                val newSuperAdmin = AdminUser(
                    uid = user.uid,
                    email = user.email ?: email,
                    displayName = displayName.ifBlank { "Initial Super Admin" },
                    role = AdminRole.SUPER_ADMIN,
                    isActive = true,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                    createdBy = "system_bootstrap"
                )
                firestore.collection("admin_users").document(user.uid).set(
                    mapOf(
                        "uid" to newSuperAdmin.uid,
                        "email" to newSuperAdmin.email,
                        "displayName" to newSuperAdmin.displayName,
                        "role" to newSuperAdmin.role.name,
                        "isActive" to newSuperAdmin.isActive,
                        "createdAt" to newSuperAdmin.createdAt,
                        "updatedAt" to newSuperAdmin.updatedAt,
                        "createdBy" to newSuperAdmin.createdBy
                    )
                ).await()
                Result.success(newSuperAdmin)
            } else if (adminDoc.exists()) {
                val role = AdminRole.fromString(adminDoc.getString("role"))
                val adminUser = AdminUser(
                    uid = user.uid,
                    email = user.email ?: email,
                    displayName = adminDoc.getString("displayName") ?: displayName,
                    role = role,
                    isActive = adminDoc.getBoolean("isActive") ?: true,
                    createdAt = adminDoc.getLong("createdAt") ?: System.currentTimeMillis(),
                    updatedAt = adminDoc.getLong("updatedAt") ?: System.currentTimeMillis(),
                    createdBy = adminDoc.getString("createdBy") ?: "system"
                )
                Result.success(adminUser)
            } else {
                Result.failure(Exception("Admins already exist. Contact a Super Admin to grant you access."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun signOut() {
        auth.signOut()
    }
}
