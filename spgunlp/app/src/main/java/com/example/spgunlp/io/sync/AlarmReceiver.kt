package com.example.spgunlp.io.sync

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.spgunlp.util.performSync
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

class AlarmReceiver : BroadcastReceiver() {
    private fun BroadcastReceiver.goAsync(
        context: CoroutineContext = EmptyCoroutineContext,
        block: suspend CoroutineScope.() -> Unit
    ) {
        val pendingResult = goAsync()
        @OptIn(DelicateCoroutinesApi::class) // Must run globally; there's no teardown callback.
        GlobalScope.launch(context) {
            try {
                block()
            } finally {
                pendingResult.finish()
            }
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) = goAsync {
        if (context != null) {
            performSync(context)
        }
    }
}