package com.panelpass.auth

import android.content.Context
import com.panelpass.domain.auth.AuthRepository
import com.panelpass.domain.auth.User
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

/**
 * Android implementation of [AuthRepository] using Google Sign In.
 */
internal class GoogleAuthRepository(
    private val context: Context,
) : AuthRepository {

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
        val activity = ActivityHolder.get()
            ?: return@withContext Result.failure(IllegalStateException("No Activity registered. Ensure ActivityHolder.set(activity) is called in onResume."))
        activity.startActivityForResult(signInClient.signInIntent, REQUEST_CODE_SIGN_IN)
        signInResultChannel.receive()
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

    override suspend fun signOut() = withContext(Dispatchers.Main) {
        signInClient.signOut()
    }

    override suspend fun getCurrentUser(): User? = withContext(Dispatchers.Main) {
        GoogleSignIn.getLastSignedInAccount(context)?.toUser()
    }

    override fun isSignInAvailable(): Boolean = true

    private fun getWebClientId(): String {
        return try {
            context.getString(R.string.default_web_client_id)
        } catch (e: Exception) {
            ""
        }
    }

    private fun GoogleSignInAccount.toUser(): User = User(
        id = id ?: "",
        email = email,
        name = displayName,
    )

    companion object {
        const val REQUEST_CODE_SIGN_IN = 9001
    }
}
