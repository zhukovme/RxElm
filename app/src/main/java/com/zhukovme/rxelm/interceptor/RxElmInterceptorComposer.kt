package com.zhukovme.rxelm.interceptor

import com.zhukovme.rxelm.program.Cmd
import com.zhukovme.rxelm.program.Msg
import com.zhukovme.rxelm.program.State

/**
 * Created by Michael Zhukov on 02.05.2019.
 * email: zhukovme@gmail.com
 */
class RxElmInterceptorComposer : RxElmInterceptor {

    private val interceptors: MutableSet<RxElmInterceptor> = HashSet()

    fun add(interceptor: RxElmInterceptor) {
        interceptors.add(interceptor)
    }

    override fun onInit(state: State) {
        interceptors.forEach { it.onInit(state) }
    }

    override fun onMsgReceived(msg: Msg, state: State) {
        interceptors.forEach { it.onMsgReceived(msg, state) }
    }

    override fun onUpdate(msg: Msg, cmd: Cmd, newState: State?, state: State) {
        interceptors.forEach { it.onUpdate(msg, cmd, newState, state) }
    }

    override fun onRender(state: State) {
        interceptors.forEach { it.onRender(state) }
    }

    override fun onCmdReceived(cmd: Cmd, state: State) {
        interceptors.forEach { it.onCmdReceived(cmd, state) }
    }

    override fun onCmdStarted(cmd: Cmd, state: State) {
        interceptors.forEach { it.onCmdStarted(cmd, state) }
    }

    override fun onCmdSuccess(cmd: Cmd, resultMsg: Msg, state: State) {
        interceptors.forEach { it.onCmdSuccess(cmd, resultMsg, state) }
    }

    override fun onCmdError(cmd: Cmd, error: Throwable, state: State) {
        interceptors.forEach { it.onCmdError(cmd, error, state) }
    }

    override fun onCmdCancelled(cmd: Cmd, state: State) {
        interceptors.forEach { it.onCmdCancelled(cmd, state) }
    }

    override fun onStop(state: State) {
        interceptors.forEach { it.onStop(state) }
    }
}
