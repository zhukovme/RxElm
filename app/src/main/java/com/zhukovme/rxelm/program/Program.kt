package com.zhukovme.rxelm.program

import com.jakewharton.rxrelay2.BehaviorRelay
import com.zhukovme.rxelm.interceptor.*
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import timber.log.Timber
import java.util.concurrent.ConcurrentLinkedQueue

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

    private var msgQueue = ConcurrentLinkedQueue<Msg>()
    private val msgRelay: BehaviorRelay<Msg> = BehaviorRelay.create()
    private var lock: Boolean = false

    private lateinit var state: S

    fun run(initialState: S, initialMsg: Msg = Init) {
        init(initialState)
        accept(initialMsg)
        render(initialState, state)
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
        intercept(OnMsgReceived(msg, state))
        addMsgToQueue(msg)
    }

    fun render(newState: S, oldState: S) {
        if (component is RenderableComponent) {
            intercept(OnRender(newState, oldState))
            isRendering = true
            component.render(newState, oldState)
            isRendering = false
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
        intercept(OnStop(state))
        isRunning = false
        disposableManager.disposeAll()
    }

    private fun init(initialState: S) {
        if (isRunning) throw IllegalStateException("The Program is already running")
        isRunning = true
        this.state = initialState
        intercept(OnInit(state))
        val msgDisposable = createLoop(component)
        disposableManager.addMsgDisposable(msgDisposable)
    }

    private fun createLoop(component: Component<S>): Disposable {
        return msgRelay
            .observeOn(msgScheduler)
            .subscribe { msg ->
                try {
                    val update = component.update(msg, state)

                    if (update.updatedState == null && update.cmd is None) intercept(OnIgnoreMsg(msg, state))
                    else intercept(OnUpdate(msg, update.cmd, update.updatedState, state))

                    handleNewState(update.updatedState)
                    handleCmd(update.cmd)
                } catch (error: Throwable) {
                    intercept(OnMsgError(msg, error, state))
                } finally {
                    lock = false
                    pollNextMsgFromQueue()
                }
            }
    }

    private fun addMsgToQueue(msg: Msg) {
        msgQueue.add(msg)
        pollNextMsgFromQueue()
    }

    private fun pollNextMsgFromQueue() {
        if (lock) return
        msgQueue.poll()?.let {
            lock = true
            msgRelay.accept(it)
        }
    }

    private fun handleNewState(newState: S?) {
        newState?.let {
            if (it !== state) {
                val oldState = state
                state = it
                render(newState, oldState)
            }
        }
    }

    private fun handleCmd(cmd: Cmd) {
        if (cmd is None) return
        intercept(OnCmdReceived(cmd, state))
        when (cmd) {
            is BatchCmd -> cmd.cmds.forEach { handleCmd(it) }
            is CancelAll -> disposableManager.cancelCmds()
            else -> {
                if (shouldIgnoreCmd(cmd)) return
                val cmdObservable = call(cmd)
                val cmdDisposable = handleResponse(cmd, cmdObservable)
                disposableManager.addCmdDisposable(cmd, cmdDisposable)
            }
        }
    }

    private fun shouldIgnoreCmd(cmd: Cmd): Boolean {
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
            .doOnSubscribe { intercept(OnCmdStarted(cmd, state)) }
            .doOnSuccess { intercept(OnCmdSuccess(cmd, it, state)) }
            .doOnError { intercept(OnCmdError(cmd, it, state)) }
            .doOnDispose { intercept(OnCmdCancelled(cmd, state)) }
            .doFinally { disposableManager.removeCmdDisposable(cmd) }
            .subscribeOn(cmd.scheduler ?: cmdScheduler)
    }

    private fun handleResponse(cmd: Cmd, single: Single<out Msg>): Disposable {
        return single
            .observeOn(msgScheduler)
            .subscribe({ msg -> if (msg !is Idle) addMsgToQueue(msg) },
                { error -> addMsgToQueue(ErrorMsg(error, cmd)) })
    }

    private fun intercept(event: RxElmEvent) {
        try {
            interceptor.onEvent(event)
        } catch (error: Exception) {
            Timber.e(error)
        }
    }
}
