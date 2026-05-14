package com.qalqan.antifraud.ui.state

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.qalqan.antifraud.R

@Composable
fun LoadingState(
    @StringRes messageResId: Int = R.string.state_loading_default,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .semantics { contentDescription = "Loading state" },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator()
        Text(stringResource(messageResId))
    }
}
