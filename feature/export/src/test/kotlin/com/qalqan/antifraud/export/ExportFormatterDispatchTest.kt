@file:Suppress("SENSELESS_COMPARISON")

package com.qalqan.antifraud.export

import io.kotest.matchers.shouldBe
import org.junit.Test

class ExportFormatterDispatchTest {
    @Test
    fun `dispatch returns a non-null formatter for every ExportFormat variant`() {
        ExportFormat.entries.forEach { fmt ->
            val formatter = ExportFormatters.forFormat(fmt)
            (formatter !== null) shouldBe true
        }
    }

    @Test
    fun `each formatter advertises the ExportFormat it serves`() {
        ExportFormat.entries.forEach { fmt ->
            ExportFormatters.forFormat(fmt).format shouldBe fmt
        }
    }
}
