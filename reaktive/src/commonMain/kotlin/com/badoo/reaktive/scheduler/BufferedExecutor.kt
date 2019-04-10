package com.badoo.reaktive.scheduler

import com.badoo.reaktive.utils.queue.ArrayQueue
import com.badoo.reaktive.utils.queue.isNotEmpty
import com.badoo.reaktive.utils.queue.take
import com.badoo.reaktive.utils.lock.newLock
import com.badoo.reaktive.utils.lock.synchronized

internal class BufferedExecutor<in T>(
    private val executor: Scheduler.Executor,
    private val onNext: (T) -> Unit
) {

    private val queue = ArrayQueue<T>()
    private var isDraining = false
    private val lock = newLock()
    private val drainFunction = ::drain

    fun submit(value: T) {
        lock.synchronized {
            queue.offer(value)
            if (!isDraining) {
                isDraining = true
                executor.submit(0, drainFunction)
            }
        }
    }

    private fun drain() {
        while (!executor.isDisposed) {
            lock
                .synchronized {
                    if (queue.isNotEmpty) {
                        queue.take()
                    } else {
                        isDraining = false
                        return
                    }
                }
                .also(onNext)
        }
    }
}
