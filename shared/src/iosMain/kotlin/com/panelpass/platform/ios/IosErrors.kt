package com.panelpass.platform.ios

/** For Swift: call `IosErrorsKt.throwable(message:)` to build a [Throwable] for callbacks. */
public fun throwable(message: String): Throwable = IllegalStateException(message)
