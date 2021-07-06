package com.apurebase.kgraphql.specification.v2016.language

import com.apurebase.kgraphql.*
import com.apurebase.kgraphql.Actor
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

@Specification("2.6 Arguments")
class ArgumentsSpecificationTest {
    val age = 432

    val schema = defaultSchema {

        query("actor") {
            resolver { -> Actor("Boguś Linda", age) }
        }

        type<Actor>{
            property<List<String>>("favDishes") {
                resolver { _: Actor, size: Int, prefix: String? ->
                    listOf("steak", "burger", "soup", "salad", "bread", "bird").let { dishes ->
                        if(prefix != null){
                            dishes.filter { it.startsWith(prefix) }
                        } else {
                            dishes
                        }
                    }.take(size)
                }
            }
            property<Int>("none") {
                resolver { actor -> actor.age }
            }
            property<Int>("one") {
                resolver {actor, one: Int -> actor.age + one }
            }
            property<Int>("two") {
                resolver { actor, one: Int, two: Int -> actor.age + one + two }
            }
            property<Int>("three") {
                resolver { actor, one: Int, two: Int, three: Int ->
                    actor.age + one + two + three
                }
            }
            property<Int>("four") {
                resolver { actor, one: Int, two: Int, three: Int, four: Int ->
                    actor.age + one + two + three + four
                }
            }
            property<Int>("five") {
                resolver { actor, one: Int, two: Int, three: Int, four: Int, five: Int ->
                    actor.age + one + two + three + four + five
                }
            }
        }
    }

    @Test
    fun `arguments are unordered`(){
        listOf(
            schema.executeBlocking("{actor{favDishes(size: 2, prefix: \"b\")}}"),
            schema.executeBlocking("{actor{favDishes(prefix: \"b\", size: 2)}}")
        ).map {
            it.extract<String>("data/actor/favDishes[0]") shouldBeEqualTo "burger"
            it.extract<String>("data/actor/favDishes[1]") shouldBeEqualTo "bread"
        }
    }

    @Test
    fun `many arguments can exist on given field`(){
        schema.executeBlocking("{actor{favDishes(size: 2, prefix: \"b\")}}").run {
            extract<List<String>>("data/actor/favDishes") shouldBeEqualTo listOf("burger", "bread")
        }
    }

    @Test
    fun `all arguments to suspendResolvers`() {
        val request = """
            {
                actor {
                    none
                    one(one: 1)
                    two(one: 2, two: 3)
                    three(one: 4, two: 5, three: 6)
                    four(one: 7, two: 8, three: 9, four: 10)
                    five(one: 11, two: 12, three: 13, four: 14, five: 15)
                }
            }
        """.trimIndent()
        schema.executeBlocking(request).run {
            extract<Int>("data/actor/none") shouldBeEqualTo age
            extract<Int>("data/actor/one") shouldBeEqualTo age + 1
            extract<Int>("data/actor/two") shouldBeEqualTo age + 2 + 3
            extract<Int>("data/actor/three") shouldBeEqualTo age + 4 + 5 + 6
            extract<Int>("data/actor/four") shouldBeEqualTo age + 7 + 8 + 9 + 10
            extract<Int>("data/actor/five") shouldBeEqualTo age + 11 + 12 + 13 + 14 + 15
        }
    }

    @Test
    fun `property arguments should accept default values`() {
        val schema = defaultSchema {
            query("actor") {
                resolver {
                    -> Actor("John Doe", age)
                }
            }

            type<Actor> {
                property<String>("greeting") {
                    resolver { actor: Actor, suffix: String ->
                        "$suffix, ${actor.name}!"
                    }.withArgs {
                        arg<String> { name = "suffix"; defaultValue = "Hello" }
                    }
                }
            }
        }

        val request = """
            {
                actor {
                    greeting
                }
            }
        """.trimIndent()

        schema.executeBlocking(request).run {
            extract<String>("data/actor/greeting") shouldBeEqualTo "Hello, John Doe!"
        }
    }
}
