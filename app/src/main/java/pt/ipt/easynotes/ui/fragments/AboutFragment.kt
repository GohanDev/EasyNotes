package pt.ipt.easynotes.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import pt.ipt.easynotes.MainActivity
import pt.ipt.easynotes.R
import pt.ipt.easynotes.databinding.FragmentAboutBinding

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

        // Títulos das categorias
        binding.textAndroidTitle.text = getString(R.string.technologies_android)
        binding.textApiTitle.text = getString(R.string.technologies_api)
        binding.textDatabaseTitle.text = getString(R.string.technologies_database)

        // Tecnologias utilizadas na aplicação Android
        binding.textAndroidTechnologies.text = listOf(
            getString(R.string.technology_kotlin),
            getString(R.string.technology_xml_binding),
            getString(R.string.technology_material),
            getString(R.string.technology_fragments),
            getString(R.string.technology_recyclerview),
            getString(R.string.technology_room),
            getString(R.string.technology_ktor_client),
            getString(R.string.technology_datastore),
            getString(R.string.technology_biometric),
            getString(R.string.technology_workmanager)
        ).joinToString("\n\n")

        // Tecnologias utilizadas na API REST
        binding.textApiTechnologies.text = listOf(
            getString(R.string.technology_ktor_server),
            getString(R.string.technology_jwt),
            getString(R.string.technology_exposed),
            getString(R.string.technology_bcrypt),
            getString(R.string.technology_swagger),
            getString(R.string.technology_logback)
        ).joinToString("\n\n")

        // Bases de dados utilizadas
        binding.textDatabaseTechnologies.text = listOf(
            getString(R.string.technology_postgresql),
            getString(R.string.technology_h2)
        ).joinToString("\n\n")

        binding.buttonBack.setOnClickListener {
            activity.showLogin()
        }
    }
}