/*
 * Copyright 2017 Futurice GmbH
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

package com.petertackage.jonty.compiler;

import com.squareup.kotlinpoet.*;
import kotlin.collections.CollectionsKt;
import kotlin.jvm.functions.Function1;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

// TODO This should generate Kotlin code.
final class Fielder {

    private final ClassName fielderClassName;
    private final Set<String> names;

    private Fielder(ClassName fielderClassName,
                    Set<String> names) {
        this.fielderClassName = fielderClassName;
        this.names = Collections.unmodifiableSet(names);
    }

    FileSpec brew(boolean debuggable) {
        return FileSpec.builder(fielderClassName.packageName(), fielderClassName.simpleName())
                .addComment("Generated code by Jonty. Do not modify!")
                .addType(defineObject(fielderClassName.simpleName()))
                .build();
    }

    private TypeSpec defineObject(String name) {
        return TypeSpec.objectBuilder(name)
                .addProperty(defineFields())
                .build();
    }

    private PropertySpec defineFields() {
        return PropertySpec.builder("fields", ParameterizedTypeName.get(Iterable.class, String.class))
                .initializer("setOf(%L)", toArgs(names))
                .build();
    }

    private static String toArgs(Iterable<String> iterable) {
        return CollectionsKt.joinToString(iterable, ", ", "", "", -1, "",
                new Function1<String, CharSequence>() {
                    @Override
                    public CharSequence invoke(String name) {
                        return "\"" + name + "\"";
                    }
                });
    }

    final static class Builder {

        private final ClassName fielderClassName;
        private final Set<String> names = new TreeSet<>();

        Builder(ClassName fielderClassName) {
            this.fielderClassName = fielderClassName;
        }

        Builder addName(String name) {
            this.names.add(name);
            return this;
        }

        Fielder build() {
            return new Fielder(fielderClassName, names);
        }
    }
}
