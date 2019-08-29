package com.badoo.reaktive.utils

import platform.posix.pthread_self

internal actual val currentThreadId: Long get() = pthread_self().toLong()

internal actual val currentThreadName: String get() = "thread_$currentThreadId" // No way to get thread name in K/N Linux