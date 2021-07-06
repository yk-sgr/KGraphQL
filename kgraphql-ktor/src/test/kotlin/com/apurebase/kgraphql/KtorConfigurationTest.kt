package com.apurebase.kgraphql

import io.ktor.application.install
import io.ktor.server.testing.withTestApplication
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeNull
import org.junit.jupiter.api.Test

class KtorConfigurationTest: KtorTest() {

    @Test
    fun `default configuration should`() {
        var checked = false
        withTestApplication({
            val config = install(GraphQL) {}
            checked = true
            config.schema.configuration.timeout.shouldBeNull()
        }) {}
        checked shouldBeEqualTo true
    }

    @Test
    fun `update configuration`() {
        var checked = false
        withTestApplication({
            val config = install(GraphQL) {
                timeout = 999
            }
            checked = true
            config.schema.configuration.timeout shouldBeEqualTo 999
        }) {}
        checked shouldBeEqualTo true
    }

}
