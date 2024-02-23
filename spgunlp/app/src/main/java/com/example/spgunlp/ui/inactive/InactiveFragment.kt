package com.example.spgunlp.ui.inactive

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.spgunlp.MainActivity
import com.example.spgunlp.databinding.FragmentActiveBinding
import com.example.spgunlp.databinding.FragmentInactiveBinding
import com.example.spgunlp.io.VisitService
import com.example.spgunlp.model.AppVisit
import com.example.spgunlp.model.IS_ACTIVE
import com.example.spgunlp.model.VISIT_ITEM
import com.example.spgunlp.ui.BaseFragment
import com.example.spgunlp.ui.active.VisitAdapter
import com.example.spgunlp.ui.active.VisitClickListener
import com.example.spgunlp.ui.visit.VisitActivity
import com.example.spgunlp.util.PreferenceHelper
import com.example.spgunlp.util.PreferenceHelper.get
import com.example.spgunlp.util.calendar
import com.example.spgunlp.util.updateRecycler
import com.google.gson.Gson
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class InactiveFragment : BaseFragment(), VisitClickListener {
    private val visitService: VisitService by lazy {
        VisitService.create()
    }
    private var showAll: Boolean = false

    val visitList = mutableListOf<AppVisit>()

    private val inactiveViewModel: InactiveViewModel by activityViewModels()

    private var _binding: FragmentInactiveBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var visitLayout: FragmentActiveBinding
    private lateinit var jobToKill: Job
    private lateinit var context: Context
    private lateinit var fragmentActivity: FragmentActivity
    private lateinit var preferences: SharedPreferences

    private lateinit var listenerPreferences: SharedPreferences.OnSharedPreferenceChangeListener
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        context = requireContext()
        fragmentActivity = requireActivity()
        preferences = PreferenceHelper.defaultPrefs(context)

        showAll=inactiveViewModel.showAll

        _binding = FragmentInactiveBinding.inflate(inflater, container, false)
        val root: View = binding.root

        visitLayout = binding.layoutVisits
        visitLayout.titleActive.text = "Historial de visitas"
        visitLayout.btnFiltro.text =
            if (inactiveViewModel.showAll) "Mostrar mis visitas" else "Mostrar todas las visitas"

        // observes the jwt changes
        listenerPreferences=SharedPreferences.OnSharedPreferenceChangeListener{ sharedPreferences, key ->
            if (key == "jwt") {
                val jwt = sharedPreferences.getString(key, "")
                if (jwt != null && jwt.contains(".")) {
                    Log.i("InactiveFragment", "jwt changed")
                    populateVisits()
                }
            } else if (key=="SYNC_CLICKED" && sharedPreferences.getBoolean(key, false)) {
                Log.i("InactiveFragment", "SYNC_CLICKED")
                populateVisits()
            }
        }
        preferences.registerOnSharedPreferenceChangeListener(listenerPreferences)

        visitLayout.searchView.clearFocus()

        visitLayout.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
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
                    visitLayout.activeList, filteredList,
                    activity, this@InactiveFragment
                )
                return true
            }
        })

        visitLayout.btnCalendario.setOnClickListener {
            calendar(
                parentFragmentManager,
                visitList,
                this@InactiveFragment,
                visitLayout.activeList,
                fragmentActivity
            ).onClick(it)
        }
        visitLayout.btnFiltro.visibility=View.VISIBLE

        visitLayout.btnFiltro.setOnClickListener {
            visitLayout.btnFiltro.text =
                if (!inactiveViewModel.showAll) "Mostrar mis visitas" else "Mostrar todas las visitas"
            inactiveViewModel.showAll = !inactiveViewModel.showAll
            showAll=inactiveViewModel.showAll
            visitList.clear()
            populateVisits()
        }
        return root
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        inactiveViewModel.saveInactiveVisits(this.visitList)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        if (savedInstanceState != null){
            inactiveViewModel.getInactiveVisits()?.let { this.visitList.addAll(it) }
            updateRecycler(
                visitLayout.activeList, visitList,
                activity, this@InactiveFragment
            )
        }
    }

    override fun onResume() {
        super.onResume()
        visitLayout.activeList.setAdapter(VisitAdapter(visitList, this))
        visitLayout.activeList.setLayoutManager(LinearLayoutManager(fragmentActivity))
        visitLayout.activeList.veil()
        visitLayout.activeList.addVeiledItems(5)
        populateVisits()
    }

    private fun populateVisits() {
        jobToKill = lifecycleScope.launch {
            val jwt = preferences["jwt", ""]
            if (!jwt.contains("."))
                cancel()
            val header = "Bearer $jwt"
            val visits = (activity as MainActivity).getVisits(header, context, visitService)
            inactiveVisits(visits)
            if (_binding != null){

                updateRecycler(
                    visitLayout.activeList, visitList, activity, this@InactiveFragment
                )
                visitLayout.activeList.unVeil()
            }
            Log.i("InactiveFragment", "populateVisits: ")
        }
    }

    private fun inactiveVisits(visits: List<AppVisit>) {
        val email = preferences["email", ""]
        val filteredVisits = visits.filter { visit ->
            visit.estadoVisita == "CERRADA" && (isUserIn(email, visit) || showAll)
        }
        visitList.addAll(filteredVisits.sortedBy { it.fechaActualizacion }.reversed())
    }

    private fun isUserIn(user: String, visit: AppVisit): Boolean {
        return visit.integrantes!!.any { integrante ->
            integrante.email == user
        }

    }

    override fun onClick(visit: AppVisit) {
        val intent = Intent(fragmentActivity, VisitActivity::class.java)
        val gson = Gson()
        val visitGson = gson.toJson(visit)
        intent.putExtra(VISIT_ITEM, visitGson)
        intent.putExtra(IS_ACTIVE, false)
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (::jobToKill.isInitialized)
            jobToKill.cancel()
        visitList.clear()
        preferences.unregisterOnSharedPreferenceChangeListener(listenerPreferences)
        _binding = null
    }
}