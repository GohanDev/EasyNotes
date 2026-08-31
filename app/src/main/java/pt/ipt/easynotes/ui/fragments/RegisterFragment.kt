package pt.ipt.easynotes.ui.fragments

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import pt.ipt.easynotes.MainActivity
import pt.ipt.easynotes.R
import pt.ipt.easynotes.databinding.FragmentRegisterBinding

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

            // Validar nome
            if (name.isBlank()) {
                showError("O nome é obrigatório.")
                return@setOnClickListener
            }

            // Validar email
            if (email.isBlank()) {
                showError("O email é obrigatório.")
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                showError("Email inválido.")
                return@setOnClickListener
            }

            // Validar password
            if (password.isBlank()) {
                showError("A palavra-passe é obrigatória.")
                return@setOnClickListener
            }

            if (password.length < 6) {
                showError("A palavra-passe deve ter pelo menos 6 caracteres.")
                return@setOnClickListener
            }

            // Dados válidos - fazer registo
            activity.authViewModel.register(
                name = name,
                email = email,
                password = password
            )
        }

        binding.buttonBack.setOnClickListener {
            activity.authViewModel.clearError()
            activity.goBack()
        }

        observeRegistration()
    }

    private fun showError(message: String) {
        binding.textError.text = message
        binding.textError.visibility = View.VISIBLE
    }

    private fun observeRegistration() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                activity.authViewModel.uiState.collect { state ->

                    binding.buttonCreateAccount.isEnabled = !state.isLoading

                    binding.buttonCreateAccount.text = if (state.isLoading) {
                        getString(R.string.registering)
                    } else {
                        getString(R.string.create_account)
                    }

                    if (!state.errorMessage.isNullOrBlank()) {
                        binding.textError.text = state.errorMessage
                        binding.textError.visibility = View.VISIBLE
                    }

                    if (state.registrationSuccessful) {
                        activity.authViewModel.clearRegistrationSuccess()
                        activity.authViewModel.clearError()
                        activity.goBack()
                    }
                }
            }
        }
    }
}