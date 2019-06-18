package com.zhukovme.rxelm.program

import io.reactivex.disposables.Disposable

/**
 * Created by Michael Zhukov on 04.05.2019.
 * email: zhukovme@gmail.com
 */
class DisposableManager {

    private var msgDisposable: Disposable? = null
    private val cmdDisposablesMap: MutableMap<Int, MutableMap<Int, Disposable>> = HashMap()

    fun addMsgDisposable(disposable: Disposable) {
        msgDisposable = disposable
    }

    fun addCmdDisposable(cmd: Cmd, disposable: Disposable) {
        val cmdClass = getCmdClass(cmd)
        val cmdHash = getCmdHash(cmd)

        var cmdDisposablesClassMap = cmdDisposablesMap[cmdClass]
        if (cmdDisposablesClassMap == null) {
            cmdDisposablesClassMap = HashMap()
            cmdDisposablesMap[cmdClass] = cmdDisposablesClassMap
        }
        cmdDisposablesClassMap[cmdHash] = disposable
    }

    fun isCmdAlive(cmd: Cmd): Boolean {
        val cmdClass = getCmdClass(cmd)
        val cmdHash = getCmdHash(cmd)

        return cmdDisposablesMap[cmdClass]?.get(cmdHash)?.isDisposed == false
    }

    fun isCmdClassAlive(cmd: Cmd): Boolean {
        val cmdClass = getCmdClass(cmd)
        cmdDisposablesMap[cmdClass]?.values?.forEach { if (!it.isDisposed) return true }
        return false
    }

    fun cancel(cmd: Cmd) {
        val cmdClass = getCmdClass(cmd)
        val cmdHash = getCmdHash(cmd)

        cmdDisposablesMap[cmdClass]?.get(cmdHash)?.dispose()
    }

    fun <C : Cmd> cancelByClass(clazz: Class<C>) {
        val cmdClass = clazz.hashCode()
        cmdDisposablesMap[cmdClass]?.values?.forEach { it.dispose() }
    }

    fun cancelByClass(cmd: Cmd) {
        cancelByClass(cmd.javaClass)
    }

    fun cancelByHash(cmdHash: Int) {
        cmdDisposablesMap.values.forEach { cmdDisposablesClassMap ->
            cmdDisposablesClassMap[cmdHash]?.dispose()
        }
    }

    fun cancelCmds() {
        cmdDisposablesMap.values.forEach { cmdDisposablesClassMap ->
            cmdDisposablesClassMap.values.forEach { it.dispose() }
        }
    }

    fun removeCmdDisposable(cmd: Cmd) {
        val cmdClass = getCmdClass(cmd)
        val cmdHash = getCmdHash(cmd)

        cmdDisposablesMap[cmdClass]?.remove(cmdHash)
    }

    fun disposeAll() {
        msgDisposable?.dispose()
        cancelCmds()
    }

    private fun getCmdClass(cmd: Cmd) = cmd.javaClass.hashCode()

    private fun getCmdHash(cmd: Cmd) = cmd.hashCode()
}
