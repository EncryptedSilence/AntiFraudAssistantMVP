package com.qalqan.antifraud

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.qalqan.antifraud.calls.CallObserverPermissions
import com.qalqan.antifraud.calls.CallObserverService

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StatusScreen()
        }
    }

    override fun onResume() {
        super.onResume()
        if (CallObserverPermissions(this).state() == CallObserverPermissions.State.GRANTED) {
            CallObserverService.start(this)
        }
    }
}
