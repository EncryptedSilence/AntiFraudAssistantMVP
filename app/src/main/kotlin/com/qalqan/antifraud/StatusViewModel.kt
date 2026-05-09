package com.qalqan.antifraud

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.qalqan.antifraud.database.Repositories
import com.qalqan.antifraud.database.manual.ManualEntry
import com.qalqan.antifraud.demo.BuiltInScenario
import com.qalqan.antifraud.demo.DemoImporter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant

class StatusViewModel(application: Application) : AndroidViewModel(application) {
    private val repos = Repositories.build(application)
    private val manual = ManualEntry.create(application, repos)
    private val importer = DemoImporter(manual)

    private val _state = MutableStateFlow(State())
    val state = _state.asStateFlow()

    data class State(
        val calls: Int = 0,
        val sms: Int = 0,
        val web: Int = 0,
        val campaigns: Int = 0,
    )

    fun runDemo() {
        viewModelScope.launch {
            importer.importBuiltin(getApplication(), BuiltInScenario.FAST_ATTACK)
            refresh()
        }
    }

    fun wipe() {
        viewModelScope.launch {
            repos.wipeAll()
            refresh()
        }
    }

    private suspend fun refresh() {
        val callsCount = repos.calls.listSince(Instant.EPOCH).size
        val smsCount = repos.sms.listSince(Instant.EPOCH).size
        _state.value =
            State(
                calls = callsCount,
                sms = smsCount,
                web = 0,
                campaigns = 0,
            )
    }

    override fun onCleared() {
        super.onCleared()
        repos.close()
    }
}
