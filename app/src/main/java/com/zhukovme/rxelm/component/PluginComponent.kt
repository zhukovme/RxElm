package com.zhukovme.rxelm.component

import com.zhukovme.rxelm.program.Cmd
import com.zhukovme.rxelm.program.Component
import com.zhukovme.rxelm.program.Msg
import com.zhukovme.rxelm.program.State

interface PluginComponent<S : State> : Component<S> {
    /**
     * Optional method.
     * This is useful when there several identical Components in CompositeComponent,
     * in order not to intercept messages of each other
     */
    fun handlesMessage(msg: Msg): Boolean

    /**
     * Optional method.
     * This is useful when there several identical Components in CompositeComponent,
     * in order not to intercept commands of each other
     */
    fun handlesCommands(cmd: Cmd): Boolean

    fun initialState(): S
}
