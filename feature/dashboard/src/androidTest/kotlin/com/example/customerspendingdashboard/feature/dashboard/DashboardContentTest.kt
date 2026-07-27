package com.example.customerspendingdashboard.feature.dashboard

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.customerspendingdashboard.core.designsystem.theme.SpendingTheme
import com.example.customerspendingdashboard.feature.dashboard.presentation.DashboardContent
import com.example.customerspendingdashboard.feature.dashboard.presentation.DashboardUiState
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DashboardContentTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    // Tapping Retry in the error state invokes the retry callback.
    @Test
    fun showsErrorStateWithRetryWhenThereIsNoDataAndAnError() {
        var retried = false
        composeTestRule.setContent {
            SpendingTheme {
                DashboardContent(
                    state = DashboardUiState(isLoading = false, error = "Couldn't refresh."),
                    onRangeSelected = {},
                    onRetry = { retried = true },
                )
            }
        }

        composeTestRule.onNodeWithText("Couldn't refresh.").assertExists()
        composeTestRule.onNodeWithText("Retry").performClick()

        assertTrue(retried)
    }
}
