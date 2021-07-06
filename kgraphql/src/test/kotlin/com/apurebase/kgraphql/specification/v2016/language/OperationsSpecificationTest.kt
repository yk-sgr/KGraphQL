package com.apurebase.kgraphql.specification.v2016.language

import com.apurebase.kgraphql.*
import com.apurebase.kgraphql.schema.SchemaException
import com.apurebase.kgraphql.schema.dsl.operations.subscribe
import com.apurebase.kgraphql.schema.dsl.operations.unsubscribe
import org.amshove.kluent.*
import org.junit.jupiter.api.Test

data class Actor(var name : String? = "", var age: Int? = 0)
data class Actress(var name : String? = "", var age: Int? = 0)

@Specification("2.3 Operations")
class OperationsSpecificationTest {

    var subscriptionResult = ""

    fun newSchema() = defaultSchema {
        query("fizz") {
            resolver{ -> "buzz"}.withArgs {  }
        }

        val publisher = mutation("createActor") {
            resolver { name : String -> Actor(name, 11) }
        }

        subscription("subscriptionActor") {
            resolver { subscription: String ->
                subscribe(subscription, publisher, Actor()) {
                    subscriptionResult = it
                    println(it)
                }
            }
        }

        subscription("unsubscriptionActor") {
            resolver { subscription: String ->
                unsubscribe(subscription, publisher, Actor())
            }
        }

        subscription("subscriptionActress") {
            resolver { subscription: String ->
                subscribe(subscription, publisher, Actress()) {
                    subscriptionResult = it
                }
            }
        }
    }

    @Test
    fun `unnamed and named queries are equivalent`(){
        listOf(
            newSchema().executeBlocking("{fizz}"),
            newSchema().executeBlocking("query {fizz}"),
            newSchema().executeBlocking("query BUZZ {fizz}")
        ).map {
            it.extract<String>("data/fizz") shouldBeEqualTo "buzz"
        }
    }

    @Test
    fun `unnamed and named mutations are equivalent`(){
        val res1 = newSchema().executeBlocking("mutation {createActor(name : \"Kurt Russel\"){name}}")
        val res2 = newSchema().executeBlocking("mutation KURT {createActor(name : \"Kurt Russel\"){name}}")

        listOf(res1, res2).map {
            it.extract<String>("data/createActor/name") shouldBeEqualTo "Kurt Russel"
        }

    }

    @Test
    fun `handle subscription`(){
        val schema = newSchema()
        schema.executeBlocking("subscription {subscriptionActor(subscription : \"mySubscription\"){name}}")

        subscriptionResult = ""
        schema.executeBlocking("mutation {createActor(name : \"Kurt Russel\"){name}}")

        subscriptionResult shouldBeEqualTo "{\"data\":{\"name\":\"Kurt Russel\"}}"

        subscriptionResult = ""
        schema.executeBlocking("mutation{createActor(name : \"Kurt Russel1\"){name}}")
        subscriptionResult shouldBeEqualTo "{\"data\":{\"name\":\"Kurt Russel1\"}}"

        subscriptionResult = ""
        schema.executeBlocking("mutation{createActor(name : \"Kurt Russel2\"){name}}")
        subscriptionResult shouldBeEqualTo "{\"data\":{\"name\":\"Kurt Russel2\"}}"

        schema.executeBlocking("subscription {unsubscriptionActor(subscription : \"mySubscription\"){name}}")

        subscriptionResult = ""
        schema.executeBlocking("mutation{createActor(name : \"Kurt Russel\"){name}}")
        subscriptionResult shouldBeEqualTo ""

    }

    @Test
    fun `Subscription return type must be the same as the publisher's`(){
        invoking {
            newSchema().executeBlocking("subscription {subscriptionActress(subscription : \"mySubscription\"){age}}")
        } shouldThrow GraphQLError::class with {
            originalError shouldBeInstanceOf SchemaException::class
            message shouldBeEqualTo "Subscription return type must be the same as the publisher's"
        }
    }
}

