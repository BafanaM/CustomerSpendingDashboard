package com.example.customerspendingdashboard.core.testing

import com.example.customerspendingdashboard.core.common.StringProvider

/** Test [StringProvider] that echoes the resource id as a string, avoiding any Android dependency. */
class FakeStringProvider : StringProvider {
    override fun getString(resId: Int): String = "string-$resId"
}
