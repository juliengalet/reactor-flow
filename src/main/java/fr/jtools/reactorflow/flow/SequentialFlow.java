package fr.jtools.reactorflow.flow;

import fr.jtools.reactorflow.state.FlowContext;
import fr.jtools.reactorflow.state.Metadata;
import fr.jtools.reactorflow.state.State;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Class managing a sequential {@link Flow}.
 *
 * @param <T> Context type
 */
public final class SequentialFlow<T extends FlowContext> extends Flow<T> {
  /**
   * {@link List} of {@link Flow}s to execute sequentially.
   */
  private final List<Flow<T>> flows;
  /**
   * Final {@link Flow} to execute, even if a previous {@link Flow} has an error.
   */
  private final Flow<T> finalFlow;
  /**
   * The name.
   */
  private final String name;

  /**
   * Static method used to create a {@link SequentialFlow}.
   *
   * @param name      {@link SequentialFlow#name}
   * @param flows     {@link SequentialFlow#flows}
   * @param finalFlow {@link SequentialFlow#finalFlow}
   * @param <T>       Context type
   * @return A {@link SequentialFlow}
   */
  public static <T extends FlowContext> SequentialFlow<T> create(String name, List<Flow<T>> flows, Flow<T> finalFlow) {
    return new SequentialFlow<>(name, flows, finalFlow);
  }

  private SequentialFlow(String name, List<Flow<T>> flows, Flow<T> finalFlow) {
    this.name = name;
    this.flows = flows;
    this.finalFlow = finalFlow;
  }

  /**
   * Get the {@link SequentialFlow} name.
   *
   * @return The name
   */
  @Override
  public final String getName() {
    return name;
  }

  /**
   * {@link SequentialFlow} execution.
   * It sequentially executes {@link SequentialFlow#flows},
   * and executes {@link SequentialFlow#finalFlow} when all {@link SequentialFlow#flows} are executed,
   * or after the first one with an error.
   *
   * @param previousState The previous {@link State}
   * @param metadata      A {@link Metadata} object
   * @return The new {@link State}
   */
  @Override
  protected final Mono<State<T>> execution(State<T> previousState, Metadata<?> metadata) {
    Mono<State<T>> newState = Mono.just(previousState);

    for (Flow<T> flow : this.flows) {
      newState = newState
          .flatMap(state -> this.executeFlow(flow, state, Metadata.from(metadata)));
    }

    return newState
        .flatMap(stateBeforeFinalFlow -> Objects.isNull(this.finalFlow) ?
            Mono.just(stateBeforeFinalFlow) :
            this.executeFinalFlow(
                this.finalFlow,
                stateBeforeFinalFlow,
                Metadata.from(metadata)
                    .addErrors(this.getErrorsForFlowAndChildren())
                    .addWarnings(this.getWarningsForFlowAndChildren())
            )
        );
  }

  /**
   * Clone the {@link SequentialFlow} with a new name.
   *
   * @param newName {@link SequentialFlow} new name
   * @return Cloned {@link SequentialFlow}
   */
  @Override
  public final SequentialFlow<T> cloneFlow(String newName) {
    return SequentialFlow.create(
        newName,
        this.flows.stream().map(Flow::cloneFlow).collect(Collectors.toList()),
        Objects.nonNull(this.finalFlow) ? this.finalFlow.cloneFlow() : null
    );
  }

  /**
   * Clone the {@link SequentialFlow}.
   *
   * @return Cloned {@link SequentialFlow}
   */
  @Override
  public final SequentialFlow<T> cloneFlow() {
    return this.cloneFlow(this.getName());
  }

  /**
   * Get {@link SequentialFlow} children, aka :
   * <ul>
   *   <li>{@link SequentialFlow#flows}</li>
   *   <li>{@link SequentialFlow#finalFlow}, if defined</li>
   * </ul>
   *
   * @return A {@link List} containing children {@link Flow}s
   */
  @Override
  protected final List<Flow<T>> getChildren() {
    List<Flow<T>> children = new ArrayList<>(this.flows);

    if (Objects.nonNull(this.finalFlow)) {
      children.add(this.finalFlow);
    }

    return children;
  }

  /**
   * Executes the next {@link Flow},
   * or return the previous {@link State} if an error had occurred previously in the {@link SequentialFlow}.
   *
   * @param flow          The next {@link Flow}
   * @param previousState The previous {@link State}
   * @param metadata      A {@link Metadata} object
   * @return The new {@link State}
   */
  private Mono<State<T>> executeFlow(Flow<T> flow, State<T> previousState, Metadata<?> metadata) {
    if (!this.getErrorsForFlowAndChildren().isEmpty()) {
      return Mono.just(previousState);
    }

    return flow.execute(previousState, metadata);
  }

  /**
   * Executes the final {@link Flow}.
   *
   * @param finalFlow     The final {@link Flow}
   * @param previousState The previous {@link State}
   * @param metadata      A {@link Metadata} object
   * @return The new {@link State}
   */
  private Mono<State<T>> executeFinalFlow(Flow<T> finalFlow, State<T> previousState, Metadata<?> metadata) {
    return finalFlow.execute(previousState, metadata);
  }
}
