package com.example.myapplication.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.api.RetrofitClient
import com.example.myapplication.data.model.AuthRequest
import com.example.myapplication.data.model.Player
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val _loggedInPlayer = MutableStateFlow<Player?>(null)
    val loggedInPlayer: StateFlow<Player?> = _loggedInPlayer.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun login(username: String, pass: String) {
        if (username.isBlank() || pass.isBlank()) {
            _errorMessage.value = "Por favor, ingresa tu usuario y contraseña."
            return
        }
        viewModelScope.launch {
            try {
                _errorMessage.value = null
                val request = AuthRequest(username, pass)
                val player = RetrofitClient.apiService.login(request)
                _loggedInPlayer.value = player
            } catch (e: Exception) {
                _errorMessage.value = "Datos incorrectos. Verifica tu usuario o contraseña."
            }
        }
    }

    fun register(username: String, pass: String) {
        if (username.isBlank() || pass.isBlank()) {
            _errorMessage.value = "No puedes dejar los campos vacíos."
            return
        }
        if (pass.length < 3) {
            _errorMessage.value = "La contraseña debe tener al menos 3 caracteres."
            return
        }
        viewModelScope.launch {
            try {
                _errorMessage.value = null
                val request = AuthRequest(username, pass)
                val player = RetrofitClient.apiService.register(request)
                _loggedInPlayer.value = player
            } catch (e: Exception) {
                _errorMessage.value = "El usuario ya existe. Intenta con otro nombre."
            }
        }
    }

    // NUEVO: Para actualizar el dinero después de la carrera
    fun updatePlayer(updatedPlayer: Player) {
        _loggedInPlayer.value = updatedPlayer
    }
}