package com.apurebase.kgraphql.specification.v2016.typesystem

import com.apurebase.kgraphql.*
import com.apurebase.kgraphql.integration.BaseSchemaTest
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeNull
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test

@Specification("3.2 Directives")
class DirectivesSpecificationTest : BaseSchemaTest() {

    @Test
    fun `query with @include directive on field`(){
        val map = execute("{film{title, year @include(if: false)}}")
        assertThat(map.extractOrNull("data/film/year"), nullValue())
    }

    @Test
    fun `query with @skip directive on field`(){
        val map = execute("{film{title, year @skip(if: true)}}")
        assertThat(map.extractOrNull("data/film/year"), nullValue())
    }

    @Test
    fun `query with @include and @skip directive on field`(){
        val mapBothSkip = execute("{film{title, year @include(if: false) @skip(if: true)}}")
        assertThat(mapBothSkip.extractOrNull("data/film/year"), nullValue())

        val mapOnlySkip = execute("{film{title, year @include(if: true) @skip(if: true)}}")
        assertThat(mapOnlySkip.extractOrNull("data/film/year"), nullValue())

        val mapOnlyInclude = execute("{film{title, year @include(if: false) @skip(if: false)}}")
        assertThat(mapOnlyInclude.extractOrNull("data/film/year"), nullValue())

        val mapNeither = execute("{film{title, year @include(if: true) @skip(if: false)}}")
        assertThat(mapNeither.extractOrNull("data/film/year"), notNullValue())
    }

    @Test
    fun `query with @include and @skip directive on field object`() {
        val mapWithSkip = execute("{ number(big: true), film @skip(if: true) { title } }")
        mapWithSkip.extractOrNull<String?>("data/film").shouldBeNull()

        val mapWithoutSkip = execute("{ number(big: true), film @skip(if: false) { title } }")
        mapWithoutSkip.extract<String>("data/film/title") shouldBeEqualTo "Prestige"

        val mapWithInclude = execute("{ number(big: true), film @include(if: true) { title } }")
        mapWithInclude.extract<String?>("data/film/title") shouldBeEqualTo "Prestige"

        val mapWithoutInclude = execute("{ number(big: true), film @include(if: false) { title } }")
        mapWithoutInclude.extractOrNull<String>("data/film").shouldBeNull()
    }

    @Test
    fun `query with @include directive on field with variable`(){
        val map = execute(
                "query film (\$include: Boolean!) {film{title, year @include(if: \$include)}}",
                "{\"include\":\"false\"}"
        )
        assertThat(map.extractOrNull("data/film/year"), nullValue())
    }
}
