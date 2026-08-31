package pt.ipt.easynotes.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pt.ipt.easynotes.data.AuthRepository
import pt.ipt.easynotes.network.UserResponse
import kotlinx.coroutines.flow.first
import pt.ipt.easynotes.network.InvalidSessionException
import pt.ipt.easynotes.network.ApiException
import pt.ipt.easynotes.network.InvalidCredentialsException
import pt.ipt.easynotes.network.EmailAlreadyExistsException

data class AuthUiState(
    val isLoading: Boolean = false,
    val token: String? = null,
    val user: UserResponse? = null,
    val errorMessage: String? = null,
    val registrationSuccessful: Boolean = false,
    val restoredSession: Boolean = false
)

class AuthViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        AuthUiState()
    )

    val uiState: StateFlow<AuthUiState> =
        _uiState.asStateFlow()

    fun login(
        email: String,
        password: String
    ) {

        if (email.isBlank() || password.isBlank()) {
            _uiState.value = AuthUiState(
                errorMessage = "Email e password são obrigatórios."
            )
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.value = AuthUiState(
                errorMessage = "Introduza um email válido."
            )
            return
        }

        viewModelScope.launch {

            _uiState.value = AuthUiState(
                isLoading = true
            )

            try {

                val response = repository.login(
                    email = email.trim(),
                    password = password
                )

                _uiState.value = AuthUiState(
                    token = response.token,
                    user = response.user
                )

            } catch (e: InvalidCredentialsException) {

                _uiState.value = AuthUiState(
                    errorMessage = "Email ou password incorretos."
                )

            } catch (e: ApiException) {
                _uiState.value = AuthUiState(
                    errorMessage = e.message ?: "Ocorreu um erro no servidor."
                )


            } catch (e: Exception) {

                _uiState.value = AuthUiState(
                    errorMessage = "Não foi possível contactar o servidor."
                )
            }
        }
    }

    fun register(
        name: String,
        email: String,
        password: String
    ) {

        if (
            name.isBlank() ||
            email.isBlank() ||
            password.isBlank()
        ) {
            _uiState.value = AuthUiState(
                errorMessage = "Todos os campos são obrigatórios."
            )
            return
        }

        viewModelScope.launch {

            _uiState.value = AuthUiState(
                isLoading = true
            )

            try {

                repository.register(
                    name = name.trim(),
                    email = email.trim(),
                    password = password
                )

                _uiState.value = AuthUiState(
                    registrationSuccessful = true
                )

            } catch (e: EmailAlreadyExistsException) {

                _uiState.value = AuthUiState(
                    errorMessage = "Já existe uma conta com este email."
                )

            } catch (e: ApiException) {
                _uiState.value = AuthUiState(
                    errorMessage = e.message ?: "Ocorreu um erro no servidor."
                )

            } catch (e: Exception) {

                _uiState.value = AuthUiState(
                    errorMessage = "Não foi possível contactar o servidor."
                )
            }
        }
    }

    fun clearRegistrationSuccess() {
        _uiState.value = _uiState.value.copy(
            registrationSuccessful = false
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null
        )
    }

    fun restoreSession() {

        viewModelScope.launch {

            val session = repository
                .getSession()
                .first()

            if (session == null) {
                _uiState.value = AuthUiState()
                return@launch
            }

            try {

                val user = repository.validateSession(
                    token = session.token
                )

                // A API respondeu e confirmou que o JWT é válido.
                _uiState.value = AuthUiState(
                    token = session.token,
                    user = user,
                    restoredSession = true
                )

            } catch (e: InvalidSessionException) {

                // A API respondeu 401.
                // O JWT já não é válido, portanto apagamos a sessão.
                repository.logout()

                _uiState.value = AuthUiState(
                    errorMessage = "A sessão expirou. Inicie sessão novamente."
                )

            } catch (e: ApiException) {

                // A API respondeu, mas ocorreu outro erro HTTP.
                // Não apagamos a sessão guardada.
                _uiState.value = AuthUiState(
                    errorMessage = "Não foi possível validar a sessão."
                )

            } catch (e: Exception) {
                _uiState.value = AuthUiState(
                    token = session.token,
                    user = UserResponse(
                        id = session.userId,
                        name = session.name,
                        email = session.email
                    ),
                    errorMessage = "Modo offline",
                    restoredSession = true
                )
            }
        }
    }

    fun logout() {

        // Limpa imediatamente o estado visível na interface.
        _uiState.value = AuthUiState()

        viewModelScope.launch {
            repository.logout()
        }
    }
}