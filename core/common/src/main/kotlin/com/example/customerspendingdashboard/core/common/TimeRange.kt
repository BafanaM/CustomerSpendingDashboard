package com.example.customerspendingdashboard.core.common

/** Rolling window, evaluated relative to "today", used to bucket/filter transactions. */
enum class TimeRange(
    val displayLabel: String,
    val days: Int?,
) {
    WEEK("7 Days", 7),
    MONTH("30 Days", 30),
    QUARTER("90 Days", 90),
    YEAR("12 Months", 365),
    ALL("All Time", null),
}
