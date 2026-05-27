package com.qalqan.antifraud.ui.manual

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.qalqan.antifraud.WebEntrySheet
import com.qalqan.antifraud.database.Repositories
import com.qalqan.antifraud.database.crypto.InMemoryCryptoBox
import com.qalqan.antifraud.database.crypto.KeyStoreCryptoBox
import com.qalqan.antifraud.database.manual.ManualEntry
import com.qalqan.antifraud.database.manual.WebEntryDigest
import com.qalqan.antifraud.domain.CallDirection
import com.qalqan.antifraud.ui.home.SuspiciousCallSheet
import com.qalqan.antifraud.ui.home.SuspiciousSmsSheet
import com.qalqan.antifraud.web.DomainNormalizer
import com.qalqan.antifraud.web.DomainSeenChecker
import com.qalqan.antifraud.web.LookalikeDetector
import com.qalqan.antifraud.web.LookalikeSeedCatalog
import com.qalqan.antifraud.web.WebEventBuilder
import com.qalqan.antifraud.web.WebManualCapture
import com.qalqan.antifraud.web.WebObserverActionLog
import kotlinx.coroutines.launch
import java.time.Instant

/**
 * Shared host for the three manual-entry sheets (§23 #45). Owns the sheet visibility state
 * and the [ManualEntry] wiring once, and exposes open-callbacks to its [content] so multiple
 * screens (Home / Add) can trigger the same fallback capture path without duplicating it.
 */
@Composable
@Suppress("LongMethod")
fun ManualEntrySheetsHost(
    repos: Repositories,
    onAfterSubmit: () -> Unit = {},
    content: @Composable (openCall: () -> Unit, openSms: () -> Unit, openSite: () -> Unit) -> Unit,
) {
    val app = LocalContext.current.applicationContext as Application
    var showCallSheet by remember { mutableStateOf(false) }
    var showSmsSheet by remember { mutableStateOf(false) }
    var showSiteSheet by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val manual =
        remember(repos) {
            val box =
                runCatching { KeyStoreCryptoBox.create(app, alias = "antifraud.field_box") }
                    .getOrElse { InMemoryCryptoBox() }
            ManualEntry.create(app, repos, box)
        }

    content(
        { showCallSheet = true },
        { showSmsSheet = true },
        { showSiteSheet = true },
    )

    if (showCallSheet) {
        SuspiciousCallSheet(
            onDismiss = { showCallSheet = false },
            onSubmit = { raw ->
                scope.launch {
                    runCatching {
                        manual.calls.submit(
                            rawNumber = raw,
                            direction = CallDirection.INCOMING,
                            startedAt = Instant.now(),
                            durationSec = 0,
                            isKnownContact = false,
                        )
                    }
                    onAfterSubmit()
                }
            },
        )
    }
    if (showSmsSheet) {
        SuspiciousSmsSheet(
            onDismiss = { showSmsSheet = false },
            onSubmit = { sender, body ->
                scope.launch {
                    runCatching { manual.sms.submit(sender, Instant.now(), body) }
                    onAfterSubmit()
                }
            },
        )
    }
    if (showSiteSheet) {
        WebEntrySheet(
            onDismiss = { showSiteSheet = false },
            onSubmit = { rawInput, onResult ->
                scope.launch {
                    val capture =
                        WebManualCapture(
                            normalizer = DomainNormalizer(),
                            detector = LookalikeDetector(LookalikeSeedCatalog.seeds),
                            seenChecker = DomainSeenChecker(repos.web),
                            builder = WebEventBuilder(WebEntryDigest.create(app)),
                            repo = repos.web,
                            actionLog = WebObserverActionLog(repos.actionLogger),
                        )
                    val outcome = capture.submit(rawInput, Instant.now())
                    onResult(outcome)
                    onAfterSubmit()
                }
            },
        )
    }
}
