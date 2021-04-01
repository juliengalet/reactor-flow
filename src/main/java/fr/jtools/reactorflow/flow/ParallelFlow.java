package fr.jtools.reactorflow.flow;

import fr.jtools.reactorflow.builder.ParallelFlowBuilder;
import fr.jtools.reactorflow.exception.FlowTechnicalException;
import fr.jtools.reactorflow.state.FlowContext;
import fr.jtools.reactorflow.state.Metadata;
import fr.jtools.reactorflow.state.State;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Class managing a parallel {@link Flow}.
 *
 * @param <T> Context type
 * @param <M> Metadata type
 */
public final class ParallelFlow<T extends FlowContext, M> extends Flow<T> {
  /**
   * The name.
   */
  private final String name;
  /**
   * The list of {@link Flow} to parallelize, if {@link ParallelFlow#parallelizeFromArray} is not defined.
   */
  private final List<Flow<T>> flows;
  /**
   * The merge strategy used for merging all {@link State} states after all {@link Flow}s execution.
   */
  private final BinaryOperator<State<T>> mergeStrategy;
  /**
   * The function used to extract an array from {@link T} context,
   * in order to execute {@link ParallelFlow#flowToParallelize}, with items from extracted array as {@link M} metadata.
   */
  private final Function<T, ? extends Iterable<M>> parallelizeFromArray;
  /**
   * The {@link Flow} to parallelize, if {@link ParallelFlow#parallelizeFromArray} is defined.
   */
  private final Flow<T> flowToParallelize;

  /**
   * Field used to store copied {@link Flow}s from {@link ParallelFlow#flowToParallelize} when {@link ParallelFlow#parallelizeFromArray} is defined.
   * It is used in {@link ParallelFlow#getChildren()}.
   */
  private final List<Flow<T>> flowsToParallelizeFromArray = new ArrayList<>();

  /**
   * Static method used to create a {@link ParallelFlow}.
   *
   * @param name                 {@link ParallelFlow#name}
   * @param flows                {@link ParallelFlow#flows}
   * @param mergeStrategy        {@link ParallelFlow#mergeStrategy}
   * @param parallelizeFromArray {@link ParallelFlow#parallelizeFromArray}
   * @param flowToParallelize    {@link ParallelFlow#flowToParallelize}
   * @param <T>                  Context type
   * @param <M>                  Metadata type
   * @return A {@link ParallelFlow}
   */
  public static <T extends FlowContext, M> ParallelFlow<T, M> create(String name, List<Flow<T>> flows, BinaryOperator<State<T>> mergeStrategy, Function<T, ? extends Iterable<M>> parallelizeFromArray, Flow<T> flowToParallelize) {
    return new ParallelFlow<>(name, flows, mergeStrategy, parallelizeFromArray, flowToParallelize);
  }

  private ParallelFlow(String name, List<Flow<T>> flows, BinaryOperator<State<T>> mergeStrategy, Function<T, ? extends Iterable<M>> parallelizeFromArray, Flow<T> flowToParallelize) {
    this.name = name;
    this.flows = flows;
    this.mergeStrategy = mergeStrategy;
    this.parallelizeFromArray = parallelizeFromArray;
    this.flowToParallelize = flowToParallelize;
  }

  /**
   * Get the {@link ParallelFlow} name.
   *
   * @return The name
   */
  @Override
  public final String getName() {
    return name;
  }

  /**
   * {@link ParallelFlow} execution.
   * It chooses between {@link ParallelFlow#executeFlows} and {@link ParallelFlow#executeFlowOnArray},
   * if {@link ParallelFlow#parallelizeFromArray} is defined or not.
   *
   * @param previousState The previous {@link State}
   * @param metadata      A {@link Metadata} object
   * @return The new {@link State}
   */
  @Override
  protected final Mono<State<T>> execution(State<T> previousState, Metadata<?> metadata) {
    if (Objects.nonNull(this.parallelizeFromArray)) {
      return executeFlowOnArray(previousState, metadata);
    }

    return executeFlows(previousState, metadata);
  }

