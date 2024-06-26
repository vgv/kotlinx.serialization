/*
 * Copyright 2017-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */
package kotlinx.serialization.protobuf

import kotlinx.serialization.builtins.*
import kotlin.test.*

class ProtobufPrimitivesTest {
    @Test
    fun testPrimitiveTypes() {
        testConversion(true, Boolean.serializer(), "01")
        testConversion('c', Char.serializer(), "63")
        testConversion(1, Byte.serializer(), "01")
        testConversion(1, Short.serializer(), "01")
        testConversion(1, Int.serializer(), "01")
        testConversion(1, Long.serializer(), "01")
        testConversion(1f, Float.serializer(), "0000803F")
        testConversion(1.0, Double.serializer(), "000000000000F03F")
        testConversion("string", String.serializer(), "06737472696E67")
        testConversion(Unit, Unit.serializer(), "")
    }
}
