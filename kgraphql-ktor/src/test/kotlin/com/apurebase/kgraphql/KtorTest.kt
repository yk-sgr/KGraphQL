package com.apurebase.kgraphql

import com.apurebase.kgraphql.schema.dsl.SchemaBuilder
import io.ktor.http.*
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.basic
import io.ktor.server.testing.*


open class KtorTest {

    fun withServer(ctxBuilder: ContextBuilder.(ApplicationCall) -> Unit = {}, block: SchemaBuilder.() -> Unit): (String, Kraph.() -> Unit) -> String {
        return { type, kraph ->
            withTestApplication({
                install(Authentication) {
                    basic {
                        realm = "ktor"
                        validate {
                            KtorPluginTest.User(4, it.name)
                        }
                    }
                }
                install(GraphQL) {
                    context(ctxBuilder)
                    wrap { next ->
                        authenticate(optional = true) { next() }
                    }
                    schema(block)
                }
            }) {
                handleRequest {
                    uri = "graphql"
                    method = HttpMethod.Post
                    addHeader(HttpHeaders.ContentType, "application/json;charset=UTF-8")
                    setBody(when(type.toLowerCase().trim()) {
                        "query" -> graphqlQuery(kraph).build()
                        "mutation" -> graphqlMutation(kraph).build()
                        else -> throw TODO("$type is not a valid graphql operation type")
                    }.also(::println))
                }.response.content!!
            }
        }
    }
}
