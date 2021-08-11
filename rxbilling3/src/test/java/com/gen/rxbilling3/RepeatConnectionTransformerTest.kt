package com.gen.rxbilling3

import com.gen.rxbilling3.connection.RepeatConnectionTransformer
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.schedulers.TestScheduler
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import java.util.concurrent.TimeUnit

@RunWith(MockitoJUnitRunner::class)
class RepeatConnectionTransformerTest {

    @Test
    fun shouldRepeat() {
        val subscriber = Flowable.just(1)
                .compose(RepeatConnectionTransformer<Int>())
                .take(2).test()
        subscriber.assertValueSequence(listOf(1, 1))
        subscriber.assertComplete()
    }


    @Test
    fun shouldShare() {
        var callCount = 0
        val testScheduler = TestScheduler()
        val flowable = Flowable.fromCallable {
            callCount = callCount.inc()
            return@fromCallable 1
        }.delay(2, TimeUnit.SECONDS, testScheduler)
                .compose(RepeatConnectionTransformer<Int>())
                .take(1)
        val observer1 = flowable.test()
        testScheduler.advanceTimeBy(1, TimeUnit.SECONDS)
        val observer2 = flowable.test()
        testScheduler.advanceTimeBy(1, TimeUnit.SECONDS)
        observer1.assertValue(1)
        observer2.assertValue(1)
        observer1.assertComplete()
        observer2.assertComplete()

        assertEquals(1, callCount)
    }

    @Test
    fun shouldReplay() {
        var callCount = 0
        val testScheduler = TestScheduler()
        val flowable = Flowable.just(2)
                .delay(100, TimeUnit.SECONDS, testScheduler)
                .startWith(Flowable.fromCallable {
                    callCount = callCount.inc()
                    return@fromCallable 1
                })
                .compose(RepeatConnectionTransformer<Int>())
        val observer1 = flowable.test()
        testScheduler.advanceTimeBy(1, TimeUnit.SECONDS)
        val observer2 = flowable.test()
        testScheduler.advanceTimeBy(1, TimeUnit.SECONDS)
        observer1.assertValue(1)
        observer2.assertValue(1)
        observer1.assertNotComplete()
        observer2.assertNotComplete()

        assertEquals(1, callCount)
    }


    @Test
    fun shouldCancelWhenNoSubscribers() {
        val testScheduler = TestScheduler()
        var cancelCalled = false
        val flowable = Flowable.just(2)
                .delay(100, TimeUnit.SECONDS, testScheduler)
                .startWith(Flowable.create<Int>({
                    it.onNext(1)
                    it.setCancellable { cancelCalled = true }
                }, BackpressureStrategy.LATEST))
                .compose(RepeatConnectionTransformer<Int>())
        val subscriber = flowable.test()
        subscriber.cancel()
        subscriber.assertValueSequence(listOf(1))
        subscriber.assertNotComplete()
        assertTrue(cancelCalled)
    }

}