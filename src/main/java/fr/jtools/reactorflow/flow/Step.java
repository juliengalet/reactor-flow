package fr.jtools.reactorflow.flow;

import fr.jtools.reactorflow.state.FlowContext;
import fr.jtools.reactorflow.state.Metadata;
import fr.jtools.reactorflow.state.State;
import fr.jtools.reactorflow.utils.TriFunction;
import reactor.core.publisher.Mono;

/**
 * An interface defining a {@link StepFlow} execution.
 *
 * @param <T> Context type
 * @param <M> Metadata type
 */
public interface Step<T extends FlowContext, M> extends TriFunction<StepFlow<T, M>, State<T>, Metadata<M>, Mono<State<T>>> {
}
