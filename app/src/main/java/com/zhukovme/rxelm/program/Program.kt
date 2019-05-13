package com.zhukovme.rxelm.program

import com.jakewharton.rxrelay2.BehaviorRelay
import com.zhukovme.rxelm.interceptor.RxElmInterceptor
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import java.util.*

class Program<S : State> internal constructor(
    private val msgScheduler: Scheduler,
    private val cmdScheduler: Scheduler,
    private val interceptor: RxElmInterceptor,
    private val disposableManager: DisposableManager,
    private val component: Component<S>
) {

    var isRunning: Boolean = false
        private set
    var isRendering: Boolean = false
        private set

    private var msgQueue = ArrayDeque<Msg>()
    private val msgRelay: BehaviorRelay<Msg> = BehaviorRelay.create()
    private var lock: Boolean = false

    private lateinit var state: S

    fun run(initialState: S, initialMsg: Msg = Init) {
//        if (isRunning) throw IllegalStateException()
        init(initialState)
        render(initialState)
        accept(initialMsg)
    }

    fun run(initialState: S, initialMsgs: List<Msg>) {
        init(initialState)
        render(initialState)
        initialMsgs.forEach { accept(it) }
    }

    fun getState(): S? = if (this::state.isInitialized) state else null

    fun <T : Msg> addEventObservable(eventSource: Observable<T>): Disposable {
        return eventSource.observeOn(msgScheduler)
            .subscribe { msg -> accept(msg) }
    }

    fun <T : Msg> addEventObservable(eventSource: Flowable<T>): Disposable {
        return eventSource.observeOn(msgScheduler)
            .subscribe { msg -> accept(msg) }
    }

    fun accept(msg: Msg) {
        interceptor.onMsgReceived(msg, state)
        addMsgToQueue(msg)
    }

    fun render(state: S) {
        if (component is RenderableComponent) {
            isRendering = true
            component.render(state)
            isRendering = false
            interceptor.onRender(state)
        }
    }

    fun cancelCmd(cmd: Cmd) {
        disposableManager.cancel(cmd)
    }

    fun <C : Cmd> cancelCmdByClass(clazz: Class<C>) {
        disposableManager.cancelByClass(clazz)
    }

    fun cancelCmdByHash(cmdHash: Int) {
        disposableManager.cancelByHash(cmdHash)
    }

    fun stop() {
        isRunning = false
        disposableManager.disposeAll()
        interceptor.onStop(state)
    }

    private fun init(initialState: S) {
        this.state = initialState
        val msgDisposable = createLoop(component)
        disposableManager.addMsgDisposable(msgDisposable)
        isRunning = true
        interceptor.onInit(state)
    }

    private fun createLoop(component: Component<S>): Disposable {
        return msgRelay
            .observeOn(msgScheduler)
            .subscribe { msg ->
                val update = component.update(msg, state)
                interceptor.onUpdate(msg, update.cmd, update.updatedState, state)

                handleNewState(update.updatedState)
                handleCmd(update.cmd)

                lock = false
                pollNextMsgFromQueue()
            }
    }

    private fun addMsgToQueue(msg: Msg) {
        msgQueue.addLast(msg)
        pollNextMsgFromQueue()
    }

    private fun pollNextMsgFromQueue() {
        if (lock) return
        msgQueue.pollFirst()?.let {
            lock = true
            msgRelay.accept(it)
        }
    }

    private fun handleNewState(newState: S?) {
        newState?.let {
            if (it !== state) {
                render(newState)
                state = it
            }
        }
    }

    private fun handleCmd(cmd: Cmd) {
        interceptor.onCmdReceived(cmd, state)
        when (cmd) {
            is None -> return
            is BatchCmd -> cmd.cmds.forEach { handleCmd(it) }
            else -> {
                if (handleOnConflict(cmd)) return
                val cmdObservable = call(cmd)
                val cmdDisposable = handleResponse(cmd, cmdObservable)
                disposableManager.addCmdDisposable(cmd, cmdDisposable)
            }
        }
    }

    private fun handleOnConflict(cmd: Cmd): Boolean {
        return when (cmd.onConflict) {
            OnConflict.IgnoreByHash -> disposableManager.isCmdAlive(cmd)
            OnConflict.IgnoreByClass -> disposableManager.isCmdClassAlive(cmd)
            OnConflict.ReplaceByHash -> {
                disposableManager.cancel(cmd)
                false
            }
            OnConflict.ReplaceByClass -> {
                disposableManager.cancelByClass(cmd)
                false
            }
        }
    }

    private fun call(cmd: Cmd): Single<out Msg> {
        return component.call(cmd)
            .doOnSubscribe { interceptor.onCmdStarted(cmd, state) }
            .doOnSuccess { interceptor.onCmdSuccess(cmd, it, state) }
            .doOnError { interceptor.onCmdError(cmd, it, state) }
            .doOnDispose { interceptor.onCmdCancelled(cmd, state) }
            .doFinally { disposableManager.removeCmdDisposable(cmd) }
            .subscribeOn(cmd.scheduler ?: cmdScheduler)
    }

    private fun handleResponse(cmd: Cmd, single: Single<out Msg>): Disposable {
        return single
            .observeOn(msgScheduler)
            .subscribe({ msg -> if (msg !is Idle) addMsgToQueue(msg) },
                { error -> addMsgToQueue(ErrorMsg(error, cmd)) })
    }
}
