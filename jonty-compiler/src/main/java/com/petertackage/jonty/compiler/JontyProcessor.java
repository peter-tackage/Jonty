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

import com.petertackage.jonty.Fieldable;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

final class JontyProcessor extends AbstractProcessor {

    private static final String OPTION_DEBUGGABLE = "jonty.debuggable";

    private Elements elementUtils;
    private Filer filer;
    private boolean debuggable = true;

    @Override
    public synchronized void init(ProcessingEnvironment env) {
        super.init(env);
        elementUtils = env.getElementUtils();
        filer = env.getFiler();
        debuggable = !"false".equals(env.getOptions().get(OPTION_DEBUGGABLE));

    }

    @Override
    public boolean process(final Set<? extends TypeElement> annotations,
                           final RoundEnvironment roundEnv) {

        Map<TypeElement, Fielder> fielderMap = findAndParseFields(roundEnv);

        for (Map.Entry<TypeElement, Fielder> entry : fielderMap.entrySet()) {
            TypeElement typeElement = entry.getKey();
            Fielder fielder = entry.getValue();

            // Write it out to a file.
            JavaFile javaFile = fielder.brewJava(debuggable);
            try {
                javaFile.writeTo(filer);
            } catch (IOException e) {
                error(typeElement, "Unable to write fielder for type %s: %s", typeElement, e.getMessage());
            }

        }

        return false;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.RELEASE_6;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(Fieldable.class.getName());
    }

    private Map<TypeElement, Fielder> findAndParseFields(RoundEnvironment env) {

        // Maps of classes to builders
        Map<TypeElement, Fielder.Builder> builderMap = new LinkedHashMap<>();

        // Process each @Fieldable class element.
        for (Element element : env.getElementsAnnotatedWith(Fieldable.class)) {

            try {
                parseFieldableClass(element, builderMap);
            } catch (Exception e) {
                logParsingError(element, Fieldable.class, e);
            }
        }

        // @TODO Traverse up the tree, but without duplicates. Don't need to do this yet.
        // @TODO Will just assume that only one annotation exists in the hierachy.
        // @TODO In any case, duplicates aren't problematic
        // Associate superclass binders with their subclass binders. This is a queue-based tree walk
        // which starts at the roots (superclasses) and walks to the leafs (subclasses).
        Map<TypeElement, Fielder> fielderMap = new LinkedHashMap<>();

        for (Map.Entry<TypeElement, Fielder.Builder> entry : builderMap.entrySet()) {
            fielderMap.put(entry.getKey(), entry.getValue().build());
        }
        return fielderMap;
    }

    private void parseFieldableClass(Element element,
                                     Map<TypeElement, Fielder.Builder> builderMap) {

        Fielder.Builder fielderBuilder = new Fielder.Builder(ClassName.get((TypeElement) element));
        for (Element enclosedElement : element.getEnclosedElements()) {

            TypeElement typeElement = (TypeElement) enclosedElement;

            // Only interested in fields
            if (typeElement.getKind() == ElementKind.FIELD) {
                String fieldName = typeElement.getSimpleName().toString();
                fielderBuilder.addName(fieldName);
            }

        }

        builderMap.put((TypeElement) element, fielderBuilder);
    }

    private static String getClassName(TypeElement type, String packageName) {
        return packageName.length() == 0 ?
                type.getQualifiedName().toString() :
                type.getQualifiedName().toString().substring(packageName.length() + 1)
                        .replace('.', '$');
    }

    private String getPackageName(TypeElement type) {
        return elementUtils.getPackageOf(type).getQualifiedName().toString();
    }

    private void logParsingError(Element element, Class<? extends Annotation> annotation,
                                 Exception e) {
        StringWriter stackTrace = new StringWriter();
        e.printStackTrace(new PrintWriter(stackTrace));
        error(element, "Unable to parse @%s fielder.\n\n%s", annotation.getSimpleName(),
                stackTrace);
    }

    private void error(Element element, String message, Object... args) {
        printMessage(Diagnostic.Kind.ERROR, element, message, args);
    }

    private void note(Element element, String message, Object... args) {
        printMessage(Diagnostic.Kind.NOTE, element, message, args);
    }

    private void printMessage(Diagnostic.Kind kind, Element element, String message,
                              Object[] args) {
        if (args.length > 0) {
            message = String.format(message, args);
        }

        processingEnv.getMessager().printMessage(kind, message, element);
    }

}

