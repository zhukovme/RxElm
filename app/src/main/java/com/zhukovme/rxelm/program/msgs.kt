package com.zhukovme.rxelm.program

sealed class AbstractMsg

open class Msg : AbstractMsg() {
    override fun toString(): String {
        return this.javaClass.simpleName
    }
}

object Idle : Msg()
object Init : Msg()
data class ErrorMsg(val error: Throwable, val cmd: Cmd) : Msg() {
    override fun toString(): String {
        return "${super.toString()} error: ${error.javaClass.simpleName}  cmd: $cmd"
    }
}
