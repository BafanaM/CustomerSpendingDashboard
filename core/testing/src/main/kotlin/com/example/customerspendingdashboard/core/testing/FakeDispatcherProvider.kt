package com.example.customerspendingdashboard.core.testing

import com.example.customerspendingdashboard.core.common.DispatcherProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher

// Test [DispatcherProvider] backing every dispatcher with the same single test dispatcher.
@OptIn(ExperimentalCoroutinesApi::class)
class FakeDispatcherProvider(
    dispatcher: CoroutineDispatcher = UnconfinedTestDispatcher(),
) : DispatcherProvider {
    override val main: CoroutineDispatcher = dispatcher
    override val io: CoroutineDispatcher = dispatcher
    override val default: CoroutineDispatcher = dispatcher
}
