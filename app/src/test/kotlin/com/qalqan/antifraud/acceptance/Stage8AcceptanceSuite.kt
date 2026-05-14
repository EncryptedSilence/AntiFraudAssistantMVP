package com.qalqan.antifraud.acceptance

import org.junit.runner.RunWith
import org.junit.runners.Suite

/**
 * Spec §23 — aggregate suite for the Stage 8 acceptance boundaries. Re-running this
 * suite from the command line is the single command an integrator runs to verify the
 * Stage 8 surface is intact.
 */
@RunWith(Suite::class)
@Suite.SuiteClasses(
    Acceptance2NoRegistrationOnFirstLaunchTest::class,
    Acceptance14DisablePatternUiTest::class,
    Acceptance17ExplainabilityRendersAtLeastThreeReasonsTest::class,
    Acceptance18PostCallSmsSiteQuestionsTest::class,
    Acceptance20DeleteAllViaPrivacyTest::class,
    Acceptance42NoQuestionBelowHighTest::class,
    Acceptance43AtMostThreeQuestionsPerCampaignTest::class,
    Acceptance44WizardGatingTest::class,
    Acceptance45ManualEntryReachableHomeTest::class,
    OnboardingSdkAwareSequenceTest::class,
    PauseBeforeActionAtCriticalTest::class,
    EducationalCardOncePer24hTest::class,
)
class Stage8AcceptanceSuite
