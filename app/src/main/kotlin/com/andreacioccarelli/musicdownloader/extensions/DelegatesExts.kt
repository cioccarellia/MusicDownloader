package com.andreacioccarelli.musicdownloader.extensions

import kotlin.reflect.KProperty

/**
 *  Designed and developed by Andrea Cioccarelli
 */

object Delegates {
    fun <T> singleValue() = SingleValue<T>()
}

class SingleValue<T> {
    private var value: T? = null

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T =
            value ?: throw IllegalStateException("${property.name} not initialized")

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        this.value = value
    }
}