package com.badoo.reaktive.utils

import com.badoo.reaktive.utils.atomic.AtomicList
import com.badoo.reaktive.utils.atomic.getAndUpdate
import com.badoo.reaktive.utils.atomic.update
import kotlin.system.getTimeMillis

internal class DelayQueue<T : Any> {

    private val lock = Lock()
    private val condition = lock.newCondition()
    private val queue: AtomicList<Holder<T>> = AtomicList(emptyList(), true)

    fun destroy() {
        condition.destroy()
        lock.destroy()
    }

    fun removeFirst(): T? {
        lock.synchronized {
            val item: T? =
                queue
                    .getAndUpdate { it.drop(1) }
                    .firstOrNull()
                    ?.value

            if (item != null) {
                condition.signal()
            }

            return item
        }
    }

    fun take(): T {
        lock.acquire()
        try {
            while (true) {
                val item: Holder<T>? = queue.value.firstOrNull()
                if (item == null) {
                    condition.await(Long.MAX_VALUE)
                } else {
                    val timeout = item.endTimeMillis.minus(getTimeMillis())

                    if (timeout <= 0L) {
                        queue.update { it.drop(1) }

                        return item.value
                    }

                    condition.await(timeout)
                }
            }
        } finally {
            lock.release()
        }
    }

    fun offer(value: T, timeoutMillis: Long) {
        val holder = Holder(value, getTimeMillis() + timeoutMillis)
        lock.synchronized {
            queue.update { it.plusSorted(holder, HolderComparator) }
            condition.signal()
        }
    }

    private class Holder<out T>(
        val value: T,
        val endTimeMillis: Long
    )

    private object HolderComparator : Comparator<Holder<*>> {
        override fun compare(a: Holder<*>, b: Holder<*>): Int = a.endTimeMillis.compareTo(b.endTimeMillis)
    }
}
