package com.qalqan.antifraud.ui.references

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.qalqan.antifraud.crypto.BundleManifest
import com.qalqan.antifraud.crypto.BundleManifestJson
import com.qalqan.antifraud.database.Repositories
import com.qalqan.antifraud.domain.SmsCategory
import com.qalqan.antifraud.domain.TrustStatus
import com.qalqan.antifraud.web.LookalikeSeedCatalog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

/**
 * Spec §17.4 — ViewModel for the References screen. All data is read-only and pulled
 * from existing repositories / in-APK seed catalogs / the current sync manifest.
 *
 * Official-contacts list is derived from [LookalikeSeedCatalog.seeds]; entries hosted
 * on the `.gov.kz` and `egov.kz` zones surface as official.
 */
class ReferencesViewModel(
    application: Application,
    private val repos: Repositories,
) : AndroidViewModel(application) {
    private val _state = MutableStateFlow(ReferencesUiState())
    val state = _state.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val contacts = repos.contacts.listAll()
            val manifest = readCurrentManifest()
            _state.value =
                ReferencesUiState(
                    suspiciousNumbers =
                        contacts
                            .filter { it.trustStatus == TrustStatus.SUSPICIOUS }
                            .map { it.phoneHash.value },
                    trustedNumbers =
                        contacts
                            .filter { it.trustStatus == TrustStatus.TRUSTED }
                            .map { it.phoneHash.value },
                    suspiciousDomains = LookalikeSeedCatalog.seeds.toList(),
                    trustedDomains = emptyList(),
                    smsCategories = SmsCategory.entries.map { it.name.lowercase() },
                    officialContacts =
                        LookalikeSeedCatalog.seeds
                            .filter { it.endsWith(".gov.kz") || it == "egov.kz" }
                            .toList(),
                    lastBundleAt = manifest?.createdAt,
                    lastBundleSource = manifest?.source,
                    isLoading = false,
                )
        }
    }

    private fun readCurrentManifest(): BundleManifest? {
        val storeDir = File(getApplication<Application>().filesDir, "sync/current")
        val manifestFile = File(storeDir, "manifest.json")
        if (!manifestFile.exists()) return null
        return BundleManifestJson.parse(manifestFile.readBytes()).getOrNull()
    }
}
