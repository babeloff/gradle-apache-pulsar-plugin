
/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/**
 * This class is *HEAVILY* influenced by
 * https://github.com/apache/pulsar/blob/v2.4.2/pulsar-functions/localrun/src/main/java/org/apache/pulsar/functions/LocalRunner.java
 * Any changes to it should be reflected here.
 *
 * The main differences are
 * * conversion to Kotlin (automatic by jetbrains/idea)
 * * the removal of JCommander (this is not a standalone application)
 * * change System.getenv to System.getProperty
 *
 */

package org.example.gradle.api.pulsar

import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.thinkinglogic.builder.annotation.Builder
import com.thinkinglogic.builder.annotation.DefaultValue
import org.apache.pulsar.common.functions.FunctionConfig
import org.apache.pulsar.common.functions.Utils
import org.apache.pulsar.common.io.SinkConfig
import org.apache.pulsar.common.io.SourceConfig
import org.apache.pulsar.functions.instance.AuthenticationConfig
import org.apache.pulsar.functions.instance.InstanceConfig
import org.apache.pulsar.functions.proto.Function
import org.apache.pulsar.functions.runtime.ProcessRuntimeFactory
import org.apache.pulsar.functions.runtime.RuntimeSpawner
import org.apache.pulsar.functions.runtime.ThreadRuntimeFactory
import org.apache.pulsar.functions.secretsprovider.ClearTextSecretsProvider
import org.apache.pulsar.functions.secretsproviderconfigurator.DefaultSecretsProviderConfigurator
import org.apache.pulsar.functions.utils.FunctionCommon
import org.apache.pulsar.functions.utils.FunctionConfigUtils
import org.apache.pulsar.functions.utils.SinkConfigUtils
import org.apache.pulsar.functions.utils.SourceConfigUtils
import org.apache.pulsar.functions.utils.functioncache.FunctionCacheEntry
import org.apache.pulsar.functions.utils.io.ConnectorUtils
import org.apache.pulsar.functions.utils.io.Connectors
import org.gradle.api.logging.Logging
import java.io.File
import java.io.IOException
import java.nio.file.Paths
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.reflect.KClass

@Builder
class PulsarLocalRunner (
        @field:Parameter(
                names = ["--functionConfig"],
                description = "The json representation of FunctionConfig",
                hidden = true,
                converter = FunctionConfigConverter::class)
        val functionConfig: FunctionConfig?,

        @field:Parameter(
                names = ["--sourceConfig"],
                description = "The json representation of SourceConfig",
                hidden = true,
                converter = SourceConfigConverter::class)
        val sourceConfig: SourceConfig?,

        @field:Parameter(
                names = ["--sinkConfig"],
                description = "The json representation of SinkConfig",
                hidden = true,
                converter = SinkConfigConverter::class)
        val sinkConfig: SinkConfig?,

        @field:Parameter(
                names = ["--stateStorageServiceUrl"],
                description = "The URL for the state storage service (by default Apache BookKeeper)",
                hidden = true)
        val stateStorageServiceUrl: String?,

        @field:Parameter(
                names = ["--brokerServiceUrl"],
                description = "The URL for the Pulsar broker",
                hidden = true)
        @DefaultValue("pulsar://localhost:6650")
        val brokerServiceUrl: String = "pulsar://localhost:6650",

        @field:Parameter(
                names = ["--clientAuthPlugin"],
                description = "Client authentication plugin using which function-process can connect to broker",
                hidden = true)
        val clientAuthPlugin: String?,

        @field:Parameter(
                names = ["--clientAuthParams"],
                description = "Client authentication param",
                hidden = true)
        val clientAuthParams: String?,

        @field:Parameter(
                names = ["--useTls"],
                description = "Use tls connection\n",
                hidden = true,
                arity = 1)
        @DefaultValue("false")
        val useTls: Boolean = false,

        @field:Parameter(
                names = ["--tlsAllowInsecureConnection"],
                description = "Allow insecure tls connection\n",
                hidden = true,
                arity = 1)
        @DefaultValue("false")
        val tlsAllowInsecureConnection: Boolean = false,

        @field:Parameter(
                names = ["--tlsHostNameVerificationEnabled"],
                description = "Enable hostname verification",
                hidden = true,
                arity = 1)
        @DefaultValue("false")
        val tlsHostNameVerificationEnabled: Boolean = false,

        @field:Parameter(
                names = ["--tlsTrustCertFilePath"],
                description = "tls trust cert file path",
                hidden = true)
        val tlsTrustCertFilePath: String?,

        @field:Parameter(
                names = ["--instanceIdOffset"],
                description = "Start the instanceIds from this offset",
                hidden = true)
        @DefaultValue("10")
        val instanceIdOffset : Int = 10,

        @field:Parameter(
                names = ["--runtime"],
                description = "Function runtime to use (Thread/Process)",
                hidden = true,
                converter = RuntimeConverter::class)
        val runtimeEnv: RuntimeEnv?
        )

