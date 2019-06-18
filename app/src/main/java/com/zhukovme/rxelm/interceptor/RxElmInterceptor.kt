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
    fun onEvent(event: RxElmEvent)
}

sealed class RxElmEvent

data class OnInit(val state: State) : RxElmEvent()

data class OnMsgReceived(val msg: Msg, val state: State) : RxElmEvent()

data class OnMsgError(val msg: Msg, val error: Throwable, val state: State) : RxElmEvent()

data class OnIgnoreMsg(val ignoredMsg: Msg, val state: State) : RxElmEvent()

data class OnUpdate(val msg: Msg, val cmd: Cmd, val newState: State?, val state: State) : RxElmEvent()

data class OnRender(val newState: State, val oldState: State) : RxElmEvent()

data class OnCmdReceived(val cmd: Cmd, val state: State) : RxElmEvent()

data class OnCmdStarted(val cmd: Cmd, val state: State) : RxElmEvent()

data class OnCmdSuccess(val cmd: Cmd, val resultMsg: Msg, val state: State) : RxElmEvent()

data class OnCmdError(val cmd: Cmd, val error: Throwable, val state: State) : RxElmEvent()

data class OnCmdCancelled(val cmd: Cmd, val state: State) : RxElmEvent()

data class OnStop(val state: State) : RxElmEvent()
