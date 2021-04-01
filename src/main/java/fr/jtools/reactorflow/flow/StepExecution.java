package fr.jtools.reactorflow.flow;

import fr.jtools.reactorflow.builder.StepFlowBuilder;
import fr.jtools.reactorflow.state.FlowContext;

/**
 * Abstract class used to create custom step executions.
 * Can be extended in any application, with dependency injection.
 *
 * @param <T> Context type
 * @param <M> Metadata type
 */
public abstract class StepExecution<T extends FlowContext, M> implements Step<T, M> {
  /**
   * Static method used to a {@link StepFlow} from a {@link Step} (or a {@link StepExecution}) and a name.
   *
   * @param step An object implementing {@link Step} interface
   * @param name {@link StepFlow} name
   * @param <T>  Context type
   * @param <M>  Metadata type
   * @return A {@link StepFlow}
   */
  public static <T extends FlowContext, M> StepFlow<T, M> buildFlow(Step<T, M> step, String name) {
    return StepFlowBuilder
        .<T, M>defaultBuilder()
        .named(name)
        .execution(step)
        .build();
  }

  /**
   * Build a {@link StepFlow} from the {@link StepExecution}.
   *
   * @param name {@link StepFlow} name
   * @return A {@link StepFlow}
   */
  public final StepFlow<T, M> buildFlowNamed(String name) {
    return StepFlowBuilder
        .<T, M>defaultBuilder()
        .named(name)
        .execution(this)
        .build();
  }
}
