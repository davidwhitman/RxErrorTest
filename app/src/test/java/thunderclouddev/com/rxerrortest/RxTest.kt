package thunderclouddev.com.rxerrortest

import org.junit.Test
import rx.Observable
import rx.subjects.PublishSubject
import java.util.*

/**
 * @author David Whitman on Aug 04, 2016.
 */
class RxTest {
    var index: Int = 0
    var subject = PublishSubject.create<String>()

    /**
     * In order to use `onErrorResumeNext` with `retryWhen`,
     * it's necessary to wrap these first two calls in a single observable.
     * Otherwise, the `retryWhen` will resubscribe to `Observable.error` in `onErrorResumeNext`
     * instead of `makeCall`.
     * Wrapping them has the effect of getting `retryWhen` to resubscribe to the entire unit,
     * which is what we want.
     */
    @Test
    fun test_onError_plus_retryWhen_working() {
        print("Starting")

        Observable.create<String> { subscriber ->
            makeCall()
                    .onErrorResumeNext { error ->
                        print("onErrorResumeNext called")
                        subscriber.onError(error)
                        return@onErrorResumeNext Observable.just<String>(null)
                    }
                    .subscribe {
                        subscriber.onNext(it)
                        subscriber.onCompleted()
                    }
        }
                .retryWhen { observable ->
                    print("Entered RetryWhen")

                    return@retryWhen observable
                            .flatMap { i ->
                                print("retry")
                                subject = PublishSubject.create<String>()

                                Timer().schedule(object : TimerTask() {
                                    override fun run() {
                                        print("Calling onNext after delay")
                                        subject.onNext(null)
//                                        subject.onCompleted()

                                    }
                                }, 250)

                                return@flatMap subject
                            }
                }
                .subscribe { value ->
                    print(value)
                }

        waitFor(2000)
        // Prints:

//        Starting
//        Entered RetryWhen
//        Resubscribing
//        onErrorResumeNext called
//        retry
//        Calling onNext after delay
//        Resubscribing
//        onErrorResumeNext called
//        retry
//        Calling onNext after delay
//        Resubscribing
//        onErrorResumeNext called
//        retry
//        Calling onNext after delay
//        Resubscribing
//        Value reached end
    }

    @Test
    fun test_onError_plus_retryWhen_problematic() {
        print("Starting")

        makeCall()
                .onErrorResumeNext { error ->
                    print("onErrorResumeNext called")
                    return@onErrorResumeNext Observable.error(error)
                }
                .retryWhen { observable ->
                    print("Entered RetryWhen")

                    return@retryWhen observable
                            .flatMap { i ->
                                print("retry")
                                subject = PublishSubject.create<String>()

                                Timer().schedule(object : TimerTask() {
                                    override fun run() {
                                        print("Calling onNext after delay")
                                        subject.onNext(null)
//                                        subject.onCompleted()

                                    }
                                }, 250)

                                return@flatMap subject
                            }
                }
                .subscribe { value ->
                    print(value)
                }

        waitFor(2000)
        // Prints:

//        Starting
//        Resubscribing
//        Entered RetryWhen
//        onErrorResumeNext called
//        retry
//        Calling onNext after delay
//        onErrorResumeNext called
//        retry
//        Calling onNext after delay
//        onErrorResumeNext called
//        retry
//        Calling onNext after delay
//        onErrorResumeNext called
//        retry
//        Calling onNext after delay
//        onErrorResumeNext called
//        retry
//        Calling onNext after delay
//        onErrorResumeNext called
//        retry
//        Calling onNext after delay
//        onErrorResumeNext called
//        retry
//        Calling onNext after delay
//        onErrorResumeNext called
//        retry
//        Calling onNext after delay
//        onErrorResumeNext called
//        retry
    }

    @Test
    fun test_zip() {
        print("Starting")

        Observable.create<String> { subscriber ->
            print("Resubscribing")

            if (index < 3) {
                index++
                subscriber.onError(Exception())
            } else {
                subscriber.onNext("Value reached end")
            }
        }
                .onErrorResumeNext { error ->
                    print("onErrorResumeNext called")
                    return@onErrorResumeNext Observable.error(error)
                }
                .retryWhen { observable ->
                    print("Retrying")
                    return@retryWhen observable
                            .zipWith(Observable.range(1, 5), { s, i -> i })
                            .flatMap { i ->
                                print("#$i retry")
                                return@flatMap Observable.just(null)
                            }
                }
                .subscribe { value ->
                    print(value)
                }

        // Prints

//        Starting
//        Retrying
//        Resubscribing
//        onErrorResumeNext called
//        #1 retry
//        Resubscribing
//        onErrorResumeNext called
//        #2 retry
//        Resubscribing
//        onErrorResumeNext called
//        #3 retry
//        Resubscribing
//        Value reached end
    }

    private fun print(input: String) {
        System.out.println(input)
    }

    private fun waitFor(timeInMillis: Int) {
        try {
            Thread.sleep(timeInMillis.toLong())
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

    }

    private fun makeCall(): Observable<String> {
        print("Resubscribing")

        if (index < 3) {
            index++
            return Observable.error(Exception())
        } else {
            return Observable.just("Value reached end")
        }


//        return Observable.create<String> { subscriber ->
//            print("Resubscribing")
//
//            if (index < 3) {
//                index++
//                subscriber.onError(Exception())
//            } else {
//                subscriber.onNext("Value reached end")
//            }
//        }
    }
}