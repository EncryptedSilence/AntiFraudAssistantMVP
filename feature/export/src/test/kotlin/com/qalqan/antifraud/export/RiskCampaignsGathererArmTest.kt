package com.qalqan.antifraud.export

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.qalqan.antifraud.database.Repositories
import com.qalqan.antifraud.domain.CampaignId
import com.qalqan.antifraud.domain.CampaignStatus
import com.qalqan.antifraud.domain.RiskBand
import com.qalqan.antifraud.domain.RiskCampaign
import com.qalqan.antifraud.domain.ScenarioCategory
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Instant

@RunWith(RobolectricTestRunner::class)
class RiskCampaignsGathererArmTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val repos = Repositories.inMemory(context)

    @After
    fun tearDown() {
        repos.close()
    }

    @Test
    fun `empty repository emits no records`() {
        runBlocking {
            RiskCampaignsGathererArm.gather(repos).size shouldBe 0
        }
    }

    @Test
    fun `emits one record per active or closed campaign, skipping archived and false_positive`() {
        runBlocking {
            val now = Instant.parse("2026-05-01T10:00:00Z")
            repos.campaigns.save(
                RiskCampaign(
                    campaignId = CampaignId("c-active"),
                    startedAt = now,
                    lastEventAt = now,
                    status = CampaignStatus.ACTIVE,
                    scenarioType = ScenarioCategory.BANK_FRAUD,
                    relatedPhoneHashes = emptySet(),
                    relatedSmsSenderHashes = emptySet(),
                    relatedDomainHashes = emptySet(),
                    relatedEventIds = emptyList(),
                    relatedSessionIds = emptyList(),
                    userAnswerIds = emptyList(),
                    triggeredPatternIds = emptyList(),
                    campaignRiskScore = 87,
                    campaignRiskBand = RiskBand.CRITICAL,
                    explanation = "unknown call then SMS within 24h",
                ),
            )
            repos.campaigns.save(
                RiskCampaign(
                    campaignId = CampaignId("c-archived"),
                    startedAt = now,
                    lastEventAt = now,
                    status = CampaignStatus.ARCHIVED,
                    scenarioType = null,
                    relatedPhoneHashes = emptySet(),
                    relatedSmsSenderHashes = emptySet(),
                    relatedDomainHashes = emptySet(),
                    relatedEventIds = emptyList(),
                    relatedSessionIds = emptyList(),
                    userAnswerIds = emptyList(),
                    triggeredPatternIds = emptyList(),
                    campaignRiskScore = 10,
                    campaignRiskBand = RiskBand.LOW,
                    explanation = null,
                ),
            )

            val records = RiskCampaignsGathererArm.gather(repos)
            records.size shouldBe 1
            val campaign = records.first() as ExportRecord.RiskCampaign
            campaign.campaignId shouldBe "c-active"
            campaign.status shouldBe "active"
            campaign.scenarioType shouldBe "bank_fraud"
            campaign.campaignRiskLevel shouldBe "critical"
        }
    }
}
