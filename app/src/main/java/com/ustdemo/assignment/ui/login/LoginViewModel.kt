package com.ustdemo.assignment.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseUser
import com.ustdemo.assignment.data.repository.AuthRepository
import com.ustdemo.assignment.util.NetworkUtils
import com.ustdemo.assignment.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val networkUtils: NetworkUtils
) : ViewModel() {

    private val _loginState = MutableLiveData<LoginState>()
    val loginState: LiveData<LoginState> = _loginState

    private val _silentAuthState = MutableLiveData<SilentAuthState>()
    val silentAuthState: LiveData<SilentAuthState> = _silentAuthState

    init {
        checkSilentAuthentication()
    }

    private fun checkSilentAuthentication() {
        if (authRepository.isLoggedIn()) {
            viewModelScope.launch {
                _silentAuthState.value = SilentAuthState.Loading

                if (!networkUtils.isNetworkAvailable()) {
                    // Force logout if network is not available during silent auth
                    authRepository.forceLogout()
                    _silentAuthState.value = SilentAuthState.ForceLogout
                    return@launch
                }

                when (val result = authRepository.silentSignIn()) {
                    is Resource.Success -> {
                        _silentAuthState.value = SilentAuthState.Success(result.data!!)
                    }
                    is Resource.Error -> {
                        // If silent auth fails, show login screen
                        _silentAuthState.value = SilentAuthState.RequiresLogin
                    }
                    is Resource.Loading -> {
                        _silentAuthState.value = SilentAuthState.Loading
                    }
                }
            }
        } else {
            _silentAuthState.value = SilentAuthState.RequiresLogin
        }
    }

    fun signInWithGoogle(account: GoogleSignInAccount) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading

            when (val result = authRepository.signInWithGoogle(account)) {
                is Resource.Success -> {
                    _loginState.value = LoginState.Success(result.data!!)
                }
                is Resource.Error -> {
                    _loginState.value = LoginState.Error(result.message ?: "Authentication failed")
                }
                is Resource.Loading -> {
                    _loginState.value = LoginState.Loading
                }
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
            _loginState.value = LoginState.SignedOut
        }
    }

    sealed class LoginState {
        object Idle : LoginState()
        object Loading : LoginState()
        data class Success(val user: FirebaseUser) : LoginState()
        data class Error(val message: String) : LoginState()
        object SignedOut : LoginState()
    }

    sealed class SilentAuthState {
        object Loading : SilentAuthState()
        data class Success(val user: FirebaseUser) : SilentAuthState()
        object RequiresLogin : SilentAuthState()
        object ForceLogout : SilentAuthState()
    }
}
