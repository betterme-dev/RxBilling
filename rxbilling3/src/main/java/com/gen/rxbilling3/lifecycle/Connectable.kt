package com.gen.rxbilling3.lifecycle

import io.reactivex.rxjava3.core.Flowable

interface Connectable<T> {

    fun connect() : Flowable<T>
}