package com.dasbikash.cs_android_sdk.android_sdk.exception

class CmSessionInitiationException:RuntimeException {
    constructor() : super()
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)
    constructor(cause: Throwable?) : super(cause)
}