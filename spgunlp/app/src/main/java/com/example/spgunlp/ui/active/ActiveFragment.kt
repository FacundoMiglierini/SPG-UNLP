package com.example.spgunlp.ui.active

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.spgunlp.MainActivity
import com.example.spgunlp.databinding.FragmentActiveBinding
import com.example.spgunlp.io.VisitService
import com.example.spgunlp.model.AppVisit
import com.example.spgunlp.model.VISIT_ITEM
import com.example.spgunlp.ui.BaseFragment
import com.example.spgunlp.ui.visit.VisitActivity
import com.example.spgunlp.util.PreferenceHelper
import com.example.spgunlp.util.PreferenceHelper.get
import com.example.spgunlp.util.calendar
import com.example.spgunlp.util.syncPrinciplesWithDB
import com.example.spgunlp.util.updateRecycler
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class ActiveFragment : BaseFragment(), VisitClickListener {
    private val visitService: VisitService by lazy {
        VisitService.create()
    }

    private var _binding: FragmentActiveBinding? = null
    private lateinit var someActivityResultLauncher: ActivityResultLauncher<Intent>
    val visitList = mutableListOf<AppVisit>()
    private lateinit var jobToKill: Job
    private lateinit var context: Context
    private lateinit var fragmentActivity: FragmentActivity
    private lateinit var preferences: SharedPreferences
    private val binding get() = _binding!!

    private lateinit var listenerPreferences: SharedPreferences.OnSharedPreferenceChangeListener
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentActiveBinding.inflate(inflater, container, false)
        val root: View = binding.root
        context=requireContext()
        fragmentActivity=requireActivity()
        preferences = PreferenceHelper.defaultPrefs(context)

        someActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { _ ->  }

        binding.searchView.clearFocus()

        binding.btnCalendario.setOnClickListener {
            calendar(
                parentFragmentManager,
                visitList,
                this@ActiveFragment,
                binding.activeList,
                fragmentActivity
            ).onClick(it)
        }
        binding.activeList.setAdapter(VisitAdapter(visitList, this))
        binding.activeList.setLayoutManager(LinearLayoutManager(fragmentActivity))
        binding.activeList.veil()
        binding.activeList.addVeiledItems(5)

        // observes the jwt changes
        listenerPreferences=SharedPreferences.OnSharedPreferenceChangeListener{ sharedPreferences, key ->
            if (key == "jwt") {
                val jwt = sharedPreferences.getString(key, "")
                if (jwt != null && jwt.contains(".")) {
                    Log.i("ActiveFragment", "jwt changed")
                    populateVisits()
                }
            } else if (key=="SYNC_CLICKED" && sharedPreferences.getBoolean(key, false)) {
                Log.i("ActiveFragment", "SYNC_CLICKED")
                populateVisits()
            }
        }
        preferences.registerOnSharedPreferenceChangeListener(listenerPreferences)


        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val filteredList = visitList.filter { visit ->
                    val searchQuery = newText?.lowercase() ?: ""
                    val quinta = visit.quintaResponse
                    val quintaName = quinta?.organizacion?.lowercase()
                    val quintaProductor = quinta?.nombreProductor?.lowercase()
                    quintaName?.contains(searchQuery)!! || quintaProductor?.contains(searchQuery)!!
                }
                updateRecycler(
                    binding.activeList, filteredList,
                    activity, this@ActiveFragment
                )
                return true
            }
        })

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (::jobToKill.isInitialized)
            jobToKill.cancel()
        visitList.clear()
        _binding = null
        preferences.unregisterOnSharedPreferenceChangeListener(listenerPreferences)
    }

    override fun onResume() {
        super.onResume()
        binding.activeList.setAdapter(VisitAdapter(visitList, this))
        binding.activeList.setLayoutManager(LinearLayoutManager(fragmentActivity))
        binding.activeList.veil()
        binding.activeList.addVeiledItems(5)
        populateVisits()
    }

    private fun populateVisits() {
        jobToKill = lifecycleScope.launch {
            val jwt = preferences["jwt", ""]
            if (!jwt.contains("."))
                cancel()
            val header = "Bearer $jwt"
            if (activity == null) cancel()
            val visits = (activity as MainActivity).getVisits(header, context, visitService)
            syncPrinciplesWithDB(header, visitService, context)
            activeVisits(visits)
            if (_binding != null) {
                updateRecycler(
                    binding.activeList, visitList,
                    activity, this@ActiveFragment
                )
            }
        }
    }

    private fun activeVisits(visits: List<AppVisit>) {
        val email = preferences["email", ""]
        val filteredVisits = visits.filter { visit ->
            visit.estadoVisita == "ABIERTA" && visit.integrantes!!.any { integrante ->
                integrante.email == email
            }
        }
        visitList.clear()
        visitList.addAll(filteredVisits.sortedBy { it.fechaActualizacion }.reversed())
    }

    override fun onClick(visit: AppVisit) {
        val intent = Intent(fragmentActivity, VisitActivity::class.java)
        intent.putExtra(VISIT_ITEM, visit)
        someActivityResultLauncher.launch(intent)
    }
}