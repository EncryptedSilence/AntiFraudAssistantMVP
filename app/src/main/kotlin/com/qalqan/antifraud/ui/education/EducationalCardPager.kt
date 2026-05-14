package com.qalqan.antifraud.ui.education

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.qalqan.antifraud.R
import com.qalqan.antifraud.ui.state.accessibleTouchTarget

/**
 * Spec §19A — five-card HorizontalPager. Shown at most once per 24 h, gated by
 * `UserSettings.educationalCardsEnabled` and the [com.qalqan.antifraud.settings.EducationalCardScheduler].
 */
@Composable
fun EducationalCardPager(
    visible: Boolean,
    onDismiss: () -> Unit,
) {
    if (!visible) return
    val pages =
        listOf(
            R.string.edu_card_1_title to R.string.edu_card_1_body,
            R.string.edu_card_2_title to R.string.edu_card_2_body,
            R.string.edu_card_3_title to R.string.edu_card_3_body,
            R.string.edu_card_4_title to R.string.edu_card_4_body,
            R.string.edu_card_5_title to R.string.edu_card_5_body,
        )
    val pagerState = rememberPagerState(pageCount = { pages.size })
    Card(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        HorizontalPager(state = pagerState) { page ->
            val (titleResId, bodyResId) = pages[page]
            Column(modifier = Modifier.padding(16.dp)) {
                Text(stringResource(titleResId), style = MaterialTheme.typography.titleMedium)
                Text(stringResource(bodyResId), style = MaterialTheme.typography.bodyMedium)
            }
        }
        TextButton(onClick = onDismiss, modifier = Modifier.accessibleTouchTarget()) {
            Text(stringResource(R.string.edu_card_dismiss_button))
        }
    }
}
