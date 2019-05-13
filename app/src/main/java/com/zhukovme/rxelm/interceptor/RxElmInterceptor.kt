package com.zhukovme.rxelm.interceptor

import com.zhukovme.rxelm.program.Cmd
import com.zhukovme.rxelm.program.Msg
import com.zhukovme.rxelm.program.State

/**
 * Created by Michael Zhukov on 02.05.2019.
 * email: zhukovme@gmail.com
 */
@Suppress("ComplexInterface")
interface RxElmInterceptor {
    fun onInit(state: State)
    fun onMsgReceived(msg: Msg, state: State)
    fun onUpdate(msg: Msg, cmd: Cmd, newState: State?, state: State)
    fun onRender(state: State)
    fun onCmdReceived(cmd: Cmd, state: State)
    fun onCmdStarted(cmd: Cmd, state: State)
    fun onCmdSuccess(cmd: Cmd, resultMsg: Msg, state: State)
    fun onCmdError(cmd: Cmd, error: Throwable, state: State)
    fun onCmdCancelled(cmd: Cmd, state: State)
    fun onStop(state: State)
}
