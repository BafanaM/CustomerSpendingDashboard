package com.example.customerspendingdashboard.core.model

/** A spending or income category, with the label shown in the UI. */
enum class Category(
    val displayName: String,
) {
    GROCERIES("Groceries"),
    TRANSPORT("Transport"),
    DINING("Dining Out"),
    SHOPPING("Shopping"),
    BILLS_UTILITIES("Bills & Utilities"),
    ENTERTAINMENT("Entertainment"),
    HEALTH("Health"),
    INCOME("Income"),
    TRANSFERS("Transfers"),
    OTHER("Other"),
}
