package fr.jtools.reactorflow.flow;

import fr.jtools.reactorflow.builder.StepFlowBuilder;
import fr.jtools.reactorflow.report.FlowContext;
import fr.jtools.reactorflow.report.Metadata;
import fr.jtools.reactorflow.report.Report;
import reactor.core.publisher.Mono;

import java.util.function.BiFunction;

/**
 * An interface defining a {@link StepFlow} execution.
 * Can be implemented in any application, with dependency injection.
 *
 * @param <T> Context type
 * @param <M> Metadata type
 */
public interface Step<T extends FlowContext, M> extends BiFunction<T, Metadata<M>, Mono<Report<T>>> {
  /**
   * Build a {@link StepFlow} from a class instance implementing {@link Step} interface.
   *
   * @param name {@link StepFlow} name
   * @return A {@link StepFlow}
   */
  default StepFlow<T, M> build(String name) {
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
  static <T extends FlowContext, M> StepFlow<T, M> build(Step<T, M> step, String name) {
    return StepFlowBuilder
        .<T, M>defaultBuilder()
        .named(name)
        .execution(step)
        .build();
  }
}
