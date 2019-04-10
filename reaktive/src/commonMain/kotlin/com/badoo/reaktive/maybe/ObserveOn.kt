package com.badoo.reaktive.maybe

import com.badoo.reaktive.disposable.CompositeDisposable
import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.scheduler.Scheduler

fun <T> Maybe<T>.observeOn(scheduler: Scheduler): Maybe<T> =
    maybeByEmitter { emitter ->
        val disposables = CompositeDisposable()
        emitter.setDisposable(disposables)
        val executor = scheduler.newExecutor()
        disposables += executor

        subscribeSafe(
            object : MaybeObserver<T> {
                override fun onSubscribe(disposable: Disposable) {
                    disposables += disposable
                }

                override fun onSuccess(value: T) {
                    executor.submit {
                        emitter.onSuccess(value)
                    }
                }

                override fun onComplete() {
                    executor.submit {
                        emitter.onComplete()
                    }
                }

                override fun onError(error: Throwable) {
                    executor.submit {
                        emitter.onError(error)
                    }
                }
            }
        )
    }