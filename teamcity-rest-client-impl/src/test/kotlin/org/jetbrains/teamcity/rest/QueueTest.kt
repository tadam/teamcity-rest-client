package org.jetbrains.teamcity.rest

import org.junit.Before
import org.junit.Test
import kotlinx.coroutines.experimental.runBlocking

class QueueTest {
    @Before
    fun setupLog4j() {
        setupLog4jDebug()
    }

    @Test
    fun test_all() = runBlocking<Unit> {
        publicInstance().queuedBuilds().forEach {
            it.toString() // toString prints all properties thus evaluating them
            println(it)
        }
    }

    @Test
    fun test_kotlin_dev() = runBlocking<Unit> {
        publicInstance().queuedBuilds(ProjectId("Kotlin_dev")).forEach {
            it.toString() // toString prints all properties thus evaluating them
            println(it)
        }
    }
}