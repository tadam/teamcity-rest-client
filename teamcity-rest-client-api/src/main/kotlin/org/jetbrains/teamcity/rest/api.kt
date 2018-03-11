package org.jetbrains.teamcity.rest

import java.io.File
import java.util.*

abstract class TeamCityInstance {
    abstract fun withLogResponses(): TeamCityInstance

    abstract fun builds(): BuildLocator
    abstract suspend fun queuedBuilds(projectId: ProjectId? = null): List<QueuedBuild>

    abstract suspend fun build(id: BuildId): Build
    abstract suspend fun build(buildType: BuildConfigurationId, number: String): Build?
    abstract suspend fun buildConfiguration(id: BuildConfigurationId): BuildConfiguration
    abstract fun vcsRoots(): VcsRootLocator
    abstract suspend fun vcsRoot(id: VcsRootId): VcsRoot
    abstract suspend fun project(id: ProjectId): Project
    abstract suspend fun rootProject(): Project

    abstract fun getWebUrl(projectId: ProjectId, branch: String? = null): String
    abstract fun getWebUrl(buildConfigurationId: BuildConfigurationId, branch: String? = null): String
    abstract fun getWebUrl(buildId: BuildId): String
    abstract fun getWebUrl(queuedBuildId: QueuedBuildId): String
    abstract fun getWebUrl(changeId: ChangeId, specificBuildConfigurationId: BuildConfigurationId? = null, includePersonalBuilds: Boolean? = null): String

    companion object {
        private const val factoryFQN = "org.jetbrains.teamcity.rest.TeamCityInstanceFactory"

        @JvmStatic
        @Deprecated("Use [TeamCityInstanceFactory] class instead", ReplaceWith("TeamCityInstanceFactory.guestAuth(serverUrl)", factoryFQN))
        fun guestAuth(serverUrl: String): TeamCityInstance = TeamCityInstance::class.java.classLoader
                .loadClass(factoryFQN)
                .getMethod("guestAuth", String::class.java)
                .invoke(null, serverUrl) as TeamCityInstance

        @JvmStatic
        @Deprecated("Use [TeamCityInstanceFactory] class instead", ReplaceWith("TeamCityInstanceFactory.httpAuth(serverUrl, username, password)", factoryFQN))
        fun httpAuth(serverUrl: String, username: String, password: String): TeamCityInstance
                = TeamCityInstance::class.java.classLoader
                .loadClass(factoryFQN)
                .getMethod("httpAuth", String::class.java, String::class.java, String::class.java)
                .invoke(null, serverUrl, username, password) as TeamCityInstance
    }
}

interface VcsRootLocator {
    suspend fun list(): List<VcsRoot>
}

interface BuildLocator {
    fun fromConfiguration(buildConfigurationId: BuildConfigurationId): BuildLocator

    /**
     * By default only successful builds are returned, call this method to include failed builds as well.
     */
    fun withAnyStatus(): BuildLocator

    fun withStatus(status: BuildStatus): BuildLocator
    fun withTag(tag: String): BuildLocator

    fun withBranch(branch: String): BuildLocator

    /**
     * By default only builds from the default branch are returned, call this method to include builds from all branches.
     */
    fun withAllBranches(): BuildLocator

    fun pinnedOnly(): BuildLocator

    fun runningOnly(): BuildLocator

    fun limitResults(count: Int): BuildLocator
    
    fun sinceDate(date: Date) : BuildLocator

    suspend fun latest(): Build?
    suspend fun list(): List<Build>
}

data class ProjectId(val stringId: String)

data class BuildId(val stringId: String)

data class QueuedBuildId(val stringId: String)

data class ChangeId(val stringId: String)

data class BuildConfigurationId(val stringId: String)

data class VcsRootId(val stringId: String)

interface Project {
    val id: ProjectId
    val name: String
    val archived: Boolean
    val parentProjectId: ProjectId

    /**
     * Web UI URL for user, especially useful for error and log messages
     */
    fun getWebUrl(branch: String? = null): String

    fun fetchChildProjects(): List<Project>
    fun fetchBuildConfigurations(): List<BuildConfiguration>
    fun fetchParameters(): List<Parameter>

