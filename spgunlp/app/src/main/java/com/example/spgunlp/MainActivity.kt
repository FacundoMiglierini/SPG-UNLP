package com.example.spgunlp

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import android.widget.Toast.makeText
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.spgunlp.databinding.ActivityMainBinding
import com.example.spgunlp.io.AuthService
import com.example.spgunlp.io.VisitService
import com.example.spgunlp.io.sync.AndroidAlarmScheduler
import com.example.spgunlp.model.AppVisit
import com.example.spgunlp.util.PreferenceHelper
import com.example.spgunlp.util.PreferenceHelper.get
import com.example.spgunlp.util.PreferenceHelper.set
import com.example.spgunlp.util.VisitChangesDBViewModel
import com.example.spgunlp.util.VisitsDBViewModel
import com.example.spgunlp.util.createVisit
import com.example.spgunlp.util.performLogin
import com.example.spgunlp.util.performSync
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val authService: AuthService by lazy {
        AuthService.create()
    }

    private val visitService: VisitService by lazy {
        VisitService.create()
    }

    private lateinit var binding: ActivityMainBinding

    private lateinit var visitsDBViewModel: VisitsDBViewModel
    private lateinit var visitUpdateViewModel: VisitChangesDBViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val scheduler = AndroidAlarmScheduler(this)
        scheduler.schedule()

        // init viewmodel
        visitsDBViewModel = ViewModelProvider(this)[VisitsDBViewModel::class.java]
        visitUpdateViewModel = ViewModelProvider(this)[VisitChangesDBViewModel::class.java]

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        navView.setupWithNavController(navController)

        binding.fabSync.setOnClickListener {
            val preferences = PreferenceHelper.defaultPrefs(this)
            if (!preferences["jwt", ""].contains(".")) {
                return@setOnClickListener
            }
            lifecycleScope.launch {
                binding.fabSync.animate().rotationBy(360f).setDuration(1000).start()
                val colorRed = ContextCompat.getColor(applicationContext, R.color.red)
                if (performSync(this@MainActivity)) {
                    makeText(
                        this@MainActivity,
                        "Sincronización exitosa",
                        Toast.LENGTH_SHORT
                    ).show()
                    updateColorFab()
                } else if (preferences["COLOR_FAB", -1] == colorRed) {
                    makeLoginPopup(visitService)
                } else
                    makeText(
                        this@MainActivity,
                        "Sincronización fallida",
                        Toast.LENGTH_SHORT
                    ).show()
                updateColorFab()
                // cancel animation after 1 second
                delay(1000)
                binding.fabSync.animate().cancel()
            }
        }
    }

    fun setBottomNavigationVisibility(visibility: Int) {
        binding.navView.visibility = visibility
        binding.fabSync.visibility = visibility
    }

    override fun onBackPressed() {
        val preferences = PreferenceHelper.defaultPrefs(this)
        if (!preferences["jwt", ""].contains(".")) {
            return
        }

        val bottomNavigationView = binding.navView
        val selectedItemId = bottomNavigationView.selectedItemId
        if (R.id.navigation_active != selectedItemId) {
            bottomNavigationView.selectedItemId = R.id.navigation_active
            return
        }
        super.onBackPressed()
    }

    fun getFab(): FloatingActionButton {
        return binding.fabSync
    }

    @OptIn(DelicateCoroutinesApi::class)
    suspend fun updateColorFab() {
        val preferences = PreferenceHelper.defaultPrefs(this)
        val color = preferences["COLOR_FAB", -1]

        if (color != -1) {
            binding.fabSync.backgroundTintList = ColorStateList.valueOf(color)
        } else {
            val visits = GlobalScope.async {
                return@async visitUpdateViewModel.getVisitsByEmail(preferences["email"])
            }.await()
            if (visits.isEmpty()) {
                binding.fabSync.backgroundTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(applicationContext, R.color.green))
            } else {
                binding.fabSync.backgroundTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(applicationContext, R.color.yellow))
            }
        }

        Log.i("MainActivity", "updateColorFab: ${preferences["COLOR_FAB", -1]}")
    }

    @OptIn(DelicateCoroutinesApi::class)
    suspend fun getVisits(
        header: String,
        context: Context,
        visitService: VisitService,
    ): List<AppVisit> {
        var visits: List<AppVisit> = emptyList()

        try {
            val response = visitService.getVisits(header)
            val body = response.body()
            if (response.isSuccessful && body != null) {
                visits = body
                visitsDBViewModel.clearVisits()
                visitsDBViewModel.insertVisits(visits)
                visits = updateVisitsWithLocalChanges(context, visits)
                Log.i("SPGUNLP_TAG", "getVisits: made api call and was successful")
            } else if (response.code() == 401 || response.code() == 403) {
                visits = GlobalScope.async {
                    val visitsDB = visitsDBViewModel.getAllVisits()
                    return@async updateVisitsWithLocalChanges(context, visitsDB)
                }.await()
                makeLoginPopup(visitService).also {
                    it.observe(this) { visits = it }
                }
            }
        } catch (e: Exception) {
            Log.e("SPGUNLP_TAG", e.message.toString())
            visits = GlobalScope.async {
                val visitsDB = visitsDBViewModel.getAllVisits()
                return@async updateVisitsWithLocalChanges(context, visitsDB)
            }.await()
            val preferences = PreferenceHelper.defaultPrefs(this)
            preferences["COLOR_FAB"]= ContextCompat.getColor(this, R.color.yellow) // not synced
            updateColorFab()
        }

        return visits
    }

    private fun makeLoginPopup(visitService: VisitService): LiveData<List<AppVisit>>{
        val dialog = MaterialAlertDialogBuilder(this@MainActivity).create()
        val inflater = LayoutInflater.from(this@MainActivity)
        val view = inflater.inflate(R.layout.fragment_login, null)
        view.findViewById<TextView>(R.id.title_inicio).text = "Token expirado, inicie sesión nuevamente"
        view.findViewById<Button>(R.id.btn_crear_usuario).visibility = View.GONE
        val mail = view.findViewById<EditText>(R.id.edit_mail)
        val preferences = PreferenceHelper.defaultPrefs(this)
        mail.setText(preferences["email", ""])
        dialog.setView(view)
        val pwd = view.findViewById<EditText>(R.id.edit_password)
        val result = MutableLiveData<List<AppVisit>>()

        preferences["COLOR_FAB"] = ContextCompat.getColor(this, R.color.red)
        lifecycleScope.launch {
            updateColorFab()
        }

        view.findViewById<Button>(R.id.btn_iniciar_sesion).setOnClickListener {
            lifecycleScope.launch {
                if (
                    performLogin(
                        mail.text.toString(),
                        pwd.text.toString(),
                        this@MainActivity,
                        authService
                    ) && performSync(this@MainActivity)
                ) {
                    makeText(
                        this@MainActivity,
                        "Se ha sincronizado correctamente",
                        Toast.LENGTH_SHORT
                    ).show()
                    val header = "Bearer ${preferences["jwt", ""]}"
                    val visits = getVisits(header, this@MainActivity, visitService)
                    result.postValue(visits)
                    preferences["COLOR_FAB"] = ContextCompat.getColor(this@MainActivity, R.color.green)
                    updateColorFab()
                    dialog.dismiss()
                } else {
                    makeText(
                        this@MainActivity,
                        "Inicio de sesion fallido",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
        dialog.show()
        return result
    }

    @OptIn(DelicateCoroutinesApi::class)
    private suspend fun updateVisitsWithLocalChanges(context: Context, visits: List<AppVisit>): List<AppVisit> {
        val visitsUpdates = mutableListOf<AppVisit>()
        val preferences = PreferenceHelper.defaultPrefs(context)
        val email: String = preferences["email"]
        val updates = GlobalScope.async {
            return@async visitUpdateViewModel.getVisitsByEmail(email)
        }.await()
        visits.forEach { visit ->
            val visitFound = updates.find {
                it.visitId == visit.id
            }
            if (visitFound != null) {
                visitsUpdates.add(createVisit(visit, visitFound.visit))
            } else {
                visitsUpdates.add(visit)
            }
        }

        return visitsUpdates.toList()
    }

}