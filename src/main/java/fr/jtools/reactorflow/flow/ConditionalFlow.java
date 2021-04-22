package fr.jtools.reactorflow.flow;

import fr.jtools.reactorflow.exception.FlowTechnicalException;
import fr.jtools.reactorflow.report.FlowContext;
import fr.jtools.reactorflow.report.Metadata;
import fr.jtools.reactorflow.report.Report;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Predicate;

/**
 * Class managing a conditional {@link Flow}.
 *
 * @param <T> Context type
 */
public final class ConditionalFlow<T extends FlowContext> extends Flow<T> {
  /**
   * The name.
   */
  private final String name;
  /**
   * {@link Flow} executed if {@link ConditionalFlow#condition} returns true.
   */
  private final Flow<T> flowCaseTrue;
  /**
   * {@link Flow} executed if {@link ConditionalFlow#condition} returns false.
   */
  private final Flow<T> flowCaseFalse;
  /**
   * The condition.
   */
  private final Predicate<T> condition;

  /**
   * Static method used to create a {@link ConditionalFlow}.
   *
   * @param name          {@link ConditionalFlow#name}
   * @param condition     {@link ConditionalFlow#condition}
   * @param flowCaseTrue  {@link ConditionalFlow#flowCaseTrue}
   * @param flowCaseFalse {@link ConditionalFlow#flowCaseFalse}
   * @param <T>           Context type
   * @return A {@link ConditionalFlow}
   */
  public static <T extends FlowContext> ConditionalFlow<T> create(String name, Predicate<T> condition, Flow<T> flowCaseTrue, Flow<T> flowCaseFalse) {
    return new ConditionalFlow<>(name, condition, flowCaseTrue, flowCaseFalse);
  }

  private ConditionalFlow(String name, Predicate<T> condition, Flow<T> flowCaseTrue, Flow<T> flowCaseFalse) {
    this.name = name;
    this.condition = condition;
    this.flowCaseTrue = flowCaseTrue;
    this.flowCaseFalse = flowCaseFalse;
  }

  /**
   * Get the {@link ConditionalFlow} name.
   *
   * @return The name
   */
  @Override
  public final String getName() {
    return name;
  }

  /**
   * {@link ConditionalFlow} execution.
   * It chooses between {@link ConditionalFlow#flowCaseTrue} and {@link ConditionalFlow#flowCaseFalse} depending on {@link ConditionalFlow#condition}.
   *
   * @param context  The previous {@link T} context
   * @param metadata A {@link Metadata} object
   * @return A {@link Report}
   */
  @Override
  protected final Mono<Report<T>> execution(T context, Metadata<?> metadata) {
    return Mono
        .defer(() -> Mono.just(this.condition.test(context)))
        .onErrorMap(error -> new FlowTechnicalException(
            error,
            String.format("Error occurred during condition evaluation: %s", error.getMessage())
        ))
        .flatMap(result -> Boolean.TRUE.equals(result) ?
            this.flowCaseTrue.execute(context, Metadata.from(metadata)) :
            this.flowCaseFalse.execute(context, Metadata.from(metadata))
        );
  }

  /**
   * Clone the {@link ConditionalFlow} with a new name.
   *
   * @param newName {@link ConditionalFlow} new name
   * @return Cloned {@link ConditionalFlow}
   */
  @Override
  public ConditionalFlow<T> cloneFlow(String newName) {
    return ConditionalFlow.create(
        newName,
        this.condition,
        this.flowCaseTrue.cloneFlow(),
        this.flowCaseFalse.cloneFlow()
    );
  }

  /**
   * Clone the {@link ConditionalFlow}.
   *
   * @return Cloned {@link ConditionalFlow}
   */
  @Override
  public ConditionalFlow<T> cloneFlow() {
    return this.cloneFlow(this.getName());
  }

  /**
   * Get {@link ConditionalFlow} children, aka the {@link ConditionalFlow#flowCaseTrue} and {@link ConditionalFlow#flowCaseFalse}.
   *
   * @return A {@link List} containing children {@link Flow}s
   */
  @Override
  protected final List<Flow<T>> getChildren() {
    return List.of(this.flowCaseTrue, this.flowCaseFalse);
  }

  /**
   * Has error policy : a {@link ConditionalFlow} is in error if none of its children succeeded.
   *
   * @return A {@link Boolean}
   */
  @Override
  protected boolean flowOrChildrenHasError() {
    return !FlowStatusPolicy.flowAndOneChildSucceeded().test(this);
  }
}
