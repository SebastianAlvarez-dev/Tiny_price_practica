package com.example.practica_inetgracion.presentation.deals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.practica_inetgracion.data.auth.AuthException
import com.example.practica_inetgracion.data.auth.SupabaseAuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val isRegistering: Boolean = false,
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val message: String? = null,
)

class AuthViewModel(private val repository: SupabaseAuthRepository) : ViewModel() {
    private val _state = MutableStateFlow(AuthUiState())
    val state = _state.asStateFlow()

    fun setEmail(value: String) { _state.value = _state.value.copy(email = value, message = null) }
    fun setPassword(value: String) { _state.value = _state.value.copy(password = value, message = null) }
    fun toggleMode() { _state.value = _state.value.copy(isRegistering = !_state.value.isRegistering, message = null) }

    fun submit() {
        val current = _state.value
        if (current.email.isBlank() || current.password.length < 6) {
            _state.value = current.copy(message = "Ingresa un correo y una contraseña de al menos 6 caracteres.")
            return
        }
        viewModelScope.launch {
            _state.value = current.copy(isLoading = true, message = null)
            try {
                if (current.isRegistering) {
                    val result = repository.signUp(current.email, current.password)
                    _state.value = current.copy(isLoading = false, isAuthenticated = result.session != null, message = result.message)
                } else {
                    repository.signIn(current.email, current.password)
                    _state.value = current.copy(isLoading = false, isAuthenticated = true)
                }
            } catch (exception: AuthException) {
                _state.value = current.copy(isLoading = false, message = exception.message)
            } catch (_: Exception) {
                _state.value = current.copy(isLoading = false, message = "No se pudo conectar con Supabase. Inténtalo de nuevo.")
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            repository.signOut()
            _state.value = _state.value.copy(isAuthenticated = false, password = "", message = null)
        }
    }

    companion object {
        fun factory(repository: SupabaseAuthRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = AuthViewModel(repository) as T
        }
    }
}
