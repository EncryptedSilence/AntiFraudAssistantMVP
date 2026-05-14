package com.qalqan.antifraud.ui.question

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.qalqan.antifraud.R
import com.qalqan.antifraud.settings.QuestionPromptKind
import com.qalqan.antifraud.ui.state.accessibleTouchTarget

/**
 * Spec §5.5.2 — renders one of the three irreducible questions as a card with three
 * answer buttons (yes / no / not sure) plus a "Don't ask again for this campaign" link.
 */
@Composable
fun QuestionPromptCard(
    kind: QuestionPromptKind,
    onAnswerYes: () -> Unit,
    onAnswerNo: () -> Unit,
    onAnswerNotSure: () -> Unit,
    onDontAskAgain: () -> Unit,
) {
    Card(modifier = Modifier.padding(16.dp)) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                stringResource(questionResId(kind)),
                style = MaterialTheme.typography.bodyLarge,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onAnswerYes, modifier = Modifier.accessibleTouchTarget()) {
                    Text(stringResource(R.string.question_answer_yes))
                }
                Button(onClick = onAnswerNo, modifier = Modifier.accessibleTouchTarget()) {
                    Text(stringResource(R.string.question_answer_no))
                }
                Button(onClick = onAnswerNotSure, modifier = Modifier.accessibleTouchTarget()) {
                    Text(stringResource(R.string.question_answer_not_sure))
                }
            }
            TextButton(onClick = onDontAskAgain, modifier = Modifier.accessibleTouchTarget()) {
                Text(stringResource(R.string.question_dont_ask_again))
            }
        }
    }
}

private fun questionResId(kind: QuestionPromptKind): Int =
    when (kind) {
        QuestionPromptKind.CALLER_IDENTITY -> R.string.question_caller_identity
        QuestionPromptKind.PRESSURE -> R.string.question_pressure
        QuestionPromptKind.ACTION_REQUEST -> R.string.question_action_request
    }
