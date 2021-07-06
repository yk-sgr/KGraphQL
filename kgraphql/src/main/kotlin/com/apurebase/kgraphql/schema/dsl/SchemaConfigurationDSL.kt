package com.apurebase.kgraphql.schema.dsl

import com.apurebase.kgraphql.configuration.PluginConfiguration
import com.apurebase.kgraphql.configuration.SchemaConfiguration
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonBuilder
import kotlin.reflect.KClass

open class SchemaConfigurationDSL {
    var useDefaultPrettyPrinter: Boolean = false
    var useCachingDocumentParser: Boolean = true
    var configureJson: JsonBuilder.() -> Unit = {}
    var documentParserCacheMaximumSize: Long = 1000L
    var coroutineDispatcher: CoroutineDispatcher = Dispatchers.Default
    var wrapErrors: Boolean = true
    var timeout: Long? = null


    val objectMapper = jacksonObjectMapper().apply {
        configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
    }

    internal val scalarSerializers = mutableListOf<JsonBuilder.() -> Unit>()

    private val plugins: MutableMap<KClass<*>, Any> = mutableMapOf()

    fun install(plugin: PluginConfiguration) {
        val kClass = plugin::class
        require(plugins[kClass] == null)
        plugins[kClass] = plugin
    }


    internal fun update(block: SchemaConfigurationDSL.() -> Unit) = block()
    internal fun build(): SchemaConfiguration {
        return SchemaConfiguration(
            useCachingDocumentParser,
            documentParserCacheMaximumSize,
            Json {
                isLenient = true
                scalarSerializers.map { it() }
                prettyPrint = useDefaultPrettyPrinter
                configureJson()
             },
            objectMapper,
            coroutineDispatcher,
            wrapErrors,
            timeout,
            plugins
        )
    }
}
