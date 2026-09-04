package pt.ipt.easynotes.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import pt.ipt.easynotes.MainActivity
import pt.ipt.easynotes.R
import pt.ipt.easynotes.databinding.FragmentAboutBinding

/**
 * Apresenta a informação académica do projeto e identifica as tecnologias,
 * bibliotecas e frameworks utilizados na aplicação e na API.
 */
class AboutFragment : Fragment() {

    private lateinit var binding: FragmentAboutBinding
    private lateinit var activity: MainActivity

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAboutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity = requireActivity() as MainActivity

        binding.textAndroidTitle.text = getString(R.string.technologies_android)
        binding.textApiTitle.text = getString(R.string.technologies_api)
        binding.textDatabaseTitle.text = getString(R.string.technologies_database)

        binding.textAndroidTechnologies.text = listOf(
            getString(R.string.technology_kotlin),
            getString(R.string.technology_xml_binding),
            getString(R.string.technology_material),
            getString(R.string.technology_fragments),
            getString(R.string.technology_recyclerview),
            getString(R.string.technology_internal_storage),
            getString(R.string.technology_shared_preferences),
            getString(R.string.technology_ktor_client),
            getString(R.string.technology_biometric)
        ).joinToString("\n\n")

        binding.textApiTechnologies.text = listOf(
            getString(R.string.technology_ktor_server),
            getString(R.string.technology_jwt),
            getString(R.string.technology_exposed),
            getString(R.string.technology_bcrypt),
            getString(R.string.technology_swagger),
            getString(R.string.technology_logback)
        ).joinToString("\n\n")

        binding.textDatabaseTechnologies.text = listOf(
            getString(R.string.technology_postgresql),
            getString(R.string.technology_h2)
        ).joinToString("\n\n")

        binding.buttonBack.setOnClickListener {
            if (activity.authViewModel.uiState.token == null) {
                activity.showLogin()
            } else {
                activity.showNotes()
            }
        }
    }
}
