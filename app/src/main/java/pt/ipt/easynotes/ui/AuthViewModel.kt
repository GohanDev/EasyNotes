package pt.ipt.easynotes.ui

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pt.ipt.easynotes.data.AuthRepository
import pt.ipt.easynotes.network.ApiException
import pt.ipt.easynotes.network.EmailAlreadyExistsException
import pt.ipt.easynotes.network.InvalidCredentialsException
import pt.ipt.easynotes.network.InvalidSessionException
import pt.ipt.easynotes.network.UserResponse

/**
 * Representa o estado atual da autenticação utilizado pelos ecrãs de login
 * e registo.
 */
data class AuthUiState(
    val isLoading: Boolean = false,
    val token: String? = null,
    val user: UserResponse? = null,
    val errorMessage: String? = null,
    val registrationSuccessful: Boolean = false,
    val restoredSession: Boolean = false
)

/**
 * ViewModel responsável pela autenticação e pela recuperação da sessão.
 *
 * As operações que comunicam com a API são executadas em coroutines. A resposta
 * é devolvida ao Fragment através de callbacks simples.
 */
class AuthViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    var uiState = AuthUiState()
        private set

    /**
     * Valida os dados e tenta autenticar o utilizador através da API.
     */
    fun login(
        email: String,
        password: String,
        onStateChanged: (AuthUiState) -> Unit
    ) {
        if (email.isBlank() || password.isBlank()) {
            updateState(
                AuthUiState(errorMessage = "Email e password são obrigatórios."),
                onStateChanged
            )
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            updateState(
                AuthUiState(errorMessage = "Introduza um email válido."),
                onStateChanged
            )
            return
        }

        updateState(AuthUiState(isLoading = true), onStateChanged)

        viewModelScope.launch {
            try {
                val response = repository.login(
                    email = email.trim(),
                    password = password
                )

                updateState(
                    AuthUiState(
                        token = response.token,
                        user = response.user
                    ),
                    onStateChanged
                )
            } catch (e: InvalidCredentialsException) {
                updateState(
                    AuthUiState(errorMessage = "Email ou password incorretos."),
                    onStateChanged
                )
            } catch (e: ApiException) {
                updateState(
                    AuthUiState(
                        errorMessage = e.message ?: "Ocorreu um erro no servidor."
                    ),
                    onStateChanged
                )
            } catch (e: Exception) {
                updateState(
                    AuthUiState(errorMessage = "Não foi possível contactar o servidor."),
                    onStateChanged
                )
            }
        }
    }

    /**
     * Valida os campos e envia o pedido de criação de conta para a API.
     */
    fun register(
        name: String,
        email: String,
        password: String,
        onStateChanged: (AuthUiState) -> Unit
    ) {
        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            updateState(
                AuthUiState(errorMessage = "Todos os campos são obrigatórios."),
                onStateChanged
            )
            return
        }

        updateState(AuthUiState(isLoading = true), onStateChanged)

        viewModelScope.launch {
            try {
                repository.register(
                    name = name.trim(),
                    email = email.trim(),
                    password = password
                )

                updateState(
                    AuthUiState(registrationSuccessful = true),
                    onStateChanged
                )
            } catch (e: EmailAlreadyExistsException) {
                updateState(
                    AuthUiState(errorMessage = "Já existe uma conta com este email."),
                    onStateChanged
                )
            } catch (e: ApiException) {
                updateState(
                    AuthUiState(
                        errorMessage = e.message ?: "Ocorreu um erro no servidor."
                    ),
                    onStateChanged
                )
            } catch (e: Exception) {
                updateState(
                    AuthUiState(errorMessage = "Não foi possível contactar o servidor."),
                    onStateChanged
                )
            }
        }
    }

    /**
     * Recupera a sessão guardada e tenta validar o respetivo token JWT na API.
     */
    fun restoreSession(onStateChanged: (AuthUiState) -> Unit) {
        val session = repository.getSession()

        if (session == null) {
            updateState(AuthUiState(), onStateChanged)
            return
        }

        viewModelScope.launch {
            try {
                val user = repository.validateSession(session.token)

                updateState(
                    AuthUiState(
                        token = session.token,
                        user = user,
                        restoredSession = true
                    ),
                    onStateChanged
                )
            } catch (e: InvalidSessionException) {
                repository.logout()

                updateState(
                    AuthUiState(
                        errorMessage = "A sessão expirou. Inicie sessão novamente."
                    ),
                    onStateChanged
                )
            } catch (e: ApiException) {
                updateState(
                    AuthUiState(
                        errorMessage = "Não foi possível validar a sessão."
                    ),
                    onStateChanged
                )
            } catch (e: Exception) {
                /*
                 * Sem comunicação com a API, usa os dados da sessão guardada
                 * para permitir o acesso às notas que existem localmente.
                 */
                updateState(
                    AuthUiState(
                        token = session.token,
                        user = UserResponse(
                            id = session.userId,
                            name = session.name,
                            email = session.email
                        ),
                        errorMessage = "Modo offline",
                        restoredSession = true
                    ),
                    onStateChanged
                )
            }
        }
    }

    // Remove mensagens de erro antes de mudar de ecrã.
    fun clearError() {
        uiState = uiState.copy(errorMessage = null)
    }

    /**
     * Termina a sessão local e limpa o estado da autenticação.
     */
    fun logout() {
        repository.logout()
        uiState = AuthUiState()
    }

    private fun updateState(
        newState: AuthUiState,
        onStateChanged: (AuthUiState) -> Unit
    ) {
        uiState = newState
        onStateChanged(uiState)
    }
}