{
    private val running = AtomicBoolean(false)
    private val spawners: MutableList<RuntimeSpawner> = LinkedList()

    enum class RuntimeEnv
    {
        THREAD, PROCESS
    }

    @Target(AnnotationTarget.FIELD)
    @Retention(AnnotationRetention.SOURCE)
    @MustBeDocumented
    annotation class Parameter(
            val names: Array<String>,
            val description: String,
            val hidden: Boolean = false,
            val arity: Int = 0,
            val converter: KClass<*> = StringConverter::class)
    // val converter: KClass<IStringConverter<Any>> = StringConverter::class

    @Synchronized
    fun stop()
    {
        running.set(false)
        log.info("Shutting down the localrun runtimeSpawner ...")
        for (spawner in spawners)
        {
            spawner.close()
        }
        spawners.clear()
    }

    @Throws(Exception::class)
    fun start(blocking: Boolean)
    {
        val local: MutableList<RuntimeSpawner> = LinkedList()
        synchronized(this) {
            require(!running.get())
            { "Pulsar Function local run already started!" }

            val functionDetails: Function.FunctionDetails
            var userCodeFile: String?
            val parallelism: Int
            when
            {
                (functionConfig != null) ->
                {
                    FunctionConfigUtils.inferMissingArguments(functionConfig)
                    var classLoader = Thread.currentThread().contextClassLoader
                    parallelism = functionConfig.parallelism
                    when (functionConfig.runtime)
                    {
                        FunctionConfig.Runtime.JAVA ->
                        {
                            userCodeFile = functionConfig.jar
                            // if code file not specified try to get location of the code based on class.
                            if (userCodeFile == null && functionConfig.className != null)
                            {
                                userCodeFile = Thread.currentThread().contextClassLoader
                                        .loadClass(functionConfig.className)
                                        .protectionDomain.codeSource.location.file
                            }
                            classLoader = if (Utils.isFunctionPackageUrlSupported(userCodeFile))
                            {
                                val file = FunctionCommon.extractFileFromPkgURL(userCodeFile)
                                FunctionConfigUtils.validate(functionConfig, file)
                            }
                            else
                            {
                                if (userCodeFile == null)
                                {
                                    throw RuntimeException("User jar, being null, does not exist")
                                }
                                val file = File(userCodeFile)
                                if (!file.exists())
                                {
                                    throw RuntimeException("User jar does not exist")
                                }
                                FunctionConfigUtils.validate(functionConfig, file)
                            }
                        }
                        FunctionConfig.Runtime.GO ->
                        {
                            userCodeFile = functionConfig.go
                        }
                        FunctionConfig.Runtime.PYTHON ->
                        {
                            userCodeFile = functionConfig.py
                        }
                        else ->
                        {
                            throw UnsupportedOperationException()
                        }
                    }
                    functionDetails = FunctionConfigUtils.convert(functionConfig, classLoader)
                }
                (sourceConfig != null) ->
                {
                    Utils.inferMissingArguments(sourceConfig)
                    userCodeFile = sourceConfig.archive
                    // if code file not specified try to get location of the code based on class.
                    if (userCodeFile == null && sourceConfig.className != null)
                    {
                        userCodeFile = Thread.currentThread().contextClassLoader
                                .loadClass(sourceConfig.className)
                                .protectionDomain.codeSource.location.file
                    }
                    if (userCodeFile == null)
                    {
                        userCodeFile = Thread.currentThread().contextClassLoader
                                .loadClass(PulsarLocalRunner::class.java.name)
                                .protectionDomain.codeSource.location.file
                    }
                    val builtInSource = isBuiltInSource(userCodeFile)
                    if (builtInSource != null)
                    {
                        sourceConfig.archive = builtInSource
                    }
                    parallelism = sourceConfig.parallelism
                    functionDetails = if (Utils.isFunctionPackageUrlSupported(userCodeFile))
                    {
                        val file = FunctionCommon.extractFileFromPkgURL(userCodeFile)
                        SourceConfigUtils.convert(sourceConfig, SourceConfigUtils.validate(sourceConfig, null, file))
                    }
                    else
                    {
                        if (userCodeFile == null)
                        {
                            throw RuntimeException("Source archive, being null, does not exist")
                        }
                        val file = File(userCodeFile)
                        if (!file.exists())
                        {
                            throw RuntimeException("Source archive does not exist")
                        }
                        SourceConfigUtils.convert(sourceConfig, SourceConfigUtils.validate(sourceConfig, null, file))
                    }
                }
                (sinkConfig != null) ->
                {
                    Utils.inferMissingArguments(sinkConfig)
                    userCodeFile = sinkConfig.archive
                    // if code file not specified try to get location of the code based on class.
                    if (userCodeFile == null && sinkConfig.className != null)
                    {
                        userCodeFile = Thread.currentThread().contextClassLoader
                                .loadClass(sinkConfig.className)
                                .protectionDomain.codeSource.location.file
                    }
                    val builtInSink = isBuiltInSink(userCodeFile)
                    if (builtInSink != null)
                    {
                        sinkConfig.archive = builtInSink
                    }
                    parallelism = sinkConfig.parallelism
                    functionDetails = if (Utils.isFunctionPackageUrlSupported(userCodeFile))
                    {
                        val file = FunctionCommon.extractFileFromPkgURL(userCodeFile)
                        SinkConfigUtils.convert(sinkConfig, SinkConfigUtils.validate(sinkConfig, null, file))
                    }
                    else
                    {
                        if (userCodeFile == null)
                        {
                            throw RuntimeException("Sink archive, being null, does not exist")
                        }
                        val file = File(userCodeFile)
                        if (!file.exists())
                        {
                            throw RuntimeException("Sink archive does not exist")
                        }
                        SinkConfigUtils.convert(sinkConfig, SinkConfigUtils.validate(sinkConfig, null, file))
                    }
                }
                else ->
                {
                    throw IllegalArgumentException("Must specify Function, Source or Sink config")
                }
            }

            if (System.getProperty(FunctionCacheEntry.JAVA_INSTANCE_JAR_PROPERTY) == null)
            {
                System.setProperty(FunctionCacheEntry.JAVA_INSTANCE_JAR_PROPERTY,
                        PulsarLocalRunner::class.java.protectionDomain.codeSource.location.file)
            }
            val authConfig = AuthenticationConfig.builder()
                    .clientAuthenticationPlugin(clientAuthPlugin)
                    .clientAuthenticationParameters(clientAuthParams).useTls(useTls)
                    .tlsAllowInsecureConnection(tlsAllowInsecureConnection)
                    .tlsHostnameVerificationEnable(tlsHostNameVerificationEnabled)
                    .tlsTrustCertsFilePath(tlsTrustCertFilePath).build()

            val isJavaRunnableThing = sourceConfig != null
                    || sinkConfig != null
                    || functionConfig?.runtime == FunctionConfig.Runtime.JAVA

            if (isJavaRunnableThing && (runtimeEnv == null || runtimeEnv == RuntimeEnv.THREAD))
            { // By default run java functions as threads
                startThreadedMode(functionDetails, parallelism, instanceIdOffset, brokerServiceUrl,
                        stateStorageServiceUrl, authConfig, userCodeFile)
            }
            else
            {
                startProcessMode(functionDetails, parallelism, instanceIdOffset, brokerServiceUrl,
                        stateStorageServiceUrl, authConfig, userCodeFile)
            }
            local.addAll(spawners)
        }
        if (blocking)
        {
            for (spawner in local)
            {
                spawner.join()
                log.info("RuntimeSpawner quit because of {}", spawner.runtime.deathException)
            }
        }
    }

    @Throws(Exception::class)
    private fun startProcessMode(functionDetails: Function.FunctionDetails,
                                 parallelism: Int, instanceIdOffset: Int, serviceUrl: String,
                                 stateStorageServiceUrl: String?, authConfig: AuthenticationConfig,
                                 userCodeFile: String)
    {
        ProcessRuntimeFactory(
                serviceUrl,
                stateStorageServiceUrl,
                authConfig,
                null,  /* java instance jar file */
                null,  /* python instance file */
                null,  /* log directory */
                null,  /* extra dependencies dir */
                DefaultSecretsProviderConfigurator(), false).use { containerFactory ->
            for (ix in 0 until parallelism)
            {
                val instanceConfig = InstanceConfig()
                instanceConfig.functionDetails = functionDetails
                // TODO: correctly implement function version and id
                instanceConfig.functionVersion = UUID.randomUUID().toString()
                instanceConfig.functionId = UUID.randomUUID().toString()
                instanceConfig.instanceId = ix + instanceIdOffset
                instanceConfig.maxBufferedTuples = 1024
                instanceConfig.port = FunctionCommon.findAvailablePort()
                instanceConfig.clusterName = "local"
                val runtimeSpawner = RuntimeSpawner(
                        instanceConfig,
                        userCodeFile,
                        null,
                        containerFactory,
                        30000)
                spawners.add(runtimeSpawner)
                runtimeSpawner.start()
            }
            val statusCheckTimer = Timer()
            statusCheckTimer.scheduleAtFixedRate(object : TimerTask()
            {
                override fun run()
                {
                    val futures: Array<CompletableFuture<*>?> =
                            arrayOfNulls<CompletableFuture<*>>(spawners.size)
                    var index = 0
                    for (spawner in spawners)
                    {
                        futures[index] = spawner.getFunctionStatusAsJson(index)
                        index++
                    }
                    try
                    {
                        CompletableFuture.allOf(*futures)[5, TimeUnit.SECONDS]
                        index = 0
                        while (index < futures.size)
                        {
                            val json = futures[index]?.get()
                            if (json !is String) {
                                ++index
                                continue
                            }
                            val gson = GsonBuilder().setPrettyPrinting().create()
                            log.info("json: {}", gson.toJson(JsonParser().parse(json)))
                            ++index
                        }
                    }
                    catch (ex: Exception)
                    {
                        log.error("Could not get status from all local instances")
                    }
                }
            }, 30000, 30000)
            Runtime.getRuntime().addShutdownHook(object : Thread()
            {
                override fun run()
                {
                    statusCheckTimer.cancel()
                }
            })
        }
    }

    @Throws(Exception::class)
    private fun startThreadedMode(functionDetails: Function.FunctionDetails,
                                  parallelism: Int, instanceIdOffset: Int, serviceUrl: String,
                                  stateStorageServiceUrl: String?, authConfig: AuthenticationConfig,
                                  userCodeFile: String)
    {
        val threadRuntimeFactory = ThreadRuntimeFactory("PulsarLocalRunnerThreadGroup",
                serviceUrl,
                stateStorageServiceUrl,
                authConfig,
                ClearTextSecretsProvider(), null, null)

        (0 until parallelism)
                .forEach { ix ->
                    val instanceConfig = InstanceConfig()
                    instanceConfig.functionDetails = functionDetails
                    // TODO: correctly implement function version and id
                    instanceConfig.functionVersion = UUID.randomUUID().toString()
                    instanceConfig.functionId = UUID.randomUUID().toString()
                    instanceConfig.instanceId = ix + instanceIdOffset
                    instanceConfig.maxBufferedTuples = 1024
                    instanceConfig.port = FunctionCommon.findAvailablePort()
                    instanceConfig.clusterName = "local"
                    val runtimeSpawner = RuntimeSpawner(
                            instanceConfig,
                            userCodeFile,
                            null,
                            threadRuntimeFactory,
                            30000)
                    spawners.add(runtimeSpawner)
                    runtimeSpawner.start()
                }
    }

    /**
     * Source type is a valid built-in connector type.
     * For local-run we'll fill it up with its own archive path
     */
    @Throws(IOException::class)
    private fun isBuiltInSource(sourceType: String?): String?
    {
        val connectors = connectors
        return if (connectors.sources.containsKey(sourceType))
        {
            connectors.sources[sourceType].toString()
        }
        else
        {
            null
        }
    }

    /**
     * Source type is a valid built-in connector type.
     * For local-run we'll fill it up with its own archive path
     */
    @Throws(IOException::class)
    private fun isBuiltInSink(sinkType: String): String?
    {
        val connectors = connectors
        return if (connectors.sinks.containsKey(sinkType))
        {
            connectors.sinks[sinkType].toString()
        }
        else
        {
            null
        }
    }

    /**
     *  Validate the connector source type from the locally available connectors
     */
    @get:Throws(IOException::class)
    private val connectors: Connectors
        get()
        {
            val pulsarConnectorPath = System.getProperty("pulsar.connectors.path")
            if (pulsarConnectorPath != null)
            {
                return ConnectorUtils.searchForConnectors(pulsarConnectorPath)
            }

            val pulsarHomePath = System.getProperty("pulsar.home.path")
            if (pulsarHomePath != null)
            {
                val connectorsDir = Paths.get(pulsarHomePath, "connectors")
                return ConnectorUtils.searchForConnectors(connectorsDir.toString())
            }

            val pulsarHome = System.getenv("PULSAR_HOME")
            if (pulsarHome != null)
            {
                val connectorsDir = Paths.get(pulsarHome, "connectors")
                return ConnectorUtils.searchForConnectors(connectorsDir.toString())
            }

            val connectorsDir = Paths.get("", "connectors").toAbsolutePath()
            return ConnectorUtils.searchForConnectors(connectorsDir.toString())
        }

    companion object
    {
        val log = Logging.getLogger(PulsarLocalRunner::class.java)
    }

    init
    {
        Runtime.getRuntime().addShutdownHook(object : Thread()
        {
            override fun run()
            {
                this@PulsarLocalRunner.stop()
            }
        })
    }
}
