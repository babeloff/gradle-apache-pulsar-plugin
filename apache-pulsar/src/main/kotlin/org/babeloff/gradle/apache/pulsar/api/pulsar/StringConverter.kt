package org.example.gradle.api.pulsar

import org.apache.pulsar.common.functions.FunctionConfig
import org.apache.pulsar.common.io.SinkConfig
import org.apache.pulsar.common.io.SourceConfig
import org.apache.pulsar.common.util.ObjectMapperFactory
import java.io.IOException


interface IStringConverter<T>
{
    fun convert(value: String?): T
}

class StringConverter : IStringConverter<String?>
{
    override fun convert(value: String?): String
    {
        return value ?: ""
    }
}

class FunctionConfigConverter : IStringConverter<FunctionConfig?>
{
    override fun convert(value: String?): FunctionConfig
    {
        return try
        {
            ObjectMapperFactory.getThreadLocal().readValue(value, FunctionConfig::class.java)
        }
        catch (e: IOException)
        {
            throw RuntimeException("Failed to parse function config:", e)
        }
    }
}

class SourceConfigConverter : IStringConverter<SourceConfig?>
{
    override fun convert(value: String?): SourceConfig
    {
        return try
        {
            ObjectMapperFactory.getThreadLocal().readValue(value, SourceConfig::class.java)
        }
        catch (e: IOException)
        {
            throw RuntimeException("Failed to parse source config:", e)
        }
    }
}

class SinkConfigConverter : IStringConverter<SinkConfig?>
{
    override fun convert(value: String?): SinkConfig
    {
        return try
        {
            ObjectMapperFactory.getThreadLocal().readValue(value, SinkConfig::class.java)
        }
        catch (e: IOException)
        {
            throw RuntimeException("Failed to parse sink config:", e)
        }
    }
}

class RuntimeConverter : IStringConverter<PulsarLocalRunner.RuntimeEnv?>
{
    override fun convert(value: String?): PulsarLocalRunner.RuntimeEnv
    {
        return PulsarLocalRunner.RuntimeEnv.valueOf(value!!)
    }
}
