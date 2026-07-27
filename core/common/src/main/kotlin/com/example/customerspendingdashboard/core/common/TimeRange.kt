package com.example.customerspendingdashboard.core.common

private const val WEEK_DAYS = 7
private const val MONTH_DAYS = 30
private const val QUARTER_DAYS = 90
private const val YEAR_DAYS = 365
private const val MONTHS_PER_YEAR = 12

/** Rolling window, evaluated relative to "today", used to bucket/filter transactions. */
enum class TimeRange(
    val displayLabel: String,
    val days: Int?,
) {
    WEEK("$WEEK_DAYS Days", WEEK_DAYS),
    MONTH("$MONTH_DAYS Days", MONTH_DAYS),
    QUARTER("$QUARTER_DAYS Days", QUARTER_DAYS),
    YEAR("$MONTHS_PER_YEAR Months", YEAR_DAYS),
    ALL("All Time", null),
}
