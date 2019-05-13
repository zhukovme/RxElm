package com.zhukovme.rxelm

import com.zhukovme.rxelm.program.BatchCmd
import com.zhukovme.rxelm.program.Cmd
import com.zhukovme.rxelm.program.Idle
import com.zhukovme.rxelm.program.Msg
import io.reactivex.Single

inline fun statelessEffect(crossinline operations: () -> Unit): Single<Msg> {
    return Single.fromCallable {
        operations()
    }.map { Idle }
}

fun cmds(vararg cmds: Cmd): BatchCmd {
    return BatchCmd(cmds = cmds.toMutableSet())
}
