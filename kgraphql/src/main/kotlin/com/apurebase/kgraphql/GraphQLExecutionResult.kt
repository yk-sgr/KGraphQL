package com.apurebase.kgraphql

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject


data class GraphQLExecutionResult(
    val data: JsonObject,
    val errors: List<GraphQLError>?,
) {
    override fun toString() = buildJsonObject {
        put("data", data)
    }.toString()
}
