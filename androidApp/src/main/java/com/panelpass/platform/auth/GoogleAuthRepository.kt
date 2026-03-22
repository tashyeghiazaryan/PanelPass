package com.panelpass.platform.auth

import android.content.Context
import com.panelpass.R
import com.panelpass.features.auth.domain.AuthRepository
import com.panelpass.features.auth.domain.User
import com.panelpass.shell.ActivityHolder
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.withContext

/**
 * Android implementation of [AuthRepository] using Google Sign In.
 */
internal class GoogleAuthRepository(
    private val context: Context,
) : AuthRepository {

    private val prefs by lazy {
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private val gso: GoogleSignInOptions by lazy {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestProfile()
            .requestIdToken(getWebClientId())
            .build()
    }

    private val signInClient: GoogleSignInClient by lazy {
        GoogleSignIn.getClient(context, gso)
    }

    private val signInResultChannel = Channel<Result<User>>(Channel.RENDEZVOUS)

    override suspend fun signIn(): Result<User> = withContext(Dispatchers.Main) {
        clearEmailSession()
        val activity = ActivityHolder.get()
            ?: return@withContext Result.failure(
                IllegalStateException("No Activity registered. Ensure ActivityHolder.set(activity) is called in onResume."),
            )
        activity.startActivityForResult(signInClient.signInIntent, REQUEST_CODE_SIGN_IN)
        signInResultChannel.receive()
    }

    override suspend fun signInWithEmail(email: String, password: String): Result<User> {
        val trimmed = email.trim()
        if (trimmed.isEmpty() || !trimmed.contains('@')) {
            return Result.failure(IllegalArgumentException("Invalid email"))
        }
        if (password.length < MIN_PASSWORD_LENGTH) {
            return Result.failure(
                IllegalArgumentException("Password must be at least $MIN_PASSWORD_LENGTH characters"),
            )
        }
        val user = User(
            id = "email:${trimmed.lowercase()}",
            email = trimmed,
            name = trimmed.substringBefore('@').takeIf { it.isNotEmpty() },
        )
        withContext(Dispatchers.Main) {
            signInClient.signOut()
        }
        saveEmailSession(user)
        return Result.success(user)
    }

    internal fun deliverSignInResult(data: android.content.Intent?) {
        if (data == null) {
            signInResultChannel.trySend(Result.failure(IllegalStateException("No result data")))
            return
        }
        val task = com.google.android.gms.auth.api.signin.GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
            val account = task.getResult(ApiException::class.java)
            signInResultChannel.trySend(Result.success(account.toUser()))
        } catch (e: ApiException) {
            signInResultChannel.trySend(Result.failure(e))
        }
    }

    override suspend fun signOut() {
        withContext(Dispatchers.Main) {
            clearEmailSession()
            signInClient.signOut()
        }
    }

    override suspend fun getCurrentUser(): User? = withContext(Dispatchers.Main) {
        loadEmailUser() ?: GoogleSignIn.getLastSignedInAccount(context)?.toUser()
    }

    override fun isSignInAvailable(): Boolean = true

    private fun getWebClientId(): String {
        return try {
            context.getString(R.string.default_web_client_id)
        } catch (e: Exception) {
            ""
        }
    }

    private fun saveEmailSession(user: User) {
        prefs.edit()
            .putString(KEY_EMAIL_USER_ID, user.id)
            .putString(KEY_EMAIL_USER_EMAIL, user.email)
            .putString(KEY_EMAIL_USER_NAME, user.name)
            .apply()
    }

    private fun clearEmailSession() {
        prefs.edit()
            .remove(KEY_EMAIL_USER_ID)
            .remove(KEY_EMAIL_USER_EMAIL)
            .remove(KEY_EMAIL_USER_NAME)
            .apply()
    }

    private fun loadEmailUser(): User? {
        val id = prefs.getString(KEY_EMAIL_USER_ID, null) ?: return null
        return User(
            id = id,
            email = prefs.getString(KEY_EMAIL_USER_EMAIL, null),
            name = prefs.getString(KEY_EMAIL_USER_NAME, null),
        )
    }

    private fun GoogleSignInAccount.toUser(): User = User(
        id = id ?: "",
        email = email,
        name = displayName,
    )

    companion object {
        const val REQUEST_CODE_SIGN_IN = 9001
        private const val PREFS_NAME = "panelpass_auth"
        private const val KEY_EMAIL_USER_ID = "email_user_id"
        private const val KEY_EMAIL_USER_EMAIL = "email_user_email"
        private const val KEY_EMAIL_USER_NAME = "email_user_name"
        private const val MIN_PASSWORD_LENGTH = 6
    }
}
