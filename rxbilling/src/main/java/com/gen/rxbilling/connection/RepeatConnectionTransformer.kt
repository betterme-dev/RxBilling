package com.gen.rxbilling.connection

import io.reactivex.Flowable
import io.reactivex.FlowableTransformer
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