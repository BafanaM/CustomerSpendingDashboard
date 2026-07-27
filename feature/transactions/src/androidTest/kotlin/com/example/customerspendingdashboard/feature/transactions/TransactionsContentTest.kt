package com.example.customerspendingdashboard.feature.transactions

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.customerspendingdashboard.core.designsystem.theme.SpendingTheme
import com.example.customerspendingdashboard.core.model.Category
import com.example.customerspendingdashboard.feature.transactions.presentation.TransactionsContent
import com.example.customerspendingdashboard.feature.transactions.presentation.TransactionsUiState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TransactionsContentTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    // Tapping a category filter chip invokes the selection callback with that category.
    @Test
    fun selectingACategoryChipInvokesTheCallback() {
        var selected: Category? = null
        composeTestRule.setContent {
            SpendingTheme {
                TransactionsContent(
                    state = TransactionsUiState(isLoading = false),
                    onQueryChanged = {},
                    onCategorySelected = { selected = it },
                    onRangeSelected = {},
                    onRetry = {},
                    onTransactionClick = {},
                )
            }
        }

        composeTestRule.onNodeWithText(Category.DINING.displayName).performClick()

        assertEquals(Category.DINING, selected)
    }
}
