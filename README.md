# Jonty

[![Build Status](https://travis-ci.org/peter-tackage/jonty.svg?branch=master)](https://travis-ci.org/peter-tackage/jonty) [![Release](https://jitpack.io/v/peter-tackage/jonty.svg)](https://jitpack.io/#peter-tackage/jonty)


A simple Kotlin/Java annotation processor to generate a list of the names of fields of a given class.

For example:

```java

@Fieldable
public class MyClass {
    private final List<SomeType> profiles;
    private final String username;
    
    // etc...
}
```

After compilation, this will generate an object in a Kotlin file:
 
```kotlin
object MyClass_JontyFielder {
  val fields: Iterable<String> = setOf("profiles", "username")
}
``` 
 
Which can be accessed via:

```kotlin
val fields = MyClass_JontyFielder.fields

```

## Usage

Just annotate your class/data class with the `@Fieldable` annotation and Jonty will generate a class based upon your class name.

There are no restrictions on private fields, or inner classes. It also works with Kotlin data classes.

## Why Jonty?

Because of the *fielding* (terrible pun, I know) done [here](https://www.youtube.com/watch?v=e4Um90BzDjM).


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


