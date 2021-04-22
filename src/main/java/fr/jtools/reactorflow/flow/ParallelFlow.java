package fr.jtools.reactorflow.flow;

import fr.jtools.reactorflow.builder.ParallelFlowBuilder;
import fr.jtools.reactorflow.exception.FlowTechnicalException;
import fr.jtools.reactorflow.report.FlowContext;
import fr.jtools.reactorflow.report.Metadata;
import fr.jtools.reactorflow.report.Report;
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
   * The merge strategy used for merging all {@link T} contexts after all {@link Flow}s execution.
   */
  private final BinaryOperator<T> mergeStrategy;
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
  public static <T extends FlowContext, M> ParallelFlow<T, M> create(String name, List<Flow<T>> flows, BinaryOperator<T> mergeStrategy, Function<T, ? extends Iterable<M>> parallelizeFromArray, Flow<T> flowToParallelize) {
    return new ParallelFlow<>(name, flows, mergeStrategy, parallelizeFromArray, flowToParallelize);
  }

  private ParallelFlow(String name, List<Flow<T>> flows, BinaryOperator<T> mergeStrategy, Function<T, ? extends Iterable<M>> parallelizeFromArray, Flow<T> flowToParallelize) {
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
   * @param context  The previous {@link T} context
   * @param metadata A {@link Metadata} object
   * @return A {@link Report}
   */
  @Override
  protected final Mono<Report<T>> execution(T context, Metadata<?> metadata) {
    if (Objects.nonNull(this.parallelizeFromArray)) {
      return executeFlowOnArray(context, metadata);
    }

    return executeFlows(context, metadata);
  }

  /**
   * {@link ParallelFlow#flows} execution.
   * It executes all the {@link Flow} in {@link ParallelFlow#flows} array, in parallel.
   *
   * @param context  The previous {@link T} context
   * @param metadata A {@link Metadata} object
   * @return A {@link Report}
   */
  private Mono<Report<T>> executeFlows(T context, Metadata<?> metadata) {
    return Flux
        .merge(this.flows.stream().map(flow -> flow.execute(context, Metadata.from(metadata))).collect(Collectors.toList()))
        .collectList()
        .map(newReports -> this.mergeReports(newReports, context));
  }

  /**
   * {@link ParallelFlow#flowToParallelize} execution.
   * It executes copies of {@link ParallelFlow#flowToParallelize} for each item contains in the return of {@link ParallelFlow#flowsToParallelizeFromArray}.
   * It also adds as {@link M} metadata the item from the array.
   *
   * @param context  The previous {@link T} context
   * @param metadata A {@link Metadata} object
   * @return A {@link Report}
   */
  private Mono<Report<T>> executeFlowOnArray(T context, Metadata<?> metadata) {
    AtomicInteger counter = new AtomicInteger();

    return Mono
        .defer(() -> Mono.just(this.parallelizeFromArray.apply(context)))
        .onErrorMap(error -> new FlowTechnicalException(
            error,
            String.format("Error occurred during parallelizeFromArray evaluation: %s", error.getMessage())
        ))
        .flatMapMany(Flux::fromIterable)
        .flatMap(data -> {
          Flow<T> copiedFlow = this.flowToParallelize.cloneFlow(String.format("%s (%d)", this.flowToParallelize.getName(), counter.incrementAndGet()));
          this.flowsToParallelizeFromArray.add(copiedFlow);

          return copiedFlow.execute(
              context,
              Metadata.create(data).addErrors(metadata.getErrors()).addWarnings(metadata.getWarnings())
          );
        })
        .collectList()
        .map(newReports -> this.mergeReports(newReports, context));
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
   * Method used to merge all the {@link T} context resulting of the {@link ParallelFlow execution}.
   * The default use case is to use {@link ParallelFlowBuilder#defaultMergeStrategy()}, that will keep a random one.
   * As {@link T} context should be thread safe, all executed {@link Flow} will populate it, and we can keep any of it.
   *
   * @param newReports      The new {@link Report}s
   * @param previousContext The previous {@link T} context, returned if {@link ParallelFlow#mergeStrategy} fails
   * @return A new report {@link Report} containing the merged {@link T} context
   */
  private Report<T> mergeReports(List<Report<T>> newReports, T previousContext) {
    try {
      return Report.success(newReports
          .stream()
          .map(Report::getContext)
          .reduce(previousContext, this.mergeStrategy)
      );
    } catch (Exception exception) {
      return Report.error(previousContext, new FlowTechnicalException(
          exception,
          String.format("Error occurred during mergeStrategy evaluation: %s", exception.getMessage())
      ));
    }
  }
}
