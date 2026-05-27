package com.qalqan.antifraud.ui.activity

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.qalqan.antifraud.R
import com.qalqan.antifraud.ui.scaffold.PlaceholderRoute

@Composable
fun ActivityRoute() =
    PlaceholderRoute(
        title = stringResource(R.string.activity_title),
        subtitle = stringResource(R.string.activity_soon),
    )
