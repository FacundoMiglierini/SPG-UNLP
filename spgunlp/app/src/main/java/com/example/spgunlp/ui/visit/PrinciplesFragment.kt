package com.example.spgunlp.ui.visit

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.spgunlp.databinding.FragmentPrinciplesBinding
import com.example.spgunlp.io.VisitService
import com.example.spgunlp.model.AppVisitParameters
import com.example.spgunlp.ui.BaseFragment
import com.example.spgunlp.util.PreferenceHelper
import com.example.spgunlp.util.PreferenceHelper.get
import com.example.spgunlp.util.getPrinciples
import kotlinx.coroutines.launch

class PrinciplesFragment : BaseFragment(), PrincipleClickListener {
    private val visitService: VisitService by lazy {
        VisitService.create()
    }

    private var _binding: FragmentPrinciplesBinding? = null

    private val binding get() = _binding!!
    private val principlesList = mutableListOf<AppVisitParameters.Principle>()
    private val statesList = mutableListOf<Boolean>()
    private val parametersViewModel: ParametersViewModel by activityViewModels()
    private val bundleViewModel: BundleViewModel by activityViewModels()

    private lateinit var context: Context
    private lateinit var preferences: SharedPreferences
    private lateinit var fragmentActivity: FragmentActivity

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPrinciplesBinding.inflate(inflater, container, false)

        context = requireContext()
        fragmentActivity = requireActivity()
        preferences = PreferenceHelper.defaultPrefs(context)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.principlesList.setAdapter(PrinciplesAdapter(principlesList, statesList, this))
        binding.principlesList.setLayoutManager(LinearLayoutManager(activity))
        binding.principlesList.veil()
        binding.principlesList.addVeiledItems(5)

        if (bundleViewModel.isPrinciplesStateEmpty()) {
            principlesList.clear()
            statesList.clear()
            populatePrinciples()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        bundleViewModel.savePrinciplesState(principlesList, statesList)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        if (savedInstanceState != null) {
            bundleViewModel.getPrinciplesList()?.let { principlesList.addAll(it) }
            bundleViewModel.getStatesList()?.let { statesList.addAll(it) }
            updateRecycler(principlesList, statesList)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun populatePrinciples() {
        lifecycleScope.launch {
            val jwt = preferences["jwt", ""]
            val header = "Bearer $jwt"
            val principles = getPrinciples(header, visitService, bundleViewModel, context.applicationContext)
            activePrinciples(principles)
            parametersViewModel.parameters.observe(viewLifecycleOwner) { value ->
                val parametersMap =
                    value?.groupBy { it?.parametro?.principioAgroecologico?.id }
                principlesList.forEach { principle ->
                    statesList.add(
                        parametersMap?.get(principle.id)?.all { it?.cumple == true }
                            ?: true)
                }
            }
            updateRecycler(principlesList, statesList)
        }
    }


    private fun activePrinciples(principles: List<AppVisitParameters.Principle>) {
        val filteredPrinciples = principles.filter {
            it.habilitado == true
        }
        principlesList.addAll(filteredPrinciples)
    }

    private fun updateRecycler(
        principles: List<AppVisitParameters.Principle>,
        states: List<Boolean>
    ) {
        binding.principlesList.setLayoutManager(LinearLayoutManager(activity))
        binding.principlesList.setAdapter(PrinciplesAdapter(principles, states, this))
        binding.principlesList.unVeil()
    }

    override fun onClickChecklist(principle: AppVisitParameters.Principle) {
        bundleViewModel.clearPrinciplesState()
        bundleViewModel.clearParametersState()

        val parametersList = mutableListOf<AppVisitParameters>()
        parametersViewModel.parameters.observe(viewLifecycleOwner) { value ->
            value?.forEach {
                if (it != null && it.parametro?.principioAgroecologico?.id == principle.id) {
                    parametersList.add(it)
                }
            }
        }
        if (parametersList.isNotEmpty()) {
            parametersViewModel.setParametersCurrentPrinciple(parametersList)
            fragmentActivity.supportFragmentManager.beginTransaction()
                .replace(this.id, ParametersFragment())
                .addToBackStack(null)
                .commit()
        } else Toast.makeText(
            context,
            "El principio seleccionado no dispone de parámetros",
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onClickObservations(principle: AppVisitParameters.Principle) {
        bundleViewModel.clearPrinciplesState()
        bundleViewModel.clearObservationsState()
        val name = principle.nombre ?: "Unamed"
        val id = principle.id ?: 0
        fragmentActivity.supportFragmentManager.beginTransaction()
            .replace(this.id, ObservationsFragment(id,name,preferences["email"]))
            .addToBackStack(null)
            .commit()
    }
}
