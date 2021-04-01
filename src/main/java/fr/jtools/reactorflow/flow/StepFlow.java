package fr.jtools.reactorflow.flow;

import fr.jtools.reactorflow.exception.FlowTechnicalException;
import fr.jtools.reactorflow.state.FlowContext;
import fr.jtools.reactorflow.state.Metadata;
import fr.jtools.reactorflow.state.State;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

/**
 * Class managing a step {@link Flow} (aka a real execution, not other {@link Flow}s management).
 *
 * @param <T> Context type
 * @param <M> Metadata type
 */
public final class StepFlow<T extends FlowContext, M> extends Flow<T> {
  /**
   * The execution.
   */
  private final Step<T, M> execution;
  /**
   * The name.
   */
  private final String name;

  /**
   * Static method used to create a {@link StepFlow}.
   *
   * @param name      {@link StepFlow#name}
   * @param execution {@link StepFlow#execution}
   * @param <T>       Context type
   * @param <M>       Metadata type
   * @return A {@link StepFlow}
   */
  public static <T extends FlowContext, M> StepFlow<T, M> create(String name, Step<T, M> execution) {
    return new StepFlow<>(name, execution);
  }

  private StepFlow(String name, Step<T, M> execution) {
    this.name = name;
    this.execution = execution;
  }

  /**
   * Get the {@link StepFlow} name.
   *
   * @return The name
   */
  @Override
  public final String getName() {
    return name;
  }

  /**
   * Clone the {@link StepFlow} with a new name.
   *
   * @param newName {@link StepFlow} new name
   * @return Cloned {@link StepFlow}
   */
  @Override
  public final StepFlow<T, M> cloneFlow(String newName) {
    return StepFlow.create(newName, this.execution);
  }

  /**
   * Clone the {@link StepFlow}.
   *
   * @return Cloned {@link StepFlow}
   */
  @Override
  public final StepFlow<T, M> cloneFlow() {
    return this.cloneFlow(this.getName());
  }

  /**
   * {@link ParallelFlow} execution.
   * Executes {@link StepFlow#execution}.
   *
   * @param previousState The previous {@link State}
   * @param metadata      A {@link Metadata} object
   * @return The new {@link State}
   */
  @Override
  @SuppressWarnings("unchecked")
  protected final Mono<State<T>> execution(State<T> previousState, Metadata<?> metadata) {
    return Mono
        .defer(() -> Mono.just(((Metadata<M>) metadata)))
        .flatMap(meta -> execution.apply(
            this,
            previousState,
            Metadata.create(meta.getData()).addErrors(meta.getErrors()).addWarnings(meta.getWarnings())
        ))
        .onErrorResume(ClassCastException.class, error -> {
          this.addError(new FlowTechnicalException(this, error, String.format("Can not convert metadata to target type: %s", error.getMessage().split(" \\(")[0])));
          return Mono.just(previousState);
        });
  }

  /**
   * Get {@link StepFlow} children, aka {@link Collections#emptyList()}.
   *
   * @return An empty list
   */
  @Override
  protected final List<Flow<T>> getChildren() {
    return Collections.emptyList();
  }
}
