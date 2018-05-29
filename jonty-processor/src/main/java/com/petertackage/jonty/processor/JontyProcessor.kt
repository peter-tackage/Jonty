/*
 * Copyright 2017 Peter Tackage
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.petertackage.jonty.processor

import com.petertackage.jonty.Fieldable
import com.petertackage.jonty.processor.internal.Fielder
import com.squareup.kotlinpoet.ClassName
import java.io.IOException
import java.io.PrintWriter
import java.io.StringWriter
import java.nio.file.Paths
import java.util.*
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedOptions
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeKind
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.Elements
import javax.tools.Diagnostic

@SupportedOptions(JontyProcessor.KAPT_KOTLIN_GENERATED_OPTION_NAME)
class JontyProcessor : AbstractProcessor() {

    private var elementUtils: Elements? = null
    private var debuggable = true

    companion object {
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
        private val OPTION_DEBUGGABLE = "jonty.debuggable"
        private val DEBUG = false
    }

    @Synchronized override fun init(env: ProcessingEnvironment) {
        super.init(env)
        elementUtils = env.elementUtils
        debuggable = "false" != env.options[OPTION_DEBUGGABLE]
    }

    override fun process(annotations: Set<TypeElement>,
                         roundEnv: RoundEnvironment): Boolean {

        debug(null, "Starting annotation processing round.")
        val fielderMap = findAndParseFields(roundEnv)

        for ((typeElement, fielder) in fielderMap) {

            val kotlinFile = fielder.brew()

            try {
                val kaptKotlinGeneratedOutDir = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME]
                debug(null, "kaptKotlin output directory: %s.", KAPT_KOTLIN_GENERATED_OPTION_NAME)
                kotlinFile.writeTo(Paths.get(kaptKotlinGeneratedOutDir))
            } catch (e: IOException) {
                error(typeElement, "Unable to write fielder for type %s: %s.", typeElement, e)
            }

        }

        return false
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latestSupported()
    }

    override fun getSupportedAnnotationTypes(): Set<String> {
        return setOf(Fieldable::class.java.canonicalName)
    }

    private fun findAndParseFields(env: RoundEnvironment): Map<TypeElement, Fielder> {

        // Maps of classes to builders
        val builderMap = LinkedHashMap<TypeElement, Fielder.Builder>()

        // Process each @Fieldable class element.
        for (element in env.getElementsAnnotatedWith(Fieldable::class.java)) {
            debug(element, "Processing annotated element: %s.", element)
            try {
                parseFieldableClass(element, builderMap)
            } catch (e: Exception) {
                logParsingError(element, Fieldable::class.java, e)
            }

        }

        val fielderMap = LinkedHashMap<TypeElement, Fielder>()

        for ((key, value) in builderMap) {
            fielderMap.put(key, value.build())
        }
        return fielderMap
    }

    private fun parseFieldableClass(element: Element,
                                    builderMap: MutableMap<TypeElement, Fielder.Builder>) {

        var typeElement: TypeElement? = element as TypeElement
        val packageName = getPackageName(typeElement)
        val className = getClassName(typeElement, packageName)
        val bindingClassName = ClassName(packageName, className + "_JontyFielder")

        val fielderBuilder = Fielder.Builder(bindingClassName)
        builderMap.put(element, fielderBuilder)

        while (typeElement != null) {
            for (enclosedElement in typeElement.enclosedElements) {

                debug(enclosedElement,
                        "Element enclosed element: %s, modifiers: %s, type: %s.",
                        enclosedElement.simpleName, enclosedElement.modifiers, enclosedElement.asType())

                // Only interested in non-static fields; properties
                if (enclosedElement.kind == ElementKind.FIELD && !enclosedElement.modifiers.contains(Modifier.STATIC)) {
                    val fieldName = enclosedElement.simpleName.toString()
                    debug(enclosedElement, "!! Adding field !!: %s.", fieldName)
                    fielderBuilder.addName(fieldName)
                }
            }
            // Add fields from the parent class
            typeElement = findParentType(typeElement)
        }

    }

    private fun findParentType(typeElement: TypeElement): TypeElement? {
        debug(typeElement, "Attempting to find superclass for %s.", typeElement)
        val type: TypeMirror = typeElement.superclass
        if (type.kind == TypeKind.NONE) {
            debug(typeElement, "No superclass for: %s.", type)
            return null
        }
        debug(typeElement, "Found superclass: %s.", type)
        return (type as DeclaredType).asElement() as TypeElement
    }

    private fun getPackageName(type: TypeElement?): String {
        return elementUtils!!.getPackageOf(type).qualifiedName.toString()
    }

    private fun logParsingError(element: Element, annotation: Class<out Annotation>, e: Exception) {
        val stackTrace = StringWriter()
        e.printStackTrace(PrintWriter(stackTrace))
        error(element, "Unable to parse @%s fielder.\n\n%s.", annotation.simpleName, stackTrace)
    }

    private fun error(element: Element, message: String, vararg args: Any) {
        printMessage(Diagnostic.Kind.ERROR, element, message, *args)
    }

    private fun note(element: Element?, message: String, vararg args: Any) {
        printMessage(Diagnostic.Kind.NOTE, element, message, *args)
    }

    private fun debug(element: Element?, message: String, vararg args: Any) {
        if (DEBUG) printMessage(Diagnostic.Kind.OTHER, element, message, *args)
    }

    private fun printMessage(kind: Diagnostic.Kind, element: Element?, message: String, vararg args: Any) {
        var formattedMsg = message;
        if (args.isNotEmpty()) {
            formattedMsg = message.format(*args)
        }
        processingEnv.messager.printMessage(kind, formattedMsg, element)
    }

    private fun getClassName(type: TypeElement?, packageName: String): String {
        return if (packageName.length == 0) type!!.qualifiedName.toString()
        else type!!.qualifiedName.toString().substring(packageName.length + 1)
                .replace('.', '_')
    }

}
