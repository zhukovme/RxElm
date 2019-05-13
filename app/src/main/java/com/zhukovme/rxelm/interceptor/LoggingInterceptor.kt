package com.zhukovme.rxelm.interceptor

import com.zhukovme.rxelm.program.Cmd
import com.zhukovme.rxelm.program.Msg
import com.zhukovme.rxelm.program.State

@Suppress("StringLiteralDuplication")
abstract class LoggingInterceptor : RxElmInterceptor {

    abstract fun log(tag: String = "RxElm", message: String, error: Throwable? = null)

    override fun onInit(state: State) {
        log(message = "onInit - state: $state")
    }

    override fun onMsgReceived(msg: Msg, state: State) {
        log(message = "onMsgReceived - msg: $msg" +
                ", state: $state")
    }

    override fun onUpdate(msg: Msg, cmd: Cmd, newState: State?, state: State) {
        log(message = "onUpdate - msg: $msg" +
                ", cmd: $cmd" +
                ", onConflict: ${cmd.onConflict}" +
                ", newState: $newState, state: $state")
    }

    override fun onRender(state: State) {
        log(message = "onRender - state: $state")
    }

    override fun onCmdReceived(cmd: Cmd, state: State) {
        log(message = "onCmdReceived - cmd: $cmd" +
                ", onConflict: ${cmd.onConflict}" +
                ", state: $state")
    }

    override fun onCmdStarted(cmd: Cmd, state: State) {
        log(message = "onCmdStarted - cmd: $cmd" +
                ", onConflict: ${cmd.onConflict}" +
                ", state: $state")
    }

    override fun onCmdSuccess(cmd: Cmd, resultMsg: Msg, state: State) {
        log(message = "onCmdSuccess - cmd: $cmd" +
                ", onConflict: ${cmd.onConflict}" +
                ", resultMsg: $resultMsg" +
                ", state: $state")
    }

    override fun onCmdError(cmd: Cmd, error: Throwable, state: State) {
        log(message = "onCmdError - cmd: $cmd" +
                ", onConflict: ${cmd.onConflict}" +
                ", state: $state",
                error = error)
    }

    override fun onCmdCancelled(cmd: Cmd, state: State) {
        log(message = "onCmdCancelled - cmd: $cmd" +
                ", onConflict: ${cmd.onConflict}" +
                ", state: $state")
    }

    override fun onStop(state: State) {
        log(message = "onStop - state: $state")
    }
}
