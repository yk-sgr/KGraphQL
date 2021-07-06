package com.apurebase.kgraphql.specification.v2016.typesystem

import com.apurebase.kgraphql.KGraphQL
import com.apurebase.kgraphql.Specification
import com.apurebase.kgraphql.extract
import com.apurebase.kgraphql.GraphQLError
import kotlinx.serialization.json.*
import org.amshove.kluent.*
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test

@Specification("3.1.7 Lists")
class ListsSpecificationTest{

    @Test
    fun `list arguments are valid`(){
        val schema = KGraphQL.schema {
            query("list"){
                resolver{ list: Iterable<String> -> list }
            }
        }

        val variables = buildJsonObject {
            put("list", buildJsonArray {
                add("GAGA")
                add("DADA")
                add("PADA")
            })
        }.toString()

        val response = schema.executeBlocking("query(\$list: [String!]!){list(list: \$list)}", variables)
        assertThat(response.extract<String>("data/list[0]"), equalTo("GAGA"))
        assertThat(response.extract<String>("data/list[1]"), equalTo("DADA"))
        assertThat(response.extract<String>("data/list[2]"), equalTo("PADA"))
    }

    @Test
    fun `lists with nullable entries are valid`(){
        val schema = KGraphQL.schema {
            query("list"){
                resolver{ list: Iterable<String?> -> list }
            }
        }

        val variables = buildJsonObject {
            put("list", buildJsonArray {
                add("GAGA")
                add(JsonNull)
                add("DADA")
                add("PADA")
            })
        }.toString()

        val response = schema.executeBlocking("query(\$list: [String!]!){list(list: \$list)}", variables)
        assertThat(response.extract<String>("data/list[1]"), nullValue())
    }

    @Test
    fun `lists with non-nullable entries should not accept list with null element`(){
        val schema = KGraphQL.schema {
            query("list"){
                resolver{ list: Iterable<String> -> list }
            }
        }

        val variables = buildJsonObject {
            put("list", buildJsonArray {
                add("GAGA")
                add(JsonNull)
                add("DADA")
                add("PADA")
            })
        }.toString()

        invoking {
            schema.executeBlocking("query(\$list: [String!]!){list(list: \$list)}", variables)
        } shouldThrow GraphQLError::class with {
            println(prettyPrint())
            message shouldBeEqualTo "Invalid argument value [GAGA, null, DADA, PADA] from variable \$list, " +
                    "expected list with non null arguments"
        }
    }

    @Test
    fun `by default coerce single element input as collection`(){
        val schema = KGraphQL.schema {
            query("list"){
                resolver{ list: Iterable<String> -> list }
            }
        }


        val variables = buildJsonObject {
            put("list", "GAGA")
        }.toString()

        val response = schema.executeBlocking("query(\$list: [String!]!){list(list: \$list)}", variables)
        assertThat(response.extract<String>("data/list[0]"), equalTo("GAGA"))
    }

    @Test
    fun `null value is not coerced as single element collection`(){
        val schema = KGraphQL.schema {
            query("list"){
                resolver{ list: Iterable<String>? -> list }
            }
        }


        val variables = buildJsonObject {
            put("list", JsonNull)
        }.toString()

        val response = schema.executeBlocking("query(\$list: [String!]!){list(list: \$list)}", variables)
        assertThat(response.extract<String>("data/list"), nullValue())
    }

    @Test
    fun `list argument can be declared non-nullable`(){
        val schema = KGraphQL.schema {
            query("list"){
                resolver{ list: Iterable<String> -> list }
            }
        }

        val variables = buildJsonObject {
            put("list", buildJsonArray {
                add("GAGA")
                add("DADA")
                add("PADA")
            })
        }.toString()

        val response = schema.executeBlocking("query(\$list: [String!]!){list(list: \$list)}", variables)
        assertThat(response.extract<Any>("data/list"), notNullValue())
    }

    @Test
    fun `Iterable implementations are treated as list`(){

        fun getResult() : Iterable<String> = listOf("POTATO", "BATATO", "ROTATO")

        val schema = KGraphQL.schema {
            query("list"){
                resolver { -> getResult() }
            }
        }

        val response = schema.executeBlocking("{list}")
        assertThat(response.extract<Iterable<String>>("data/list"), equalTo(getResult()))
    }
}
