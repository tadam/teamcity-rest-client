package org.jetbrains.teamcity.rest

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import kotlinx.coroutines.experimental.runBlocking


class ProjectTest {
    @Before
    fun setupLog4j() {
        setupLog4jDebug()
    }

    @Test
    fun `project by id`() = runBlocking<Unit> {
        val project = publicInstance().project(kotlinProject)
        assertEquals(kotlinProject, project.id)
        assertEquals("Kotlin", project.name)
        assertFalse(project.archived)
    }

    @Test
    fun `build configuration by id`() = runBlocking<Unit> {
        val configuration = publicInstance().buildConfiguration(compileExamplesConfiguration)
        assertEquals(compileExamplesConfiguration, configuration.id)
        assertEquals("Compile Kotlin examples", configuration.name)
        assertEquals(kotlinProject, configuration.projectId)
    }

    @Test
    fun `webUrl with default parameters`() = runBlocking<Unit> {
        val proj = publicInstance().project(kotlinProject)
        kotlin.test.assertEquals(
                "$publicInstanceUrl/project.html?projectId=${kotlinProject.stringId}",
                proj.getWebUrl())
    }

    @Test
    fun `webUrl with branch`() = runBlocking<Unit> {
        val proj = publicInstance().project(kotlinProject)
        kotlin.test.assertEquals(
                "$publicInstanceUrl/project.html?projectId=${kotlinProject.stringId}&branch=%3Cdefault%3E",
                proj.getWebUrl(branch = "<default>"))
    }
}