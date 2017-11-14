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

package com.petertackage.jonty.compiler

import com.squareup.kotlinpoet.*
import java.util.*

internal class Fielder private constructor(private val fielderClassName: ClassName,
                                           names: Set<String>) {

    private val names: Set<String> = Collections.unmodifiableSet(names)

    fun brew(debuggable: Boolean): FileSpec {
        return FileSpec.builder(fielderClassName.packageName(), fielderClassName.simpleName())
                .addComment("Generated code by Jonty. Do not modify!")
                .addType(defineObject(fielderClassName.simpleName()))
                .build()
    }

    private fun defineObject(name: String): TypeSpec {
        return TypeSpec.objectBuilder(name)
                .addProperty(defineFields())
                .build()
    }

    private fun defineFields(): PropertySpec {
        return PropertySpec.builder("fields",
                ParameterizedTypeName.get(Iterable::class.java, String::class.java))
                .initializer("setOf(%L)", toArgs(names))
                .build()
    }

    private fun toArgs(iterable: Iterable<String>): String {
        return iterable.joinToString(transform = { name -> "\" $name \"" })
    }

    internal class Builder(private val fielderClassName: ClassName) {
        private val names = TreeSet<String>()

        fun addName(name: String): Builder {
            this.names.add(name)
            return this
        }

        fun build(): Fielder {
            return Fielder(fielderClassName, names)
        }
    }
}
