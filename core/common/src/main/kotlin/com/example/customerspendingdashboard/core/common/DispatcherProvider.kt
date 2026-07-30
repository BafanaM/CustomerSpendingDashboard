package com.example.customerspendingdashboard.core.common

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * Seam over [Dispatchers] so use cases/repositories never reference them directly,
 * letting tests substitute a single test dispatcher for all three.
 */
interface DispatcherProvider {
    val main: CoroutineDispatcher
    val io: CoroutineDispatcher
    val default: CoroutineDispatcher
}

/** The real [DispatcherProvider] backing the app at runtime — delegates to [Dispatchers] directly. */
class DefaultDispatcherProvider : DispatcherProvider {
    override val main: CoroutineDispatcher = Dispatchers.Main
    override val io: CoroutineDispatcher = Dispatchers.IO
    override val default: CoroutineDispatcher = Dispatchers.Default
}