    suspend fun setParameter(name: String, value: String)
}

interface BuildConfiguration {
    val id: BuildConfigurationId
    val name: String
    val projectId: ProjectId
    val paused: Boolean

    /**
     * Web UI URL for user, especially useful for error and log messages
     */
    fun getWebUrl(branch: String? = null): String

    suspend fun fetchBuildTags(): List<String>

    suspend fun fetchFinishBuildTriggers(): List<FinishBuildTrigger>

    suspend fun fetchArtifactDependencies(): List<ArtifactDependency>

    suspend fun setParameter(name: String, value: String)
}

interface Parameter {
    val name: String
    val value: String?
    val own: Boolean
}

interface Branch {
    val name: String?
    val isDefault: Boolean
}

interface Build {
    val id: BuildId
    val buildTypeId: BuildConfigurationId
    val buildNumber: String
    val status: BuildStatus
    val branch: Branch

    /**
     * Web UI URL for user, especially useful for error and log messages
     */
    fun getWebUrl(): String

    fun fetchStatusText(): String
    fun fetchQueuedDate(): Date
    fun fetchStartDate(): Date
    fun fetchFinishDate(): Date

    fun fetchParameters(): List<Parameter>

    fun fetchRevisions(): List<Revision>

    suspend fun fetchChanges(): List<Change>

    fun fetchPinInfo(): PinInfo?

    fun fetchTriggeredInfo(): TriggeredInfo?

    suspend fun addTag(tag: String)
    suspend fun pin(comment: String = "pinned via REST API")
    suspend fun unpin(comment: String = "unpinned via REST API")
    suspend fun getArtifacts(parentPath: String = ""): List<BuildArtifact>
    suspend fun findArtifact(pattern: String, parentPath: String = ""): BuildArtifact
    suspend fun downloadArtifacts(pattern: String, outputDir: File)
    suspend fun downloadArtifact(artifactPath: String, output: File)
}

interface QueuedBuild {
    val id: QueuedBuildId
    val buildTypeId: BuildConfigurationId
    val status: QueuedBuildStatus
    val branch : Branch

    fun getWebUrl(): String
}

enum class QueuedBuildStatus {
    QUEUED,
    FINISHED
}

interface Change {
    val id: ChangeId
    val version: String
    val username: String
    val user: User?
    val date: Date
    val comment: String

    /**
     * Web UI URL for user, especially useful for error and log messages
     */
    fun getWebUrl(specificBuildConfigurationId: BuildConfigurationId? = null, includePersonalBuilds: Boolean? = null): String
}

interface User {
    val id: String
    val username: String
    val name: String
}

interface BuildArtifact {
    val fileName: String
    val size: Long?
    val modificationTime: Date

    suspend fun download(output: File)
}

interface VcsRoot {
    val id: VcsRootId
    val name: String
}

enum class BuildStatus {
    SUCCESS,
    FAILURE,
    ERROR
}

interface PinInfo {
    val user: User
    val time: Date
}

interface Revision {
    val version: String
    val vcsBranchName: String
    val vcsRoot: VcsRoot
}

interface TriggeredInfo {
    val user: User?
    val build: Build?
}

interface FinishBuildTrigger {
    val initiatedBuildConfiguration: BuildConfigurationId
    val afterSuccessfulBuildOnly: Boolean
    val includedBranchPatterns: Set<String>
    val excludedBranchPatterns: Set<String>
}

interface ArtifactDependency {
    val dependsOnBuildConfiguration: BuildConfiguration
    val branch: String?
    val artifactRules: List<ArtifactRule>
    val cleanDestinationDirectory: Boolean
}

interface ArtifactRule {
    val include: Boolean
    /**
     * Specific file, directory, or wildcards to match multiple files can be used. Ant-like wildcards are supported.
     */
    val sourcePath: String
    /**
     * Follows general rules for sourcePath: ant-like wildcards are allowed.
     */
    val archivePath: String?
    /**
     * Destination directory where files are to be placed.
     */
    val destinationPath: String?
}
