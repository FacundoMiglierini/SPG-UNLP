package com.example.spgunlp.ui.visit

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.spgunlp.R
import com.example.spgunlp.databinding.FragmentParametersBinding
import com.example.spgunlp.io.VisitService
import com.example.spgunlp.model.AppVisit
import com.example.spgunlp.model.AppVisitParameters
import com.example.spgunlp.model.AppVisitUpdate
import com.example.spgunlp.ui.BaseFragment
import com.example.spgunlp.util.PreferenceHelper
import com.example.spgunlp.util.PreferenceHelper.get
import com.example.spgunlp.util.PreferenceHelper.set
import com.example.spgunlp.util.VisitChangesDBViewModel
import com.example.spgunlp.util.VisitsDBViewModel
import com.example.spgunlp.util.createVisit
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import retrofit2.Response

class ParametersFragment(): BaseFragment(), ParameterClickListener {

    private val visitService: VisitService by lazy {
        VisitService.create()
    }

    private val parameterViewModel: ParametersViewModel by activityViewModels()
    private val visitViewModel: VisitViewModel by activityViewModels()
    private val bundleViewModel: BundleViewModel by activityViewModels()

    private lateinit var visitUpdateViewModel: VisitChangesDBViewModel
    private lateinit var visitsDBViewModel: VisitsDBViewModel

    private var _binding: FragmentParametersBinding? = null

    private val binding get() = _binding!!
    private val parametersList = mutableListOf<AppVisitParameters>()

    private lateinit var context: Context
    private lateinit var fragmentActivity: FragmentActivity
    private lateinit var preferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentParametersBinding.inflate(inflater, container, false)
        context=requireContext()
        fragmentActivity = requireActivity()
        preferences = PreferenceHelper.defaultPrefs(context)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // init viewmodel
        visitUpdateViewModel = ViewModelProvider(this)[VisitChangesDBViewModel::class.java]
        visitsDBViewModel = ViewModelProvider(fragmentActivity)[VisitsDBViewModel::class.java]

        if (bundleViewModel.isParametersStateEmpty()) {
            parametersList.clear()
            populateParameters()
        }

        binding.btnSave.setOnClickListener {
            MaterialAlertDialogBuilder(context)
                .setTitle(binding.btnSave.text)
                .setMessage("¿Está seguro que desea guardar los cambios realizados?")
                .setNegativeButton("Cancelar") { _, _ ->
                }
                .setPositiveButton("Aceptar") { _, _ ->
                    updateParameterList()
                    updateVisitParameters()
                    parameterViewModel.setParametersCurrentPrinciple(emptyList())
                    bundleViewModel.clearPrinciplesState()
                    bundleViewModel.clearParametersList()
                }
                .show()
        }

