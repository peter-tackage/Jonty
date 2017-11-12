package com.test.app

import com.petertackage.jonty.Fieldable

@Fieldable
data class KotlinDataClass(val xyz: String)

@Fieldable
class KotlinClass {

    private val abc : String = "AVX"
}