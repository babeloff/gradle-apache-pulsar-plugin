package org.babeloff.gradle.apache.pulsar.api.extension

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property

open class PulsarPluginExtension(objects: ObjectFactory)
{
    val tenant : Property<String> = objects.property(String::class.java)
    val namespace : Property<String> = objects.property(String::class.java)
    val topicName : Property<String> = objects.property(String::class.java)
}
