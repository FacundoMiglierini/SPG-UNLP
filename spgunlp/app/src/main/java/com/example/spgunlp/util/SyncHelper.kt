package com.example.spgunlp.util

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.spgunlp.R
import com.example.spgunlp.io.AuthService
import com.example.spgunlp.io.VisitService
import com.example.spgunlp.io.response.AuthErrorResponse
import com.example.spgunlp.model.AppUser
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.example.spgunlp.util.PreferenceHelper.get
import com.example.spgunlp.util.PreferenceHelper.set
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async

@OptIn(DelicateCoroutinesApi::class)
suspend fun performSync(context: Context): Boolean {
    val visitService = VisitService.create()
    val preferences = context.let { PreferenceHelper.defaultPrefs(it) }
    val email: String = preferences["email"]
    val dao = AppDatabase.getDatabase(context).visitUpdatesDao()
    val visits = GlobalScope.async {
        return@async dao.getVisitsByEmailSync(email)
    }.await()
    Log.i("ALARM_RECEIVER", "onReceive, viendo si hay visitas")
    val jwt = preferences["jwt", ""]
    var result = true
    if (visits.isNotEmpty() && (jwt != "")) {
        val visitService = VisitService.create()
        val header = "Bearer $jwt"
        for (visit in visits) {
            Log.i("ALARM_RECEIVER", "visita: ${visit.visitId}")
            try {
                val response = visitService.updateVisitById(header, visit.visitId, visit.visit)
                if (response.isSuccessful) {
                    Log.i("ALARM_RECEIVER", "Actualizado: ${visit.visitId}")
                    dao.deleteVisitById(visit.visitId)
                } else {
                    result = false
                    preferences["COLOR_FAB"] = ContextCompat.getColor(context, R.color.red)
                    Log.i("ALARM_RECEIVER", "Error actualizando: ${visit.visitId}")
                }
            } catch (e: Exception) {
                result = false
                preferences["COLOR_FAB"] = ContextCompat.getColor(context, R.color.yellow)
                Log.i("ALARM_RECEIVER", "onReceive: $e")
            }
        }
    }

    if (result)
        preferences["COLOR_FAB"] = ContextCompat.getColor(context, R.color.green)

    // sync_clicked is true if there's internet connection
    preferences["SYNC_CLICKED"] = false
    try {
        val res = visitService.getHome("Bearer $jwt")
        if (res.code() == 401 || res.code() == 403) {
            preferences["COLOR_FAB"] = ContextCompat.getColor(context, R.color.red)
            result = false
            Log.i("ALARM_RECEIVER", "onReceive: 401 o 403")
        } else {
            preferences["SYNC_CLICKED"] = true
        }
    } catch (e: Exception) {
        preferences["COLOR_FAB"] = ContextCompat.getColor(context, R.color.yellow)
        result = false
    }

    return result
}

private fun createSessionPreferences(jwt: String, email: String, context: Context) {
    val preferences = PreferenceHelper.defaultPrefs(context)
    preferences["jwt"] = jwt
    preferences["email"] = email
}

suspend fun performLogin(
    mail: String,
    password: String,
    context: Context,
    authService: AuthService
): Boolean {
    // make the call to the remote API with coroutines
    val user = AppUser(mail, password)
    try {

        val response = authService.login(user)

        if (response.isSuccessful) {
            val loginResponse = response.body()
            if (loginResponse != null) {
                createSessionPreferences(loginResponse.token, loginResponse.usuario, context)

                return true
            }
        } else {
            if (response.code() == 401) {
                Toast.makeText(
                    context,
                    "Usuario o contraseña incorrectos",
                    Toast.LENGTH_SHORT
                ).show()
                return false
            }
            val errorBody = response.errorBody()!!.string()
            val gson = Gson()
            val type = object : TypeToken<AuthErrorResponse>() {}.type
            val errorResponse: AuthErrorResponse? = gson.fromJson(errorBody, type)
            if (errorResponse != null) {
                Toast.makeText(context, errorResponse.detalleError, Toast.LENGTH_SHORT).show()
            }
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Error de conexión", Toast.LENGTH_SHORT).show()
    }
    return false
}