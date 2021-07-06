package com.apurebase.kgraphql.configuration

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.serialization.json.Json
import kotlin.reflect.KClass

data class SchemaConfiguration(
        //document parser caching mechanisms
        val useCachingDocumentParser: Boolean,
        val documentParserCacheMaximumSize: Long,
        val json: Json,
        val objectMapper: ObjectMapper,
        //execution
        val coroutineDispatcher: CoroutineDispatcher,

        val wrapErrors: Boolean,

        val timeout: Long?,
        val plugins: MutableMap<KClass<*>, Any>
) {

        @Suppress("UNCHECKED_CAST")
        operator fun <T: Any> get(type: KClass<T>) = plugins[type] as T?
}
