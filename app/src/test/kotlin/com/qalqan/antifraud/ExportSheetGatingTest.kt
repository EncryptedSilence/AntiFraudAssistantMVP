package com.qalqan.antifraud

import android.os.Looper
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import com.qalqan.antifraud.export.ExportCategory
import com.qalqan.antifraud.export.ExportOrchestrator
import com.qalqan.antifraud.export.ExportRequest
import io.mockk.coEvery
import io.mockk.mockk
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows

/**
 * §17.5 gate — verifies that the Save button in [ExportSheet] is disabled until a matching
 * preview has been confirmed, and becomes disabled again once the selection is mutated
 * after the preview.
 *
 * Notes on Robolectric + Compose interaction:
 * - [ExportSheet] category chips use FilterChip (Modifier.toggleable). In this Robolectric
 *   setup, performSemanticsAction(SemanticsActions.OnClick) is required to trigger the chip
 *   onClick; performClick() alone does not update Compose state for toggleable nodes.
 * - The "Generate preview" / "Save…" enabled states are driven by pure Compose state
 *   (selectedCategories, previewState). The synchronous gating tests assert states that do
 *   not depend on the coroutine completing.
 */
@OptIn(ExperimentalMaterial3Api::class)
@RunWith(RobolectricTestRunner::class)
class ExportSheetGatingTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private fun previewFor(req: ExportRequest) =
        ExportOrchestrator.Preview(
            token = "test-token",
            request = req,
            bytes = "{}".toByteArray(),
        )

    private fun stubViewModel(): StatusViewModel {
        val vm = mockk<StatusViewModel>(relaxed = true)
        coEvery { vm.generateExportPreview(any()) } answers {
            val req = firstArg<ExportRequest>()
            Result.success(previewFor(req))
        }
        return vm
    }

    private fun drainLooper() {
        repeat(5) {
            Shadows.shadowOf(Looper.getMainLooper()).idle()
        }
        composeTestRule.waitForIdle()
    }

    @Test
    fun `Save button is disabled before category selection (§17_5 gate - initial state)`() {
        val vm = stubViewModel()
        composeTestRule.setContent {
            ExportSheet(onDismiss = {}, viewModel = vm)
        }
        composeTestRule.onNodeWithText("Generate preview").assertIsNotEnabled()
        composeTestRule.onNodeWithText("Save…").assertIsNotEnabled()
    }

    @Test
    fun `Save button is disabled after category selected but before preview (§17_5 gate)`() {
        val vm = stubViewModel()
        composeTestRule.setContent {
            ExportSheet(onDismiss = {}, viewModel = vm)
        }
        composeTestRule.onNodeWithText(ExportCategory.SUSPICIOUS_NUMBERS.name)
            .performSemanticsAction(SemanticsActions.OnClick)
        drainLooper()
        composeTestRule.onNodeWithText("Generate preview").assertIsEnabled()
        composeTestRule.onNodeWithText("Save…").assertIsNotEnabled()
    }

    @Test
    fun `mutating the category selection after preview disables Save again (§17_5 gate)`() {
        val vm = stubViewModel()
        composeTestRule.setContent {
            ExportSheet(onDismiss = {}, viewModel = vm)
        }
        composeTestRule.onNodeWithText(ExportCategory.SUSPICIOUS_NUMBERS.name)
            .performSemanticsAction(SemanticsActions.OnClick)
        drainLooper()
        composeTestRule.onNodeWithText("Generate preview").performClick()
        drainLooper()
        composeTestRule.onNodeWithText(ExportCategory.RISK_CAMPAIGNS.name)
            .performSemanticsAction(SemanticsActions.OnClick)
        drainLooper()
        composeTestRule.onNodeWithText("Save…").assertIsNotEnabled()
    }
}
