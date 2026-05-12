package com.qalqan.antifraud.export

import android.content.Context
import com.qalqan.antifraud.database.Repositories

internal object RiskCampaignsGathererArm : GathererArm {
    override suspend fun gather(repositories: Repositories): List<ExportRecord> = emptyList()
}

@Suppress("UnusedPrivateProperty")
internal class TriggeredPatternsGathererArm(private val context: Context) : GathererArm {
    override suspend fun gather(repositories: Repositories): List<ExportRecord> = emptyList()
}
