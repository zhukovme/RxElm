package com.zhukovme.rxelm.program

import io.reactivex.Single

open class State

interface Component<S : State> {
    fun update(msg: Msg, state: S): Update<S>
    fun call(cmd: Cmd): Single<out Msg>
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

interface RenderableComponent<S : State> : Renderable<S>, Component<S>

interface Renderable<S : State> {
    fun render(state: S)
}
