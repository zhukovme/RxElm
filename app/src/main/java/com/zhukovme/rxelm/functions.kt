package com.zhukovme.rxelm

import com.zhukovme.rxelm.program.BatchCmd
import com.zhukovme.rxelm.program.Cmd
import com.zhukovme.rxelm.program.Idle
import io.reactivex.Completable
import io.reactivex.Single

fun Completable.asStatelessEffect(): Single<Idle> = this.andThen(Single.just(Idle))

inline fun statelessEffect(crossinline operations: () -> Unit): Single<Idle> {
    return Single.fromCallable {
        operations()
    }.map { Idle }
}

fun cmds(vararg cmds: Cmd): BatchCmd {
    return BatchCmd(cmds = cmds.toMutableSet())
}
