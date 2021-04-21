package fr.jtools.reactorflow.flow;

import fr.jtools.reactorflow.builder.StepFlowBuilder;
import fr.jtools.reactorflow.state.FlowContext;
import fr.jtools.reactorflow.state.Metadata;
import fr.jtools.reactorflow.state.State;
import fr.jtools.reactorflow.utils.TriFunction;
import reactor.core.publisher.Mono;

/**
 * An interface defining a {@link StepFlow} execution.
 * Can be implemented in any application, with dependency injection.
 *
 * @param <T> Context type
 * @param <M> Metadata type
 */
public interface Step<T extends FlowContext, M> extends TriFunction<StepFlow<T, M>, State<T>, Metadata<M>, Mono<State<T>>> {
  /**
   * Build a {@link StepFlow} from a class instance implementing {@link Step} interface.
   *
   * @param name {@link StepFlow} name
   * @return A {@link StepFlow}
   */
  default StepFlow<T, M> buildFlowNamed(String name) {
    return StepFlowBuilder
        .<T, M>defaultBuilder()
        .named(name)
        .execution(this)
        .build();
  }

  /**
   * Static method used to a {@link StepFlow} from a class instance implementing {@link Step} and a name.
   *
   * @param step An object implementing {@link Step} interface
   * @param name {@link StepFlow} name
   * @param <T>  Context type
   * @param <M>  Metadata type
   * @return A {@link StepFlow}
   */
  static <T extends FlowContext, M> StepFlow<T, M> buildFlowNamed(Step<T, M> step, String name) {
    return StepFlowBuilder
        .<T, M>defaultBuilder()
        .named(name)
        .execution(step)
        .build();
  }
}
