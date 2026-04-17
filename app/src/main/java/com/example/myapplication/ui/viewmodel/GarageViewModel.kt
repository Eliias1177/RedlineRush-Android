package com.example.myapplication.ui.viewmodel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.api.RetrofitClient
import com.example.myapplication.data.model.Player
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GarageViewModel : ViewModel() {

    // Estado de la UI: Puede ser null mientras carga, o tener el Player cuando responde la API
    private val _playerState = MutableStateFlow<Player?>(null)
    val playerState: StateFlow<Player?> = _playerState.asStateFlow()

    init {
        fetchGarageData()
    }

    private fun fetchGarageData() {
        viewModelScope.launch {
            try {
                // Hacemos la llamada al backend pidiendo el jugador con ID 1
                val player = RetrofitClient.apiService.getPlayerGarage(1)
                _playerState.value = player
            } catch (e: Exception) {
                // En un proyecto real, aquí manejaríamos el error (mostrar un Toast, etc.)
                e.printStackTrace()
            }
        }
    }
}