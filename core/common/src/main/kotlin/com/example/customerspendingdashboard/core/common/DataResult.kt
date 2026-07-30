package com.example.customerspendingdashboard.core.common

/**
 * Outcome of a one-shot operation (e.g. a sync/refresh). Continuous reads are modelled as
 * plain [kotlinx.coroutines.flow.Flow]s instead — loading state belongs to the UI layer, not
 * the repository, since a Flow of local data doesn't "fail" the way a network call can.
 */
sealed interface DataResult<out T> {
    /** The operation completed and produced [data]. */
    data class Success<out T>(
        val data: T,
    ) : DataResult<T>

    /** The operation failed with [throwable]. */
    data class Error(
        val throwable: Throwable,
    ) : DataResult<Nothing>
}

// Runs [block], wrapping a successful return in [DataResult.Success] and any thrown exception in [DataResult.Error].
@Suppress("TooGenericExceptionCaught", "InstanceOfCheckForException")
inline fun <T> runCatchingResult(block: () -> T): DataResult<T> =
    try {
        DataResult.Success(block())
    } catch (t: Throwable) {
        // Coroutine cancellation must always propagate, everything else becomes an Error result.
        if (t is kotlinx.coroutines.CancellationException) throw t
        DataResult.Error(t)
    }
