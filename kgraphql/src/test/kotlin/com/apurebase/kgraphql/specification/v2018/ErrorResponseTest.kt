package com.apurebase.kgraphql.specification.v2018

import com.apurebase.kgraphql.*
import com.apurebase.kgraphql.demo.*
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeNull
import org.junit.jupiter.api.Test

@Specification("7.1.2")
class ErrorResponseTest {

    data class Hero(val id: String, val friendIds: List<String> = emptyList())

    val names = mapOf<String, String>(
        "2001" to "R2-D2",
        "1002" to "Luke Skywalker",
        "1003" to "Leia Organa",
    )

    val r2d2 = Hero("2001", listOf("1000", "1002", "1003"))

    val schema = defaultSchema {
        query("hero") {
            resolver { -> r2d2 }
        }
        type<Hero> {
            Hero::friendIds.ignore()
            property<String?>("name") {
                resolver { hero ->
                    names.getValue(hero.id)
                }
            }

            property<List<Hero>>("friends") {
                resolver { hero ->
                    hero.friendIds.map { Hero(it) }
                }
            }
        }
    }

    @Test
    fun example184() {
        schema.executeBlocking("""
            {
              hero {
                name
                heroFriends: friends {
                  id
                  name
                }
              }
            }
        """.trimIndent(), """{"episode": "NEWHOPE"}""").run {

            println("results $this")

            // Errors
            extract<String>("errors[0]/message") shouldBeEqualTo "Name for character with ID 1002 could not be fetched."
            extract<Int>("errors[0]/locations[0]/line") shouldBeEqualTo 6
            extract<Int>("errors[0]/locations[0]/column") shouldBeEqualTo 7
            extract<String>("errors[0]/path[0]") shouldBeEqualTo "hero"
            extract<String>("errors[0]/path[1]") shouldBeEqualTo "heroFriends"
            extract<Int>("errors[0]/path[2]") shouldBeEqualTo 1
            extract<String>("errors[0]/path[3]") shouldBeEqualTo "name"

            // Data
            extract<String>("data/hero/name") shouldBeEqualTo "R2-D2"
            extract<String>("data/hero/heroFriends[0]/id") shouldBeEqualTo "1000"
            extract<String>("data/hero/heroFriends[0]/name") shouldBeEqualTo "Luke Skywalker"
            extract<String>("data/hero/heroFriends[1]/id") shouldBeEqualTo "1002"
            extractOrNull<String>("data/hero/heroFriends[1]/name").shouldBeNull()
            extract<String>("data/hero/heroFriends[2]/id") shouldBeEqualTo "1003"
            extract<String>("data/hero/heroFriends[2]/name") shouldBeEqualTo "Leia Organa"
        }
    }

}
