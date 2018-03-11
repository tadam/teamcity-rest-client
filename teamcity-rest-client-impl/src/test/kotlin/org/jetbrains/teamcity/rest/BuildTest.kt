package org.jetbrains.teamcity.rest

import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.util.*
import kotlin.test.assertEquals
import kotlinx.coroutines.experimental.runBlocking


class BuildTest {
    @Before
    fun setupLog4j() {
        setupLog4jDebug()
    }

    @Test
    fun test_to_string() = runBlocking<Unit> {
        val builds = publicInstance().builds()
                .fromConfiguration(compileExamplesConfiguration)
                .limitResults(3)
                .list()

        println(builds.joinToString("\n"))
    }
    
    @Test
    fun since_date() = runBlocking<Unit> {
        val monthAgo = GregorianCalendar()
        monthAgo.add(Calendar.MONTH, -1)
        
        val builds = publicInstance().builds()
                .fromConfiguration(compileExamplesConfiguration)
                .limitResults(3)
                .sinceDate(monthAgo.time)
                .list()

        for (build in builds) {
            assert(build.fetchStartDate() >= monthAgo.time)
        }
    }

    @Test
    fun test_build_fetch_revisions() = runBlocking<Unit> {
        publicInstance().builds()
                .fromConfiguration(compileExamplesConfiguration)
                .limitResults(10)
                .list()
                .forEach {
                    val revisions = it.fetchRevisions()
                    Assert.assertTrue(revisions.isNotEmpty())
                }
    }

    @Test
    fun test_fetch_status() = runBlocking<Unit> {
        val build = publicInstance().builds()
                .fromConfiguration(compileExamplesConfiguration)
                .limitResults(1)
                .list().first()

        build.fetchStatusText()
    }

    @Test
    fun test_get_artifacts() = runBlocking<Unit> {
        val build = publicInstance().builds()
                .fromConfiguration(kotlinDevCompilerAllPlugins)
                .limitResults(1)
                .list().first()

        val artifacts = build.getArtifacts("internal")
        Assert.assertTrue(artifacts.any { it.fileName == "dependencies.properties"})
    }

    @Test
    fun test_get_webUrl() = runBlocking<Unit> {
        val build = publicInstance().builds()
                .fromConfiguration(compilerAndPluginConfiguration)
                .limitResults(1)
                .list().first()

        assertEquals(
                "$publicInstanceUrl/viewLog.html?tab=buildResultsDiv&buildId=${build.id.stringId}",
                build.getWebUrl()
        )
    }
}
