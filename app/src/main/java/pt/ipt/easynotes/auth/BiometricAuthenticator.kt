package pt.ipt.easynotes.auth

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import pt.ipt.easynotes.R

/**
 * Encapsula a autenticação biométrica ou por credencial do dispositivo.
 */
class BiometricAuthenticator(
    private val activity: FragmentActivity
) {

    fun canAuthenticate(): Boolean {
        val biometricManager = BiometricManager.from(activity)

        val authenticators =
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
                BiometricManager.Authenticators.DEVICE_CREDENTIAL

        return biometricManager.canAuthenticate(authenticators) ==
            BiometricManager.BIOMETRIC_SUCCESS
    }

    fun authenticate(callback: BiometricPrompt.AuthenticationCallback) {
        val executor = ContextCompat.getMainExecutor(activity)

        val biometricPrompt = BiometricPrompt(
            activity,
            executor,
            callback
        )

        val authenticators =
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
                BiometricManager.Authenticators.DEVICE_CREDENTIAL

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(activity.getString(R.string.biometric_title))
            .setSubtitle(activity.getString(R.string.biometric_subtitle))
            .setAllowedAuthenticators(authenticators)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}
