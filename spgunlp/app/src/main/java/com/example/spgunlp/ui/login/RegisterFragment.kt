package com.example.spgunlp.ui.login

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.spgunlp.R
import com.example.spgunlp.databinding.FragmentRegisterBinding
import com.example.spgunlp.io.AuthService
import com.example.spgunlp.io.response.AuthErrorResponse
import com.example.spgunlp.model.AppUser
import com.example.spgunlp.ui.BaseFragment
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class RegisterFragment : BaseFragment() {

    private val mapItemInteger = mapOf(
        "Consumidor" to 1,
        "Equipo tecnico" to 2,
        "Productor/a" to 3,
        "Representante de organizacion" to 4
    )

    private val authService: AuthService by lazy {
        AuthService.create()
    }

    override var bottomNavigationViewVisibility = View.GONE
    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: RegisterViewModel

    private lateinit var jobToKill: Job

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val registerViewModel =
            ViewModelProvider(this)[RegisterViewModel::class.java]

        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        val root: View = binding.root


        val items = mapItemInteger.keys.toList()
        val adapter = ArrayAdapter(requireContext(), R.layout.list_item, items)
        binding.autoCompleteTextView.setAdapter(adapter)


        binding.btnInicioSesion.setOnClickListener {
            goToLoginFragment()
        }

        binding.btnRegistrar.setOnClickListener {
            performRegister()
        }

        binding.autoCompleteTextView.setOnClickListener {
            val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(it.windowToken, 0)
        }

        return root
    }

    private fun goToLoginFragment() {
        val transaction = parentFragmentManager.beginTransaction()
        val newFragment = LoginFragment()
        transaction.add(R.id.nav_host_fragment_activity_main, newFragment)
        transaction.remove(this)
        transaction.commit()
    }

    private fun performRegister() {
        val editMail = binding.editMail.text.toString()
        val editPassword = binding.editPassword.text.toString()
        val editName = binding.editName.text.toString()
        val editCellphone = binding.editCellphone.text.toString()
        val editOrganization = binding.editOrganization.text.toString()
        var position = mapItemInteger[binding.autoCompleteTextView.text.toString()]
        if (position == null) {
            position = 0
        }

        // make the call to the remote API with coroutines
        jobToKill = lifecycleScope.launch {
            val user = AppUser(
                editMail,
                editPassword,
                editCellphone,
                editName,
                editOrganization,
                position
            )
            try {
                val response = authService.registro(user)

                if (response.isSuccessful) {
                    Toast.makeText(
                        context,
                        "Se ha creado el usuario correctamente, se encuentra a la espera de autorizacion",
                        Toast.LENGTH_SHORT
                    ).show()
                    goToLoginFragment()
                } else {
                    val errorBody = response.errorBody()!!.string()
                    val gson = Gson()
                    val type = object : TypeToken<AuthErrorResponse>() {}.type
                    val errorResponse: AuthErrorResponse? = gson.fromJson(errorBody, type)
                    if (errorResponse != null) {
                        Toast.makeText(context, errorResponse.detalleError, Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error de conexión", Toast.LENGTH_SHORT).show()
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (::jobToKill.isInitialized)
            jobToKill.cancel()
        _binding = null
    }
}