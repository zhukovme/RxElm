package com.zhukovme.rxelm.interceptor

/**
 * Created by Michael Zhukov on 02.05.2019.
 * email: zhukovme@gmail.com
 */
class RxElmInterceptorComposer : RxElmInterceptor {

    private val interceptors: MutableSet<RxElmInterceptor> = HashSet()

    fun add(interceptor: RxElmInterceptor) {
        interceptors.add(interceptor)
    }

    override fun onEvent(event: RxElmEvent) {
        interceptors.forEach { it.onEvent(event) }
    }
}
