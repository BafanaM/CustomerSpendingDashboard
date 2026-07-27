package com.example.customerspendingdashboard.core.testing

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext

/**
 * `@RegisterExtension val mainDispatcherExtension = MainDispatcherExtension()` swaps
 * [Dispatchers.Main] for a [TestDispatcher] around every test so ViewModels using
 * `viewModelScope` run synchronously under `runTest`. Defaults to an unconfined dispatcher
 * (rather than [kotlinx.coroutines.test.StandardTestDispatcher]) so it executes eagerly without
 * needing to be coordinated with `runTest`'s own virtual-time scheduler.
 *
 * [scheduler] is exposed separately because this dispatcher has its own [TestCoroutineScheduler],
 * distinct from the one backing `runTest`'s [kotlinx.coroutines.test.TestScope] — code that needs
 * to advance virtual time for work running on `viewModelScope` (e.g. a debounced `delay`) must
 * advance *this* scheduler, not the one implicit in `runTest { ... }`.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherExtension(
    private val dispatcher: TestDispatcher = UnconfinedTestDispatcher(),
) : BeforeEachCallback,
    AfterEachCallback {
    val scheduler: TestCoroutineScheduler get() = dispatcher.scheduler

    // Swaps in the test dispatcher as Dispatchers.Main before each test.
    override fun beforeEach(context: ExtensionContext) {
        Dispatchers.setMain(dispatcher)
    }

    // Restores the original Dispatchers.Main after each test.
    override fun afterEach(context: ExtensionContext) {
        Dispatchers.resetMain()
    }
}