  /**
   * {@link ParallelFlow#flows} execution.
   * It executes all the {@link Flow} in {@link ParallelFlow#flows} array, in parallel.
   *
   * @param previousState The previous {@link State}
   * @param metadata      A {@link Metadata} object
   * @return The new {@link State}
   */
  private Mono<State<T>> executeFlows(State<T> previousState, Metadata<?> metadata) {
    return Flux
        .merge(this.flows.stream().map(flow -> flow.execute(previousState, Metadata.from(metadata))).collect(Collectors.toList()))
        .collectList()
        .map(newReports -> this.mergeReports(newReports, previousState));
  }

  /**
   * {@link ParallelFlow#flowToParallelize} execution.
   * It executes copies of {@link ParallelFlow#flowToParallelize} for each item contains in the return of {@link ParallelFlow#flowsToParallelizeFromArray}.
   * It also adds as {@link M} metadata the item from the array.
   *
   * @param previousState The previous {@link State}
   * @param metadata      A {@link Metadata} object
   * @return The new {@link State}
   */
  private Mono<State<T>> executeFlowOnArray(State<T> previousState, Metadata<?> metadata) {
    AtomicInteger counter = new AtomicInteger();

    return Mono
        .defer(() -> Mono.just(this.parallelizeFromArray.apply(previousState.getContext())))
        .onErrorMap(error -> new FlowTechnicalException(
            this,
            error,
            String.format("Error occurred during parallelizeFromArray evaluation: %s", error.getMessage())
        ))
        .flatMapMany(Flux::fromIterable)
        .flatMap(data -> {
          Flow<T> copiedFlow = this.flowToParallelize.cloneFlow(String.format("%s (%d)", this.flowToParallelize.getName(), counter.incrementAndGet()));
          this.flowsToParallelizeFromArray.add(copiedFlow);

          return copiedFlow.execute(
              previousState,
              Metadata.create(data).addErrors(metadata.getErrors()).addWarnings(metadata.getWarnings())
          );
        })
        .collectList()
        .map(newReports -> this.mergeReports(newReports, previousState));
  }

  /**
   * Clone the {@link ParallelFlow} with a new name.
   *
   * @param newName {@link ParallelFlow} new name
   * @return Cloned {@link ParallelFlow}
   */
  @Override
  public ParallelFlow<T, M> cloneFlow(String newName) {
    return ParallelFlow.create(
        newName,
        this.flows.stream().map(Flow::cloneFlow).collect(Collectors.toList()),
        this.mergeStrategy,
        this.parallelizeFromArray,
        Objects.nonNull(this.flowToParallelize) ? this.flowToParallelize.cloneFlow() : null
    );
  }

  /**
   * Clone the {@link ParallelFlow}.
   *
   * @return Cloned {@link ParallelFlow}
   */
  @Override
  public final ParallelFlow<T, M> cloneFlow() {
    return this.cloneFlow(this.getName());
  }

  /**
   * Get {@link ParallelFlow} children, aka :
   * <ul>
   *   <li>{@link ParallelFlow#flows}, if {@link ParallelFlow#parallelizeFromArray} is defined</li>
   *   <li>
   *     {@link ParallelFlow#flowsToParallelizeFromArray} (copies of {@link ParallelFlow#flowToParallelize}),
   *     if {@link ParallelFlow#parallelizeFromArray} is not defined
   *   </li>
   * </ul>
   *
   * @return A {@link List} containing children {@link Flow}s
   */
  @Override
  protected final List<Flow<T>> getChildren() {
    return Objects.nonNull(parallelizeFromArray) ? this.flowsToParallelizeFromArray : this.flows;
  }

  /**
   * Method used to merge all the {@link State} resulting of the {@link ParallelFlow execution}.
   * The default use case is to use {@link ParallelFlowBuilder#defaultMergeStrategy()}, that will keep a random one.
   * As {@link T} context should be thread safe, all executed {@link Flow} will populate it, and we can keep any of the {@link State}.
   *
   * @param newStates     The new {@link State}s
   * @param previousState The previous {@link State}, if {@link ParallelFlow#mergeStrategy} fails
   * @return The merged {@link State}
   */
  private State<T> mergeReports(List<State<T>> newStates, State<T> previousState) {
    try {
      return newStates.stream().reduce(previousState, this.mergeStrategy);
    } catch (Exception exception) {
      this.addWarning(new FlowTechnicalException(
          this,
          exception,
          String.format("Error occurred during mergeStrategy evaluation: %s", exception.getMessage())
      ));
      return previousState;
    }
  }
}
