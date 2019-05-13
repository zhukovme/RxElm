package com.zhukovme.rxelm.program

import com.zhukovme.rxelm.interceptor.RxElmInterceptor
import com.zhukovme.rxelm.interceptor.RxElmInterceptorComposer
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers

class ProgramBuilder {

    private var msgScheduler: Scheduler = Schedulers.io()
    private var cmdScheduler: Scheduler = Schedulers.io()
    private val interceptor = RxElmInterceptorComposer()
    private val disposableManager = DisposableManager()

    fun msgScheduler(scheduler: Scheduler): ProgramBuilder {
        this.msgScheduler = scheduler
        return this
    }

    fun cmdScheduler(scheduler: Scheduler): ProgramBuilder {
        this.cmdScheduler = scheduler
        return this
    }

    fun interceptor(interceptor: RxElmInterceptor): ProgramBuilder {
        this.interceptor.add(interceptor)
        return this
    }

    fun <S : State> build(component: Component<S>): Program<S> {
        return Program(msgScheduler, cmdScheduler, interceptor, disposableManager, component)
    }
}
