package com.example.spgunlp.ui.stats

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.spgunlp.MainActivity
import com.example.spgunlp.R
import com.example.spgunlp.databinding.FragmentStatsBinding
import com.example.spgunlp.io.VisitService
import com.example.spgunlp.model.AppVisitParameters
import com.example.spgunlp.ui.BaseFragment
import com.example.spgunlp.ui.visit.BundleViewModel
import com.example.spgunlp.ui.visit.ParameterClickListener
import com.example.spgunlp.util.PreferenceHelper
import com.example.spgunlp.util.PreferenceHelper.get
import com.example.spgunlp.util.getPrinciples
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class StatsFragment : BaseFragment(), StatsClickListener {

    private var _binding: FragmentStatsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val visitService: VisitService by lazy {
        VisitService.create()
    }
    private val bundleViewModel: BundleViewModel by activityViewModels()
    private var principlesList = mutableListOf<AppVisitParameters.Principle>()
    private var percentageList = mutableListOf<Float>()
    private var cumpleList = mutableListOf<Boolean>()
    private var approvedVisitsPercentage = 0f
    private lateinit var jobToKill: Job
    private lateinit var listenerPreferences: SharedPreferences.OnSharedPreferenceChangeListener
    private lateinit var mainActivity: MainActivity
    private lateinit var preferences: SharedPreferences
    private lateinit var context: Context

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatsBinding.inflate(inflater, container, false)
        context = requireContext()
        mainActivity = activity as MainActivity
        preferences = PreferenceHelper.defaultPrefs(context)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // observes the jwt changes
        listenerPreferences= SharedPreferences.OnSharedPreferenceChangeListener{ sharedPreferences, key ->
            if (key == "jwt") {
                val jwt = sharedPreferences.getString(key, "")
                if (jwt != null && jwt.contains(".")) {
                    Log.i("ActiveFragment", "jwt changed")
                    populatePrinciples()
                }
            }else if (key=="SYNC_CLICKED" && sharedPreferences.getBoolean(key, false)){
                Log.i("StatsFragment", "SYNC_CLICKED")
                populatePrinciples()
            }
        }
        preferences.registerOnSharedPreferenceChangeListener(listenerPreferences)
        if (_binding != null) {
            binding.approvedVisitsCard.setOnClickListener {
                onClickCardView(binding.approvedVisitsCard)
            }

            binding.approvedPrinciplesCard.setOnClickListener {
                onClickCardView(binding.approvedPrinciplesCard)
            }

            binding.approvedPrinciplesGrid.setOnClickListener {
                onClickCardView(binding.approvedPrinciplesCard)
            }
        }

        principlesList.clear()
        populatePrinciples()
    }

    private fun getColor(color: Int): Int {
        return ContextCompat.getColor(context, color)
    }

    @SuppressLint("SetTextI18n")
    private fun populatePrinciples() {
        jobToKill = lifecycleScope.launch {
            val jwt = preferences["jwt", ""]
            val header = "Bearer $jwt"
            val principles =
                getPrinciples(header, visitService, bundleViewModel, context.applicationContext)

            approvedVisitsPercentage = 0f
            activePrinciples(principles)
            percentageList = MutableList(principlesList.size) { 0f }
            // get visits, for each visit, get visitParamRes, for each param get principle and cumple
            val visits = mainActivity.getVisits(header, context, visitService)
            visits.forEach { it ->
                cumpleList = MutableList(principlesList.size) { true }
                if (it.visitaParametrosResponse != null) {
                    it.visitaParametrosResponse.forEach {
                        cumpleList[it.parametro?.principioAgroecologico?.id!! - 1] =
                            cumpleList[it.parametro.principioAgroecologico.id - 1] && it.cumple!!
                    }
                    cumpleList.forEachIndexed { index, cumple ->
                        percentageList[index] =
                            if (cumple) percentageList[index] + 1f / visits.size else percentageList[index]
                    }
                    approvedVisitsPercentage =
                        if (cumpleList.all { it }) approvedVisitsPercentage + 1f / visits.size else approvedVisitsPercentage
                }
            }
            if (_binding != null) {
                val value = (approvedVisitsPercentage * 100).roundToInt()
                when (value) {
                    in 0..25 -> binding.cardviewApprovedvisits.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.red)
                    in 26..74 -> binding.cardviewApprovedvisits.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.yellow)
                    in 75..100 -> binding.cardviewApprovedvisits.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.green)
                }
                binding.percentageVisitsApproved.text = "${value}%"
                
                updateRecycler(principlesList, getFormattedPercentages(percentageList))
                if (principlesList.isEmpty())
                    binding.approvedPrinciplesCard.visibility = View.GONE
                if (visits.isEmpty()){
                    binding.approvedPrinciplesCard.visibility = View.GONE
                    binding.approvedVisitsCard.visibility = View.GONE
                    Toast.makeText(
                        context,
                        "No se encontraron visitas",
                        Toast.LENGTH_SHORT
                    ).show()
                    binding.noDataLayout.visibility = View.VISIBLE
                }else{
                    binding.approvedPrinciplesCard.visibility = View.VISIBLE
                    binding.approvedVisitsCard.visibility = View.VISIBLE
                    binding.noDataLayout.visibility = View.GONE
                }
                binding.approvedPrinciplesVeil.unVeil()
                binding.approvedVisitsVeil.unVeil()
            }
        }
    }

    private fun activePrinciples(principles: List<AppVisitParameters.Principle>) {
        val filteredPrinciples = principles.filter {
            it.habilitado == true && !principlesList.contains(it)
        }
        principlesList.addAll(filteredPrinciples)
    }

    private fun updateRecycler(
        principles: List<AppVisitParameters.Principle>,
        percentages: List<String>
    ) {
        binding.approvedPrinciplesGrid.apply {
            layoutManager = GridLayoutManager(context, 1)
            adapter = StatsAdapter(requireContext(), principles, percentages, this@StatsFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (::jobToKill.isInitialized)
            jobToKill.cancel()
        _binding = null
        // remove preferences listener
        preferences.unregisterOnSharedPreferenceChangeListener(listenerPreferences)
    }

    private fun getFormattedPercentages(percentages: List<Float>): List<String> {
        val formattedList = mutableListOf<String>()
        percentages.forEach {
            formattedList.add("${(it * 100).roundToInt()}%")
        }
        return formattedList
    }

    private fun onClickCardView(cardView: MaterialCardView) {
        cardView.invalidate()
        ObjectAnimator.ofArgb(
            cardView,
            "strokeColor",
            getColor(R.color.purple_200),
            getColor(R.color.green),
            getColor(R.color.teal_200),
            getColor(R.color.purple_200),
        ).apply {
            duration = 3000
            start()
        }
    }

    override fun onClick(percentage: String, principleName: String) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        builder
            .setTitle("$percentage completado")
            .setMessage("$principleName.\n")
            .setPositiveButton("Cerrar") { _, _ ->
            }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.show()
    }
}