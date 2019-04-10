package com.badoo.reaktive.maybe

import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.disposable.DisposableWrapper
import com.badoo.reaktive.utils.UseReturnValue
import com.badoo.reaktive.utils.handleSourceError

@UseReturnValue
fun <T> Maybe<T>.subscribe(
    onSubscribe: ((Disposable) -> Unit)? = null,
    onError: ((Throwable) -> Unit)? = null,
    onComplete: (() -> Unit)? = null,
    onSuccess: ((T) -> Unit)? = null
): Disposable {
    val disposableWrapper = DisposableWrapper()

    subscribeSafe(
        object : MaybeObserver<T> {
            override fun onSubscribe(disposable: Disposable) {
                disposableWrapper.set(disposable)
                onSubscribe?.invoke(disposable)
            }

            override fun onSuccess(value: T) {
                try {
                    onSuccess?.invoke(value)
                } finally {
                    disposableWrapper.dispose()
                }
            }

            override fun onComplete() {
                try {
                    onComplete?.invoke()
                } finally {
                    disposableWrapper.dispose()
                }
            }

            override fun onError(error: Throwable) {
                try {
                    handleSourceError(error, onError)
                } finally {
                    disposableWrapper.dispose()
                }
            }
        },
        onError
    )

    return disposableWrapper
}