package pt.ipt.easynotes.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.biometric.BiometricPrompt
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import pt.ipt.easynotes.MainActivity
import pt.ipt.easynotes.R
import pt.ipt.easynotes.databinding.FragmentLoginBinding

class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding
    private lateinit var activity: MainActivity
    private var sessionHandled = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity = requireActivity() as MainActivity

        binding.buttonLogin.setOnClickListener {
            val email = binding.editEmail.text.toString().trim()
            val password = binding.editPassword.text.toString()

            activity.authViewModel.login(email, password)
        }

        binding.buttonRegister.setOnClickListener {
            activity.authViewModel.clearError()
            activity.showRegister()
        }

        binding.buttonAbout.setOnClickListener {
            activity.showAbout()
        }

        observeAuthentication()
    }

    private fun observeAuthentication() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                activity.authViewModel.uiState.collect { state ->

                    binding.buttonLogin.isEnabled = !state.isLoading
                    binding.buttonRegister.isEnabled = !state.isLoading
                    binding.buttonLogin.text = if (state.isLoading) {
                        getString(R.string.logging_in)
                    } else {
                        getString(R.string.login)
                    }

                    if (state.errorMessage.isNullOrBlank()) {
                        binding.textError.visibility = View.GONE
                    } else {
                        binding.textError.text = state.errorMessage
                        binding.textError.visibility = View.VISIBLE
                    }

                    if (state.token != null && !sessionHandled) {
                        sessionHandled = true

                        if (state.restoredSession) {
                            unlockRestoredSession()
                        } else {
                            activity.showNotes()
                        }
                    }
                }
            }
        }
    }

    private fun unlockRestoredSession() {
        if (activity.biometricAuthenticator.canAuthenticate()) {
            val callback = object : BiometricPrompt.AuthenticationCallback() {

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    super.onAuthenticationSucceeded(result)
                    activity.showNotes()
                }

                override fun onAuthenticationError(
                    errorCode: Int,
                    errString: CharSequence
                ) {
                    super.onAuthenticationError(errorCode, errString)
                    sessionHandled = false
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    sessionHandled = false
                }
            }

            activity.biometricAuthenticator.authenticate(callback)
        } else {
            // Se o dispositivo não suportar biometria/PIN, mantém o login visível.
            sessionHandled = false
        }
    }
}
