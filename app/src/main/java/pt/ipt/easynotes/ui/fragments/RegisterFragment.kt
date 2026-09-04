package pt.ipt.easynotes.ui.fragments

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import pt.ipt.easynotes.MainActivity
import pt.ipt.easynotes.R
import pt.ipt.easynotes.databinding.FragmentRegisterBinding
import pt.ipt.easynotes.ui.AuthUiState

/**
 * Fragment responsável pela criação de novas contas de utilizador.
 */
class RegisterFragment : Fragment() {

    private lateinit var binding: FragmentRegisterBinding
    private lateinit var activity: MainActivity

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity = requireActivity() as MainActivity

        binding.buttonCreateAccount.setOnClickListener {
            val name = binding.editName.text.toString().trim()
            val email = binding.editEmail.text.toString().trim()
            val password = binding.editPassword.text.toString()

            if (!validateFields(name, email, password)) {
                return@setOnClickListener
            }

            activity.authViewModel.register(
                name = name,
                email = email,
                password = password
            ) { state ->
                handleRegistrationState(state)
            }
        }

        binding.buttonBack.setOnClickListener {
            activity.authViewModel.clearError()
            activity.goBack()
        }
    }

    /**
     * Valida os dados antes de os enviar para a API.
     */
    private fun validateFields(
        name: String,
        email: String,
        password: String
    ): Boolean {
        if (name.isBlank()) {
            showError(getString(R.string.name_required))
            return false
        }

        if (email.isBlank()) {
            showError(getString(R.string.email_required))
            return false
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError(getString(R.string.invalid_email))
            return false
        }

        if (password.isBlank()) {
            showError(getString(R.string.password_required))
            return false
        }

        if (password.length < 6) {
            showError(getString(R.string.password_min_length))
            return false
        }

        return true
    }

    private fun showError(message: String) {
        binding.textError.text = message
        binding.textError.visibility = View.VISIBLE
    }

    /**
     * Atualiza o ecrã com o resultado devolvido pelo ViewModel.
     */
    private fun handleRegistrationState(state: AuthUiState) {
        binding.buttonCreateAccount.isEnabled = !state.isLoading

        binding.buttonCreateAccount.text = if (state.isLoading) {
            getString(R.string.registering)
        } else {
            getString(R.string.create_account)
        }

        if (state.errorMessage.isNullOrBlank()) {
            binding.textError.visibility = View.GONE
        } else {
            showError(state.errorMessage)
        }

        if (state.registrationSuccessful) {
            activity.authViewModel.clearError()
            activity.goBack()
        }
    }
}
