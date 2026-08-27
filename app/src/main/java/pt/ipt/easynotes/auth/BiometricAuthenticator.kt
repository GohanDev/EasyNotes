package pt.ipt.easynotes.auth

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

class BiometricAuthenticator(
    private val activity: FragmentActivity
) {

    fun canAuthenticate(): Boolean {

        val biometricManager =
            BiometricManager.from(activity)

        val authenticators =
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL

        return biometricManager.canAuthenticate(
            authenticators
        ) == BiometricManager.BIOMETRIC_SUCCESS
    }

    fun authenticate(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {

        val executor =
            ContextCompat.getMainExecutor(activity)

        val biometricPrompt =
            BiometricPrompt(
                activity,
                executor,
                object : BiometricPrompt.AuthenticationCallback() {

                    override fun onAuthenticationSucceeded(
                        result: BiometricPrompt.AuthenticationResult
                    ) {
                        super.onAuthenticationSucceeded(result)

                        onSuccess()
                    }

                    override fun onAuthenticationError(
                        errorCode: Int,
                        errString: CharSequence
                    ) {
                        super.onAuthenticationError(
                            errorCode,
                            errString
                        )

                        onError(
                            errString.toString()
                        )
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()

                        onError(
                            "Autenticação não reconhecida."
                        )
                    }
                }
            )

        val authenticators =
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL

        val promptInfo =
            BiometricPrompt.PromptInfo.Builder()
                .setTitle("Desbloquear EasyNotes")
                .setSubtitle(
                    "Use biometria ou o código do dispositivo."
                )
                .setAllowedAuthenticators(
                    authenticators
                )
                .build()

        biometricPrompt.authenticate(
            promptInfo
        )
    }
}