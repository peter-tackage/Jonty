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

package com.petertackage.jonty;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

public final class Jonty {

    static boolean DEBUG = true;

    private static String TAG = Jonty.class.getSimpleName();
    private static final String SUFFIX = "__Jonty";

    private static final Method NO_OP = null;

    private static final Map<Class<?>, Method> FIELDERS = new LinkedHashMap<>();

    public static <T> String[] field(Class<T> clazz) {

        try {
            if (DEBUG) {
                Logger.getLogger(TAG)
                      .info("Looking up object fielder for " + clazz.getName());
            }
            // Call the fielder and return the result
            Method fielder = findFielderForClass(clazz);
            if (fielder != NO_OP) {
                return (String[]) fielder.invoke(null);
            } else {
                throw new IllegalArgumentException(
                        "Unable to field class:" + clazz + " (not fieldable)");
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            Throwable t = e;
            if (t instanceof InvocationTargetException) {
                t = t.getCause();
            }
            throw new RuntimeException("Unable to field: " + clazz, t);
        }
    }

    private static Method findFielderForClass(Class<?> clazz) throws NoSuchMethodException {
        Method fielderMethod = FIELDERS.get(clazz);
        if (fielderMethod != null) {
            if (DEBUG) {
                Logger.getLogger(TAG).info("HIT: Cached in fielder map.");
            }
            return fielderMethod;
        }
        String clsName = clazz.getName();
        if (clsName.startsWith("android.") || clsName.startsWith("java.")) {
            if (DEBUG) {
                Logger.getLogger(TAG).info("MISS: Reached framework class. Abandoning search.");
            }
            return NO_OP;
        }
        try {
            if (DEBUG) {
                Logger.getLogger(TAG)
                      .info("Searching for fielders class: " + clsName + SUFFIX);
            }
            Class<?> fielderClass = Class.forName(clsName + SUFFIX);
            fielderMethod = fielderClass.getMethod("fields", clazz, clazz);
            if (DEBUG) {
                Logger.getLogger(TAG).info("HIT: Class loaded fielder class.");
            }
        } catch (ClassNotFoundException e) {
            if (DEBUG) {
                Logger.getLogger(TAG)
                      .info("Not found. Trying superclass " + clazz.getSuperclass().getName());
            }
            fielderMethod = findFielderForClass(clazz.getSuperclass());
        }
        FIELDERS.put(clazz, fielderMethod);
        return fielderMethod;
    }

}
