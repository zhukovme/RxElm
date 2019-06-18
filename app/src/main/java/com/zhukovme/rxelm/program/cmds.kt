package com.zhukovme.rxelm.program

import io.reactivex.Scheduler

abstract class Cmd(
    val onConflict: OnConflict = OnConflict.IgnoreByClass,
    val scheduler: Scheduler? = null
) {
    override fun toString(): String {
        return javaClass.simpleName
    }
}

object None : Cmd()
data class BatchCmd(val cmds: MutableSet<Cmd>) : Cmd() {
    constructor(vararg commands: Cmd) : this(commands.toMutableSet())

    fun merge(cmd: Cmd): BatchCmd {
        when (cmd) {
            is BatchCmd -> cmds.addAll(cmd.cmds)
            is None -> {
            }
            else -> cmds.add(cmd)
        }
        return this
    }
}

enum class OnConflict {
    IgnoreByHash,
    IgnoreByClass,
    ReplaceByHash,
    ReplaceByClass
//    Keep;
}
