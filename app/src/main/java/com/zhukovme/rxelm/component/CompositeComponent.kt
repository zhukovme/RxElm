package com.zhukovme.rxelm.component

import com.zhukovme.rxelm.program.*
import io.reactivex.Single

class CompositeComponent<S : State>(
        programBuilder: ProgramBuilder,
        private var renderer: Renderable<S>
) : Component<S>, Renderable<S> {

    private val components: MutableList<Triple<PluginComponent<State>,
            ((mainState: S) -> State)?,
            ((subState: State, mainState: S) -> S)?>> = mutableListOf()

    private val program: Program<S> = programBuilder.build(this)

    fun accept(msg: Msg) {
        program.accept(msg)
    }

    fun state(): S? {
        return program.getState()
    }

    fun stop() {
        program.stop()
    }

    fun run(initialState: S, initialMsg: Msg = Init) {
        if (components.isEmpty()) {
            throw IllegalStateException("No components defined!")
        }
        program.run(initialState, initialMsg)
    }

    @Suppress("UNCHECKED_CAST")
    fun <SS : State> addComponent(
            component: PluginComponent<SS>,
            toSubStateFun: (mainState: S) -> SS,
            toMainStateFun: (subState: SS, mainState: S) -> S) {
        components.add(
                Triple(
                        component,
                        toSubStateFun,
                        toMainStateFun
                ) as Triple<PluginComponent<State>,
                        (mainState: S) -> State,
                        (subState: State, mainState: S) -> S>
        )
    }

    @Suppress("UNCHECKED_CAST")
    fun addMainComponent(component: PluginComponent<S>) {
        components.add(
                Triple(
                        component,
                        null,
                        null
                ) as Triple<PluginComponent<State>,
                        ((mainState: S) -> State)?,
                        ((subState: State, mainState: S) -> S)?>
        )
    }

    override fun render(state: S) {
        renderer.render(state)
    }

    override fun call(cmd: Cmd): Single<out Msg> {
        components.forEach { (component, _) ->
            if (component.handlesCommands(cmd)) {
                return component.call(cmd)
            }
        }
        return Single.just(Idle)
    }

    @Suppress("UNCHECKED_CAST")
    override fun update(msg: Msg, state: S): Update<S> {
        var combinedCmd = BatchCmd()
        var combinedState = state
        components.forEach { (component, toSubStateFun, toMainStateFun) ->
            if (component.handlesMessage(msg)) {
                val subState = toSubStateFun?.let { it(combinedState) } ?: combinedState
                val (componentState, componentCmd) = component.update(msg, subState)
                val updatedState = componentState ?: subState
                combinedState = toMainStateFun?.let { it(updatedState, combinedState) }
                        ?: componentState as S
                combinedCmd = combinedCmd.merge(componentCmd)
            }
        }
        return Update(combinedState, combinedCmd as Cmd)
    }
}
