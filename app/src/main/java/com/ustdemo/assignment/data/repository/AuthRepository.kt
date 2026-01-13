package com.ustdemo.assignment.data.repository

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.ustdemo.assignment.util.NetworkUtils
import com.ustdemo.assignment.util.PreferencesManager
import com.ustdemo.assignment.util.Resource
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val googleSignInClient: GoogleSignInClient,
    private val preferencesManager: PreferencesManager,
    private val networkUtils: NetworkUtils
) {

    val currentUser: FirebaseUser?
        get() = firebaseAuth.currentUser

    fun isLoggedIn(): Boolean {
        return preferencesManager.isLoggedIn() && currentUser != null
    }

    suspend fun signInWithGoogle(account: GoogleSignInAccount): Resource<FirebaseUser> {
        return try {
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            val authResult = firebaseAuth.signInWithCredential(credential).await()
            val user = authResult.user

            if (user != null) {
                // Cache the token
                val token = user.getIdToken(false).await()
                token?.token?.let { preferencesManager.saveAuthToken(it) }
                preferencesManager.saveUserInfo(user.email, user.displayName)
                preferencesManager.setLoggedIn(true)
                Resource.Success(user)
            } else {
                Resource.Error("Authentication failed")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Authentication failed")
        }
    }

    suspend fun silentSignIn(): Resource<FirebaseUser> {
        // Check network availability first
        if (!networkUtils.isNetworkAvailable()) {
            return Resource.Error("Network not available")
        }

        return try {
            // Try to get cached token
            val cachedToken = preferencesManager.getAuthToken()
            val user = currentUser

            if (user != null && cachedToken != null) {
                // Refresh the token
                val tokenResult = user.getIdToken(true).await()
                tokenResult?.token?.let { preferencesManager.saveAuthToken(it) }
                Resource.Success(user)
            } else {
                // Try Google silent sign-in
                val account = googleSignInClient.silentSignIn().await()
                signInWithGoogle(account)
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Silent sign-in failed")
        }
    }

    suspend fun signOut() {
        firebaseAuth.signOut()
        googleSignInClient.signOut().await()
        preferencesManager.clearAll()
    }

    fun forceLogout() {
        firebaseAuth.signOut()
        preferencesManager.clearAll()
    }
}
