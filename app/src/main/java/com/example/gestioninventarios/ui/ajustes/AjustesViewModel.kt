package com.example.gestioninventarios.ui.ajustes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestioninventarios.data.local.PreferenciasManager
import com.example.gestioninventarios.data.local.TokenStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class AjustesUiState(
    val modoTema: String = PreferenciasManager.TEMA_AUTOMATICO,
    val isAdmin: Boolean = false
)

class AjustesViewModel(
    private val preferenciasManager: PreferenciasManager,
    private val tokenStore: TokenStore
) : ViewModel() {

    val uiState: StateFlow<AjustesUiState> = combine(
        preferenciasManager.modoTema,
        tokenStore.userRole
    ) { modoTema, role ->
        AjustesUiState(
            modoTema = modoTema,
            isAdmin = (role == "ADMIN")
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AjustesUiState(
            modoTema = preferenciasManager.getModoTema()
        )
    )

    fun seleccionarModoTema(modo: String) {
        preferenciasManager.setModoTema(modo)
    }
}
