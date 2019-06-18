package com.zhukovme.rxelm.interceptor

abstract class LoggingInterceptor : RxElmInterceptor {

    abstract fun log(tag: String = "RxElm", message: String, error: Throwable? = null, event: RxElmEvent)

    override fun onEvent(event: RxElmEvent) {
        val error = when (event) {
            is OnMsgError -> event.error
            is OnCmdError -> event.error
            else -> null
        }
        log(message = event.toString(), error = error, event = event)
    }
}
