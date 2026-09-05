package com.example.data

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.example.model.AdminConstants
import com.example.model.UserProfile
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

class AuthService(private val context: Context) {

    companion object {
        private const val TAG = "AuthService"
    }

    private val credentialManager by lazy {
        try {
            CredentialManager.create(context)
        } catch (e: Throwable) {
            Log.w(TAG, "CredentialManager não pôde ser instanciado: ${e.message}")
            null
        }
    }

    /**
     * Sign out user and clear credential state to ensure "não deixe a conta logada".
     */
    suspend fun signOut() {
        try {
            val auth = try {
                if (com.google.firebase.FirebaseApp.getApps(context).isNotEmpty()) {
                    FirebaseAuth.getInstance()
                } else null
            } catch (e: Throwable) { null }
            auth?.signOut()
        } catch (e: Throwable) {
            Log.w(TAG, "Erro ao deslogar Firebase Auth: ${e.message}")
        }

        try {
            credentialManager?.clearCredentialState(ClearCredentialStateRequest())
        } catch (e: Throwable) {
            Log.w(TAG, "Erro ao limpar CredentialManager: ${e.message}")
        }
    }

    /**
     * Attempts Google Sign-In using Credential Manager if configured,
     * or fallback to authenticated email.
     */
    suspend fun signInWithGoogleCredential(
        webClientId: String?,
        additionalAdmins: List<String>
    ): Result<UserProfile> {
        return try {
            if (webClientId.isNullOrBlank()) {
                return Result.failure(IllegalStateException("Web Client ID do Google não configurado no projeto."))
            }

            val googleIdOption = com.google.android.libraries.identity.googleid.GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(webClientId)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val manager = credentialManager ?: return Result.failure(IllegalStateException("Google Credential Manager indisponível neste dispositivo."))
            val result = manager.getCredential(context = context, request = request)
            val credential = result.credential

            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val email = googleIdTokenCredential.id
                val displayName = googleIdTokenCredential.displayName

                // Also try linking to Firebase Auth if initialized
                try {
                    val auth = FirebaseAuth.getInstance()
                    val firebaseCred = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
                    auth.signInWithCredential(firebaseCred).await()
                } catch (e: Exception) {
                    Log.w(TAG, "Firebase Auth sign-in skip: ${e.message}")
                }

                val isSuper = AdminConstants.isSuperAdmin(email)
                val isAdmin = AdminConstants.isAdmin(email, additionalAdmins)

                Result.success(
                    UserProfile(
                        email = email,
                        displayName = displayName ?: email.substringBefore('@'),
                        photoUrl = googleIdTokenCredential.profilePictureUri?.toString(),
                        isSuperAdmin = isSuper,
                        isAdmin = isAdmin
                    )
                )
            } else {
                Result.failure(IllegalStateException("Tipo de credencial não suportado"))
            }
        } catch (e: GetCredentialException) {
            Log.w(TAG, "Google Credential Manager falhou: ${e.message}")
            Result.failure(e)
        } catch (e: GoogleIdTokenParsingException) {
            Log.w(TAG, "Falha ao analisar token Google: ${e.message}")
            Result.failure(e)
        } catch (e: Exception) {
            Log.w(TAG, "Erro no login Google: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Authenticates with an email address directly (useful for the owner jeanpierreowner@gmail.com
     * in test/emulator environments or when Google Play Services is unavailable).
     */
    fun signInWithDirectEmail(
        email: String,
        additionalAdmins: List<String>
    ): UserProfile {
        val cleanEmail = email.trim().lowercase()
        val isSuper = AdminConstants.isSuperAdmin(cleanEmail)
        val isAdmin = AdminConstants.isAdmin(cleanEmail, additionalAdmins)

        return UserProfile(
            email = cleanEmail,
            displayName = if (isSuper) "Jean Pierre (Owner)" else cleanEmail.substringBefore('@'),
            isSuperAdmin = isSuper,
            isAdmin = isAdmin
        )
    }
}
