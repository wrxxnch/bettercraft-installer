package com.example.model

data class UserProfile(
    val email: String,
    val displayName: String? = null,
    val photoUrl: String? = null,
    val isSuperAdmin: Boolean = false,
    val isAdmin: Boolean = false
)

object AdminConstants {
    const val SUPER_ADMIN_EMAIL = "jeanpierreowner@gmail.com"

    fun isSuperAdmin(email: String?): Boolean {
        if (email.isNullOrBlank()) return false
        return email.trim().equals(SUPER_ADMIN_EMAIL, ignoreCase = true)
    }

    fun isAdmin(email: String?, additionalAdmins: List<String> = emptyList()): Boolean {
        if (email.isNullOrBlank()) return false
        val cleanEmail = email.trim().lowercase()
        if (cleanEmail == SUPER_ADMIN_EMAIL.lowercase()) return true
        return additionalAdmins.any { it.trim().lowercase() == cleanEmail }
    }
}
