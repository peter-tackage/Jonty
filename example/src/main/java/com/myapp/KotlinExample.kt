package com.myapp;

import com.petertackage.jonty.Fieldable

@Fieldable
data class KotlinDataClass(val xyz: String)

@Fieldable
class KotlinClass {
    private var tub: String = "ddfsdsfdsd"
    private val abc: String = "AVX"

    fun getThings(): Iterable<String> {
        return KotlinClass_JontyFielder.fields
    }
}