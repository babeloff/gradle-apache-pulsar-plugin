package org.ensample.gradle.api.extension

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property

open class GreetingPluginExtension(objects: ObjectFactory)
{
    val message : Property<String> = objects.property(String::class.java)
    val recipient : Property<String> = objects.property(String::class.java)
}