        binding.btnCancel.setOnClickListener {
            MaterialAlertDialogBuilder(context)
                .setTitle(binding.btnCancel.text)
                .setMessage("¿Está seguro que desea volver atrás? Los cambios realizados no serán guardados")
                .setNegativeButton("Cancelar") { _, _ ->
                }
                .setPositiveButton("Aceptar") { _, _ ->
                    bundleViewModel.clearParametersList()
                    goToPrincipleFragment()
                }
                .show()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val adapter = binding.parametersList.adapter as ParametersAdapter
        val newParameters = parametersList.mapIndexed { index, par ->
            par.copy(
                cumple = adapter.getCheckedMap()[index] ?: false,
            )
        }
        bundleViewModel.saveParametersState(newParameters)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        if (savedInstanceState != null) {
            bundleViewModel.getParametersList()?.let { parametersList.addAll(it) }
            updateRecycler(parametersList)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun populateParameters() {
        parameterViewModel.parametersCurrentPrinciple.observe(viewLifecycleOwner) { value ->
            value?.forEach {
                if (it != null)
                    this.parametersList.add(it)
            }
            binding.detailTitle.text =
                parametersList[0].parametro?.principioAgroecologico?.nombre.toString()
            updateRecycler(parametersList)
        }
    }

    private fun updateRecycler(list: List<AppVisitParameters>) {
        binding.parametersList.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = ParametersAdapter(list, this@ParametersFragment)
        }
    }

    private fun updateParameterList() {

        val adapter = binding.parametersList.adapter as ParametersAdapter
        val newParameters = parametersList.mapIndexed { index, par ->
            par.copy(
                cumple = adapter.getCheckedMap()[index] ?: false,
            )
        }

        parametersList.clear()
        parametersList.addAll(newParameters)
    }

    private fun updateVisitParameters() {
        lifecycleScope.launch {
            val jwt = preferences["jwt", ""]
            if (!jwt.contains("."))
                cancel()
            val header = "Bearer $jwt"

            try {
                val response = getVisitParametersUpdated(header)
                val body = response.body()

                if (response.isSuccessful && body != null) {
                    Toast.makeText(
                        context,
                        "Los cambios han sido guardados con éxito",
                        Toast.LENGTH_SHORT
                    ).show()
                    (activity as VisitActivity).updateVisit(body)
                    visitsDBViewModel.updateVisit(body)
                    preferences["COLOR_FAB"] =
                        ContextCompat.getColor(context, R.color.green)
                } else {
                    Toast.makeText(
                        context,
                        "Los cambios fueron guardados pero no se pudo sincronizar con el servidor",
                        Toast.LENGTH_SHORT
                    ).show()
                    saveUserChanges()
                    preferences["COLOR_FAB"] = ContextCompat.getColor(context, R.color.red)
                }
            } catch (e: Exception) {
                Toast.makeText(
                    context,
                    "Los cambios fueron guardados pero no se pudo sincronizar con el servidor",
                    Toast.LENGTH_SHORT
                ).show()
                saveUserChanges()
                preferences["COLOR_FAB"] = ContextCompat.getColor(context, R.color.yellow)
            }

            goToPrincipleFragment()
        }
    }

    private suspend fun getVisitParametersUpdated(header: String): Response<AppVisit> {
        val visitId = visitViewModel.visit.value?.id ?: 0
        val visitToUpdate = getAppVisitUpdate()

        return visitService.updateVisitById(header, visitId, visitToUpdate)
    }

    private fun getAppVisitUpdate(): AppVisitUpdate {

        val parametersUpdate = mutableListOf<AppVisitUpdate.ParametersUpdate>()

        parametersList.forEach {
            parametersUpdate.add(
                AppVisitUpdate.ParametersUpdate(
                    it.aspiracionesFamiliares,
                    it.comentarios,
                    it.cumple,
                    it.parametro?.id,
                    it.sugerencias
                )
            )
        }

        val idsLoaded = parametersList.map { it.id }

        parameterViewModel.parameters.observe(viewLifecycleOwner) { value ->
            value?.forEach {
                if (it != null && !idsLoaded.contains(it.id)) {
                    parametersUpdate.add(
                        AppVisitUpdate.ParametersUpdate(
                            it.aspiracionesFamiliares,
                            it.comentarios,
                            it.cumple,
                            it.parametro?.id,
                            it.sugerencias
                        )
                    )
                }
            }
        }

        val idMembers = visitViewModel.visit.value?.integrantes?.map {
            it.id ?: 0
        }

        return AppVisitUpdate(
            visitViewModel.visit.value?.fechaVisita,
            idMembers,
            parametersUpdate,
            visitViewModel.visit.value?.quintaResponse?.id
        )
    }

    private fun goToPrincipleFragment() {
        bundleViewModel.clearPrinciplesState()

        fragmentActivity.supportFragmentManager.popBackStack()
    }

    override fun onClick(parameter: AppVisitParameters) {

    }

    private fun saveUserChanges() {
        val visitToUpdate = getAppVisitUpdate()
        val email: String = preferences["email"]

        val visit = visitViewModel.visit.value
        if (visit != null) {
            try {
                val newVisit = createVisit(visit, visitToUpdate)
                (activity as VisitActivity).updateVisit(newVisit)
                visit.id?.let { visitUpdateViewModel.addVisit(visitToUpdate, email, it) }
                Log.i("SPGUNLP_DB", "stores changes on DB")
            } catch (e: Exception) {
                Log.e("SPGUNLP_DB", e.message.toString())
            }
        }
    }
}

