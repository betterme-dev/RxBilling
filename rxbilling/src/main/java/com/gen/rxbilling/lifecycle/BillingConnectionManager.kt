package com.gen.rxbilling.lifecycle

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import io.reactivex.disposables.Disposable
import timber.log.Timber

class BillingConnectionManager<T>(private val connectable: Connectable<T>)
    : LifecycleObserver {
    private var disposable: Disposable? = null

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun connect() {
        Timber.d("connect")
        disposable = connectable.connect()
                .subscribe({
                    Timber.d("$it")
                }, {
                    Timber.e(it)
                }, {
                    Timber.d("onComplete")
                })
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun disconnect() {
        Timber.d("disconnect")
        disposable?.dispose()
    }
}