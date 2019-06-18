package com.zhukovme.rxelm.program

import io.reactivex.Single

abstract class State

interface Component<S : State> {
    fun update(msg: Msg, state: S): Update<S>
    fun call(cmd: Cmd): Single<out Msg>
}

interface RenderableComponent<S : State> : Component<S> {
    fun render(newState: S, oldState: S)
}

data class Update<S : State>(
    val updatedState: S? = null,
    val cmd: Cmd = None
) {
    companion object {
        fun <S : State> idle(): Update<S> {
            return Update(null, None)
        }
    }
}
