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

    @Test
    fun test() {
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