package com.qalqan.antifraud.ui.lists

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.qalqan.antifraud.R
import com.qalqan.antifraud.ui.scaffold.PlaceholderRoute

@Composable
fun ListsRoute() =
    PlaceholderRoute(
        title = stringResource(R.string.lists_title),
        subtitle = stringResource(R.string.lists_soon),
    )
