# Jonty

[![Build Status](https://travis-ci.org/peter-tackage/jonty.svg?branch=master)](https://travis-ci.org/peter-tackage/jonty) [![Release](https://jitpack.io/v/peter-tackage/jonty.svg)](https://jitpack.io/#peter-tackage/jonty)


A simple Kotlin/Java annotation processor to generate a list of a class field names.

For example:

```java
@Fieldable
public class User {
    private final List<Profile> profiles;
    private final String username;
    private final String password;
    private final Date lastLogin;
    
    // etc...
}
```

Jonty will generate an object in a Kotlin file in the `User` class' package:
 
```kotlin
object User_JontyFielder {
  val fields: Iterable<String> = setOf("profiles", "username", "password", "lastLogin")
}
``` 
 
Which can be accessed via:

```kotlin
val fields = User_JontyFielder.FIELDS

```

or

```java
Iterable<String> fields = User_JontyFielder.FIELDS
```

## Usage

Just annotate your Java/Kotlin class with the `@Fieldable` annotation and Jonty will generate a class based upon your class name.

There are no restrictions on class or field access modifiers.

If the class is a child class, then the unique field names for all parent classes will also be added.

Static fields are ignored.

## Download

Available via Jitpack.

For Gradle, add kapt3 plugin:
```groovy
apply plugin: 'kotlin-kapt'
```

Add to your Jitpack to your project `repository` configuration"

```groovy
maven { url "https://jitpack.io" }
```

Add these dependencies:
```groovy
compileOnly "com.github.peter-tackage.jonty:jonty:<latest-version>"
kapt "com.github.peter-tackage.jonty:jonty-processor:<latest-version>"
```

See [freesound-android](https://github.com/futurice/freesound-android) for a full working example.

## Why Jonty?

Because of the *fielding* (terrible pun, I know) done [here](https://www.youtube.com/watch?v=e4Um90BzDjM).

## Development

To locally est changes to Jonty, use the `example-local` module, which imports the module directly,
rather than going through Jitpack.

To do this, uncomment this line in `settings.gradle` -
```groovy
// Uncomment to test locally.
//include 'example-local'
``` 
## Acknowledgements

Brought to you by the power of the [Chilicorn](http://spiceprogram.org/chilicorn-history/) and the [Futurice Open Source Program](http://spiceprogram.org/).

![Chilicorn Logo](https://raw.githubusercontent.com/futurice/spiceprogram/gh-pages/assets/img/logo/chilicorn_no_text-256.png)
## License

    Copyright 2017 Peter Tackage

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.


