/*
 * Copyright 2017-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.serialization.internal

import kotlinx.serialization.*
import kotlin.reflect.*

internal actual fun <T> Array<T>.getChecked(index: Int): T {
    if (index !in indices) throw IndexOutOfBoundsException("Index $index out of bounds $indices")
    return get(index)
}

internal actual fun BooleanArray.getChecked(index: Int): Boolean {
    if (index !in indices) throw IndexOutOfBoundsException("Index $index out of bounds $indices")
    return get(index)
}

internal actual fun <T : Any> KClass<T>.compiledSerializerImpl(): KSerializer<T>? =
    this.constructSerializerForGivenTypeArgs() ?: (
        if (this === Nothing::class) NothingSerializer // .js throws an exception for Nothing
        else this.js.asDynamic().Companion?.serializer()
        ) as? KSerializer<T>

internal actual fun <T: Any> KClass<T>.isInterface(): Boolean = isInterface

internal actual fun <T> createCache(factory: (KClass<*>) -> KSerializer<T>?): SerializerCache<T> {
    return object: SerializerCache<T> {
        override fun get(key: KClass<Any>): KSerializer<T>? {
            return factory(key)
        }
    }
}

internal actual fun <T> createParametrizedCache(factory: (KClass<Any>, List<KType>) -> KSerializer<T>?): ParametrizedSerializerCache<T> {
    return object: ParametrizedSerializerCache<T> {
        override fun get(key: KClass<Any>, types: List<KType>): Result<KSerializer<T>?> {
            return kotlin.runCatching { factory(key, types) }
        }
    }
}

internal actual fun <T : Any, E : T?> ArrayList<E>.toNativeArrayImpl(eClass: KClass<T>): Array<E> = toTypedArray()

internal actual fun KClass<*>.platformSpecificSerializerNotRegistered(): Nothing {
    throw SerializationException(
        notRegisteredMessage() +
                "To get enum serializer on Kotlin/JS, it should be annotated with @Serializable annotation."
    )
}

@Suppress("UNCHECKED_CAST", "DEPRECATION_ERROR")
@OptIn(ExperimentalAssociatedObjects::class)
internal actual fun <T : Any> KClass<T>.constructSerializerForGivenTypeArgs(vararg args: KSerializer<Any?>): KSerializer<T>? =
    try {
        val assocObject = findAssociatedObject<SerializableWith>()
        when {
            assocObject is KSerializer<*> -> assocObject as KSerializer<T>
            assocObject is SerializerFactory -> assocObject.serializer(*args) as KSerializer<T>
            else -> null
        }
    } catch (e: dynamic) {
        null
    }

internal actual fun isReferenceArray(rootClass: KClass<Any>): Boolean = rootClass == Array::class

/**
 * WARNING: may be broken in arbitrary time in the future without notice
 *
 * Should be eventually replaced with compiler intrinsics
 */
private val KClass<*>.isInterface: Boolean
    get(): Boolean {
        // .js throws an exception for Nothing
        if (this === Nothing::class) return false
        return js.asDynamic().`$metadata$`?.kind == "interface"
    }
