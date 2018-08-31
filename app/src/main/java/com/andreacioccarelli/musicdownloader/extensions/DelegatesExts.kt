package com.andreacioccarelli.musicdownloader.extensions

import kotlin.reflect.KProperty

/**
 * Created by andrea on 2018/Aug.
 * Part of the package andreacioccarelli.musicdownloader.extensions
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