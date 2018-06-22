package com.gen.rxbilling.lifecycle

import io.reactivex.Flowable

interface Connectable<T> {

    fun connect() : Flowable<T>
}