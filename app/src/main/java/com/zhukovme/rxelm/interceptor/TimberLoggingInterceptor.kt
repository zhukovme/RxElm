package com.zhukovme.rxelm.interceptor

import timber.log.Timber

/**
 * Created by Michael Zhukov on 06.06.2019.
 * email: zhukovme@gmail.com
 */
class TimberLoggingInterceptor : LoggingInterceptor() {
    override fun log(tag: String, message: String, error: Throwable?, event: RxElmEvent) {
        Timber.tag(tag).d(message)
        error?.let { Timber.tag(tag).e(error) }
    }
}
