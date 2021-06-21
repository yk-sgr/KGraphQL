package com.apurebase.kgraphql

import com.apurebase.kgraphql.schema.execution.Execution

data class ExecutionScope(
    val context: Context,
    val node: Execution,
)
