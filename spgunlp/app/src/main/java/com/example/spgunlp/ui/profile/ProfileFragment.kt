package com.example.spgunlp.ui.profile

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.spgunlp.MainActivity
import com.example.spgunlp.R
import com.example.spgunlp.databinding.FragmentProfileBinding
import com.example.spgunlp.io.AuthService
import com.example.spgunlp.io.UserService
import com.example.spgunlp.model.Perfil
import com.example.spgunlp.ui.BaseFragment
import com.example.spgunlp.ui.login.LoginFragment
import com.example.spgunlp.util.PreferenceHelper
import com.example.spgunlp.util.PreferenceHelper.get
import com.example.spgunlp.util.PreferenceHelper.set
import com.example.spgunlp.util.performLogin
import com.example.spgunlp.util.performSync
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class ProfileFragment : BaseFragment() {
    private val userService: UserService by lazy {
        UserService.create()
    }

    private lateinit var mProfileViewModel: ProfileViewModel

    private var _binding: FragmentProfileBinding? = null

    private lateinit var jobToKill: Job
    private lateinit var preferences: SharedPreferences
    private lateinit var context:Context
    private lateinit var fragmentActivity: FragmentActivity

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val root: View = binding.root
        mProfileViewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)
        context = requireContext()
        fragmentActivity = requireActivity()
        preferences = PreferenceHelper.defaultPrefs(context)
        populateProfile()

        if (_binding!=null)
            binding.btnCerrarSesion.setOnClickListener {
                performLogout()
            }

        return root
    }

    private fun populateProfile() {
        val email = preferences["email", ""]
        mProfileViewModel.getPerfilByEmail(email).also {
            it.observe(viewLifecycleOwner) { perfil ->
                if (perfil != null) {
                    fillProfile(perfil)
                } else {
                    jobToKill = lifecycleScope.launch {
                        val jwt = preferences["jwt", ""]
                        if (!jwt.contains("."))
                            cancel()
                        val header = "Bearer $jwt"
                        val perfil = getProfile(header, email)
                        Log.i("ProfileFragment", "populateProfile: $perfil")
                        if (perfil != null) {
                            fillProfile(perfil)
                            mProfileViewModel.addPerfil(perfil)
                        }else{
                            if (_binding!=null)
                                binding.profileData.visibility = View.GONE
                        }
                    }
                }
            }
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        if (::jobToKill.isInitialized)
            jobToKill.cancel()
        _binding = null
    }

    private fun performLogout() {
        preferences["jwt"] = ""
        preferences["email"] = ""
        goToLoginFragment()
    }

    private fun goToLoginFragment() {
        val bottomNavigationView: BottomNavigationView =
            fragmentActivity.findViewById(R.id.nav_view)
        bottomNavigationView.selectedItemId = R.id.navigation_active
        val newFragment = LoginFragment()
        val transaction = parentFragmentManager.beginTransaction()
        transaction.replace(R.id.nav_host_fragment_activity_main, newFragment)
        transaction.commit()
    }
    private suspend fun getProfile(header: String, email: String): Perfil? {
        try {
            val response = userService.getUsers(header)
            val body = response.body()
            if (response.isSuccessful && body != null) {
                Log.i("ProfileFragment", "getProfile: made api call and was successful")
                val user = body.filter { it.email == email }[0]
                val nombre = user.nombre ?: "Sin Nombre"
                val posicion = user.posicionResponse?.nombre ?: "Sin Posición"
                val celular = user.celular ?: "Sin Celular"
                val organizacion = user.organizacion ?: "Sin Organización"
                val rol = user.roles?.get(0)?.nombre ?: ""
                return Perfil(
                    email,
                    nombre,
                    posicion,
                    celular,
                    organizacion,
                    rol
                )
            } else if (response.code() == 401) {
                makeLoginPopup()
                // TODO: rehacer sincronizar, con los datos
                binding.namePositionVeil.visibility = View.GONE
                return null
            }
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "No se pudo obtener el perfil, verificar la conexión a internet",
                Toast.LENGTH_SHORT
            ).show()
            binding.namePositionVeil.visibility = View.GONE
        }
        Log.i("ProfileFragment", "getProfile: made api call and was not successful")
        return null
    }

    private fun fillProfile(perfil: Perfil){
        if (_binding!=null){
            binding.profileName.text = perfil.nombre
            binding.profilePosition.text = perfil.posicion
            binding.profileEmail.text = perfil.email
            binding.profileCellphone.text = perfil.celular
            binding.profileOrganization.text = perfil.organizacion
            binding.profileRole.text = if (perfil.rol == "ROLE_ADMIN") "Administrador" else "Usuario"
            binding.profileData.visibility = View.VISIBLE
            binding.namePositionVeil.visibility = View.VISIBLE
            binding.profileVeil.unVeil()
            binding.namePositionVeil.unVeil()
        }
    }
    private fun makeLoginPopup(){

        val dialog = MaterialAlertDialogBuilder(context).create()
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.fragment_login, null)
        view.findViewById<TextView>(R.id.title_inicio).text =
            "Token expirado, inicie sesión nuevamente"
        view.findViewById<Button>(R.id.btn_crear_usuario).visibility = View.GONE
        val mail = view.findViewById<EditText>(R.id.edit_mail)
        mail.setText(preferences["email", ""])
        dialog.setView(view)
        val pwd = view.findViewById<EditText>(R.id.edit_password)

        preferences["COLOR_FAB"] = ContextCompat.getColor(context, R.color.red)
        lifecycleScope.launch {
            (activity as MainActivity).updateColorFab()
        }

        view.findViewById<Button>(R.id.btn_iniciar_sesion).setOnClickListener {
            lifecycleScope.launch {
                val authService = AuthService.create()
                if (
                    performLogin(
                        mail.text.toString(),
                        pwd.text.toString(),
                        context,
                        authService
                    ) && performSync(context)
                ) {
                    Toast.makeText(
                        context,
                        "Se ha sincronizado correctamente",
                        Toast.LENGTH_SHORT
                    ).show()
                    populateProfile()
                    preferences["COLOR_FAB"] = ContextCompat.getColor(context, R.color.green)
                    (activity as MainActivity).updateColorFab()
                    dialog.dismiss()
                } else {
                    Toast.makeText(
                        context,
                        "Inicio de sesion fallido",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
        dialog.show()
    }
}