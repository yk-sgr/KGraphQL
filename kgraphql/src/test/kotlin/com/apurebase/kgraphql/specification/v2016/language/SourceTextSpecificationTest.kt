package com.apurebase.kgraphql.specification.v2016.language

import com.apurebase.kgraphql.*
import com.apurebase.kgraphql.Actor
import com.apurebase.kgraphql.GraphQLError
import org.amshove.kluent.*
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test

@Specification("2.1. Source Text")
class SourceTextSpecificationTest {

    val schema = defaultSchema {
        query("fizz") {
            resolver{ -> "buzz"}
        }

        query("actor") {
            resolver { -> Actor("Bogusław Linda", 65) }
        }
    }

    @Test
    fun `invalid unicode character`() {
        expect<GraphQLError>("Syntax Error: Cannot contain the invalid character \"\\u0003\"."){
            schema.executeBlocking("\u0003")
        }
    }

    @Test
    @Specification("2.1.1 Unicode")
    fun `ignore unicode BOM character`() {
        val map = schema.executeBlocking("\uFEFF{fizz}")
        assertNoErrors(map)
        assertThat(map.extract<String>("data/fizz"), equalTo("buzz"))
    }

    @Test
    @Specification (
        "2.1.3 Line Terminators",
        "2.1.5 Insignificant Commas",
        "2.1.7 Ignored Tokens"
    )
    fun `ignore whitespace, line terminator, comma characters`(){
        listOf(
            schema.executeBlocking("{fizz \nactor,{,\nname}}\n"),
            schema.executeBlocking("{fizz \tactor,  \n,\n{name}}"),
            schema.executeBlocking("{fizz\n actor\n{name,\n\n\n}}"),
            schema.executeBlocking("{\n\n\nfizz, \nactor{,name\t}\t}"),
            schema.executeBlocking("{\nfizz, actor,\n{\nname\t}}"),
            schema.executeBlocking("{\nfizz, ,actor\n{\nname,\t}}"),
            schema.executeBlocking("{\nfizz ,actor\n{\nname,\t}}"),
            schema.executeBlocking("{\nfizz, actor\n{\nname\t}}"),
            schema.executeBlocking("{\tfizz actor\n{name}}"),
        ).map {
            it.extract<String>("data/fizz") shouldBeEqualTo "buzz"
            it.extract<String>("data/actor/name") shouldBeEqualTo "Bogusław Linda"
        }
    }

    @Test
    @Specification("2.1.4 Comments")
    fun `support comments`(){
        listOf(
            schema.executeBlocking("{fizz #FIZZ COMMENTS\nactor,{,\nname}}\n"),
            schema.executeBlocking("#FIZZ COMMENTS\n{fizz \tactor#FIZZ COMMENTS\n,  #FIZZ COMMENTS\n\n#FIZZ COMMENTS\n,\n{name}}"),
            schema.executeBlocking("{fizz\n actor\n{name,\n\n\n}}"),
            schema.executeBlocking("#FIZZ COMMENTS\n{\n\n\nfizz, \nactor{,name\t}\t}#FIZZ COMMENTS\n"),
            schema.executeBlocking("{\nfizz, actor,\n{\n#FIZZ COMMENTS\nname\t}}"),
            schema.executeBlocking("{\nfizz, ,actor\n{\nname,\t}}"),
            schema.executeBlocking("#FIZZ COMMENTS\n{\nfizz ,actor#FIZZ COMMENTS\n\n{\nname,\t}}"),
            schema.executeBlocking("{\nfizz,#FIZZ COMMENTS\n#FIZZ COMMENTS\n actor\n{\nname\t}}"),
            schema.executeBlocking("{\tfizz #FIZZ COMMENTS\nactor\n{name}#FIZZ COMMENTS\n}"),
        ).map {
            it.extract<String>("data/fizz") shouldBeEqualTo "buzz"
            it.extract<String>("data/actor/name") shouldBeEqualTo "Bogusław Linda"
        }
    }

    @Test
    @Specification("2.1.9 Names")
    fun `names are case sensitive`(){
        invoking {
            schema.executeBlocking("{FIZZ}")
        } shouldThrow GraphQLError::class withMessage "Property FIZZ on Query does not exist"

        invoking {
            schema.executeBlocking("{Fizz}")
        } shouldThrow GraphQLError::class withMessage "Property Fizz on Query does not exist"

        val mapLowerCase = schema.executeBlocking("{fizz}")
        assertNoErrors(mapLowerCase)
        assertThat(mapLowerCase.extract<String>("data/fizz"), equalTo("buzz"))
    }
}
