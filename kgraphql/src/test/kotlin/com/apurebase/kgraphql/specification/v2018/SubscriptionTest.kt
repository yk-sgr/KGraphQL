package com.apurebase.kgraphql.specification.v2018

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import org.junit.jupiter.api.Test

class SubscriptionTest {

    @Test
    fun flowExample() {
        runBlocking {
            println("Getting flow")
            val flow = MutableSharedFlow<Int>()

            println("start broad casting")
            launch {
                repeat(500) {
                    delay(500L - it)
                    flow.emit(it)
                }
            }


            launch {
                println("l1 collecting")
                flow.collect {
                    println("l1 -> $it")
                }
            }
            launch {
                println("l2 collecting")
                flow.collect {
                    println("l2 -> $it")
                }
            }

        }

    }

}
