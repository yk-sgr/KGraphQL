package com.apurebase.kgraphql

import com.apurebase.kgraphql.schema.DefaultSchema
import com.apurebase.kgraphql.schema.dsl.SchemaBuilder
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.serialization.json.*
import org.hamcrest.FeatureMatcher
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.hamcrest.Matchers.instanceOf
import java.io.File


@Suppress("UNCHECKED_CAST")
inline fun <reified T> GraphQLExecutionResult.extract(path: String) : T {
    val tokens = path.trim().split('/').filter(String::isNotBlank)
    try {
        return tokens.fold(buildJsonObject { put("data", data) } as JsonElement) { workingMap, token ->
            when (workingMap) {
                is JsonObject -> if(token.contains('[')) {
                    val list = workingMap[token.substringBefore('[')]!!.jsonArray
                    val index = token.substring(token.indexOf('[')+1, token.length -1).toInt()
                    list[index]
                } else {
                    workingMap[token] ?: throw IllegalArgumentException("token: '$token' not found in object '$workingMap'")
                }
                else -> throw IllegalArgumentException("Path: $path is trying to nest further down in object tree than allowed")
            }
        }.let { jacksonObjectMapper().readValue(it.toString()) }
    } catch (e : Exception) {
        e.printStackTrace()
        throw IllegalArgumentException("Path: $path does not exist in map: $this", e)
    }
}

inline fun <reified T> GraphQLExecutionResult.extractOrNull(path : String) : T? {
    return try {
        extract(path)
    } catch (e: IllegalArgumentException){
        println("CATCHT")
        null
    }
}

fun defaultSchema(block: SchemaBuilder.() -> Unit): DefaultSchema {
    return SchemaBuilder().apply(block).build() as DefaultSchema
}

fun assertNoErrors(map: GraphQLExecutionResult) {
    if (map.errors != null) throw AssertionError("Errors encountered: ${map.errors}")
}

inline fun <reified T: Exception> expect(message: String? = null, block: () -> Unit){
    try {
        block()
        throw AssertionError("No exception caught")
    } catch (e : Exception){
        assertThat(e, instanceOf(T::class.java))
        if(message != null){
            assertThat(e, ExceptionMessageMatcher(message))
        }
    }
}

//fun executeEqualQueries(schema: Schema, expected: Map<*, Map<*, *>>, vararg queries : String) {
//    queries.map { request ->
//        schema.executeBlocking(request)
//    }.forEach { map ->
//        expected["data"]!!.map { (key, value) ->
//            map.data[]
//        }
//        map.data.map { (key, value) ->
//
//        }
//        map.data shouldBeEqualTo expected
//    }
//}

class ExceptionMessageMatcher(message: String?)
    : FeatureMatcher<Exception, String>(Matchers.containsString(message), "exception message is", "exception message"){

    override fun featureValueOf(actual: Exception?): String? = actual?.message
}

@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
fun Any.getResourceAsFile(name: String): File = this::class.java.classLoader.getResource(name).toURI().let(::File)

object ResourceFiles {
    val kitchenSinkQuery = getResourceAsFile("kitchen-sink.graphql").readText()
}


const val d = '$'
