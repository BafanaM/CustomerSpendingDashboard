package com.example.customerspendingdashboard.feature.transactions.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.customerspendingdashboard.core.designsystem.theme.LocalSpendingSemanticColors
import com.example.customerspendingdashboard.core.model.Transaction
import com.example.customerspendingdashboard.core.model.TransactionType

private val CARD_SHAPE = RoundedCornerShape(16.dp)

// A single clickable transaction card: merchant/category on the left, signed amount on the right.
@Composable
fun TransactionRow(
    transaction: Transaction,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val semanticColors = LocalSpendingSemanticColors.current
    val amountColor = if (transaction.type == TransactionType.CREDIT) semanticColors.income else semanticColors.spend
    val sign = if (transaction.type == TransactionType.CREDIT) "+" else "-"

    Card(
        onClick = onClick,
        modifier =
            modifier
                .fillMaxWidth()
                .semantics { contentDescription = "${transaction.merchant}, ${transaction.amount.format()}" },
        shape = CARD_SHAPE,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(text = transaction.merchant, style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = transaction.category.displayName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = "$sign${transaction.amount.format()}",
                style = MaterialTheme.typography.bodyLarge,
                color = amountColor,
            )
        }
    }
}
