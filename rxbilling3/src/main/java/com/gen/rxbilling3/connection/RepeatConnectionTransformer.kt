package com.gen.rxbilling3.connection

import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.FlowableTransformer
import org.reactivestreams.Publisher

class RepeatConnectionTransformer<T> : FlowableTransformer<T, T> {
    override fun apply(upstream: Flowable<T>): Publisher<T> {
        return upstream
                .share()//all observers will wait connection
                .repeat()//repeat when billing client disconnected
                .replay()//return same instance for all observers
                .refCount()//keep connection if at least one observer exists
    }
}