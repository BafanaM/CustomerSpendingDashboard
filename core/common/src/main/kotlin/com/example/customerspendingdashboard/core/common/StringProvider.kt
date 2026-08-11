package com.example.customerspendingdashboard.core.common

/**
 * Seam over [android.content.Context.getString] so ViewModels resolve string resources without
 * holding an Android [android.content.Context] (and tests stub a single method instead of
 * mocking Context).
 */
interface StringProvider {
    fun getString(resId: Int): String
}
