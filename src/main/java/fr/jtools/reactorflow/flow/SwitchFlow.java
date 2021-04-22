package fr.jtools.reactorflow.flow;

import fr.jtools.reactorflow.exception.FlowTechnicalException;
import fr.jtools.reactorflow.report.FlowContext;
import fr.jtools.reactorflow.report.Metadata;
import fr.jtools.reactorflow.report.Report;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Class managing a switch {@link Flow}.
 *
 * @param <T> Context type
 */
public final class SwitchFlow<T extends FlowContext> extends Flow<T> {
  /**
   * The name.
   */
  private final String name;
  /**
   * A {@link Map} containing switch case keys and {@link Flow}s to execute.
   */
  private final Map<String, Flow<T>> flows;
  /**
   * The default {@link Flow} is no switch case matches.
   */
  private final Flow<T> defaultFlow;
  /**
   * The switch condition.
   */
  private final Function<T, String> switchCondition;

  /**
   * Static method used to create a {@link SwitchFlow}.
   *
   * @param name            {@link SwitchFlow#name}
   * @param switchCondition {@link SwitchFlow#switchCondition}
   * @param flows           {@link SwitchFlow#flows}
   * @param defaultFlow     {@link SwitchFlow#defaultFlow}
   * @param <T>             Context type
   * @return A {@link SwitchFlow}
   */
  public static <T extends FlowContext> SwitchFlow<T> create(String name, Function<T, String> switchCondition, Map<String, Flow<T>> flows, Flow<T> defaultFlow) {
    return new SwitchFlow<>(name, switchCondition, flows, defaultFlow);
  }

  private SwitchFlow(String name, Function<T, String> switchCondition, Map<String, Flow<T>> flows, Flow<T> defaultFlow) {
    this.name = name;
    this.switchCondition = switchCondition;
    this.flows = flows;
    this.defaultFlow = defaultFlow;
  }


  /**
   * Get the {@link SwitchFlow} name.
   *
   * @return The name
   */
  @Override
  public String getName() {
    return this.name;
  }

  /**
   * Clone the {@link SwitchFlow} with a new name.
   *
   * @param newName {@link SwitchFlow} new name
   * @return Cloned {@link SwitchFlow}
   */
  @Override
  public SwitchFlow<T> cloneFlow(String newName) {
    return SwitchFlow.create(
        newName,
        this.switchCondition,
        this.flows
            .entrySet()
            .stream()
            .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().cloneFlow())),
        this.defaultFlow.cloneFlow()
    );
  }

  /**
   * Clone the {@link SwitchFlow}.
   *
   * @return Cloned {@link SwitchFlow}
   */
  @Override
  public SwitchFlow<T> cloneFlow() {
    return this.cloneFlow(this.getName());
  }

  /**
   * {@link SwitchFlow} execution.
   * It chooses the matching {@link Flow} in {@link SwitchFlow#flows} {@link Map} depending on {@link SwitchFlow#switchCondition},
   * and executes it, or executes {@link SwitchFlow#defaultFlow} if there is no match.
   *
   * @param context  The previous {@link T} context
   * @param metadata A {@link Metadata} object
   * @return A {@link Report}
   */
  @Override
  protected Mono<Report<T>> execution(T context, Metadata<?> metadata) {
    return Mono
        .defer(() -> Mono.just(this.switchCondition.apply(context)))
        .onErrorMap(error -> new FlowTechnicalException(
            error,
            String.format("Error occurred during switchCondition evaluation: %s", error.getMessage())
        ))
        .flatMap(switchResult -> {
          Flow<T> toExecute = this.flows.get(switchResult);

          if (Objects.nonNull(toExecute)) {
            return toExecute.execute(context, metadata);
          }

          return this.defaultFlow.execute(context, metadata);
        });
  }

  /**
   * Get {@link SwitchFlow} children, aka the {@link Flow}s in {@link SwitchFlow#flows} map and the {@link SwitchFlow#defaultFlow}.
   *
   * @return A {@link List} containing children {@link Flow}s
   */
  @Override
  protected List<Flow<T>> getChildren() {
    return Stream
        .concat(
            this.flows.values().stream(),
            Stream.of(this.defaultFlow)
        )
        .collect(Collectors.toList());
  }

  /**
   * Has error policy : a {@link SwitchFlow} is in error if none of its children succeeded.
   *
   * @return A {@link Boolean}
   */
  @Override
  protected boolean flowOrChildrenHasError() {
    return !FlowStatusPolicy.flowAndOneChildSucceeded().test(this);
  }
}
