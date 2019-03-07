package com.badoo.reaktive.observable

inline fun <T> observable(crossinline subscribe: (observer: ObservableObserver<T>) -> Unit): Observable<T> =
    object : Observable<T> {
        override fun subscribe(observer: ObservableObserver<T>) {
            subscribe(observer)
        }
    }

fun <T> observableOf(value: T): Observable<T> =
    observableByEmitter { emitter ->
        emitter.onNext(value)
        emitter.onComplete()
    }

fun <T> Iterable<T>.toObservable(): Observable<T> =
    observableByEmitter { emitter ->
        forEach(emitter::onNext)
        emitter.onComplete()
    }

fun <T> observableOf(vararg values: T): Observable<T> =
    observableByEmitter { emitter ->
        values.forEach(emitter::onNext)
        emitter.onComplete()
    }

fun <T> errorObservable(error: Throwable): Observable<T> =
    observableByEmitter { emitter -> emitter.onError(error) }

fun <T> Throwable.toErrorObservable(): Observable<T> = errorObservable(this)

fun <T> emptyObservable(): Observable<T> = observableByEmitter(ObservableEmitter<*>::onComplete)

fun <T> observableFromFunction(func: () -> T): Observable<T> =
    observableByEmitter { emitter ->
        emitter.onNext(func())
        emitter.onComplete()
    }