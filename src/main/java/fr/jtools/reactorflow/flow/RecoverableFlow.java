package fr.jtools.reactorflow.flow;

import fr.jtools.reactorflow.exception.FlowException;
import fr.jtools.reactorflow.exception.RecoverableFlowException;
import fr.jtools.reactorflow.report.FlowContext;
import fr.jtools.reactorflow.report.Metadata;
import fr.jtools.reactorflow.report.Report;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;

/**
 * Class managing a recoverable {@link Flow}.
 *
 * @param <T> Context type
 */
public final class RecoverableFlow<T extends FlowContext> extends Flow<T> {
  /**
   * The name.
   */
  private final String name;
  /**
   * The {@link Flow} to try.
   */
  private final Flow<T> flow;
  /**
   * The {@link Flow} that will be executed if the {@link Flow} to try fails.
   */
  private Flow<T> recover = NoOpFlow.named("Default");
  /**
   * The {@link RecoverableFlowException} type that can be recovered.
   */
  private RecoverableFlowException recoverOn = RecoverableFlowException.TECHNICAL;

  /**
   * Static method used to create a {@link RecoverableFlow}.
   *
   * @param name      {@link RecoverableFlow#name}
   * @param flow      {@link RecoverableFlow#flow}
   * @param recover   {@link RecoverableFlow#recover}
   * @param recoverOn {@link RecoverableFlow#recoverOn}
   * @param <T>       Context type
   * @return A {@link RecoverableFlow}
   */
  public static <T extends FlowContext> RecoverableFlow<T> create(String name, Flow<T> flow, Flow<T> recover, RecoverableFlowException recoverOn) {
    return new RecoverableFlow<>(name, flow, recover, recoverOn);
  }

  private RecoverableFlow(String name, Flow<T> flow, Flow<T> recover, RecoverableFlowException recoverOn) {
    this.name = name;
    this.flow = flow;
    if (Objects.nonNull(recover)) {
      this.recover = recover;
    }
    if (Objects.nonNull(recoverOn)) {
      this.recoverOn = recoverOn;
    }
  }

  /**
   * Get the {@link RecoverableFlow} name.
   *
   * @return The name
   */
  @Override
  public final String getName() {
    return this.name;
  }

  /**
   * {@link RecoverableFlow#flow} execution.
   * It executes {@link RecoverableFlow#flow}, and if it fails with an exception valid for {@link RecoverableFlow#recoverOn},
   * it executes {@link RecoverableFlow#recover}.
   *
   * @param context  The previous {@link T} context
   * @param metadata A {@link Metadata} object
   * @return A {@link Report}
   */
  @Override
  protected final Mono<Report<T>> execution(T context, Metadata<?> metadata) {
    return this.flow.execute(context, Metadata.from(metadata))
        .flatMap(report -> {
          List<FlowException> exceptionsForFlow = this.flow.getErrorsForFlowAndChildren();
          if (
              !exceptionsForFlow.isEmpty() &&
                  exceptionsForFlow.stream().allMatch(exception -> exception.isRecoverable(this.recoverOn))
          ) {
            this.flow.cleanErrorsForFlowAndChildren();
            return this.recover.execute(report.getContext(), Metadata.from(metadata));
          }
          return Mono.just(report);
        });
  }

  /**
   * Clone the {@link RecoverableFlow} with a new name.
   *
   * @param newName {@link RecoverableFlow} new name
   * @return Cloned {@link RecoverableFlow}
   */
  @Override
  public final RecoverableFlow<T> cloneFlow(String newName) {
    return RecoverableFlow.create(newName, this.flow.cloneFlow(), this.recover.cloneFlow(), this.recoverOn);
  }

  /**
   * Clone the {@link RecoverableFlow}.
   *
   * @return Cloned {@link RecoverableFlow}
   */
  @Override
  public final RecoverableFlow<T> cloneFlow() {
    return this.cloneFlow(this.getName());
  }

  /**
   * Get {@link RecoverableFlow} children, aka :
   * <ul>
   *   <li>{@link RecoverableFlow#flow}</li>
   *   <li>{@link RecoverableFlow#recover}</li>
   * </ul>
   *
   * @return A {@link List} containing children {@link Flow}s
   */
  @Override
  protected final List<Flow<T>> getChildren() {
    return List.of(this.flow, this.recover);
  }

  /**
   * Has error policy : a {@link RecoverableFlow} is in error if none of its children succeeded.
   *
   * @return A {@link Boolean}
   */
  @Override
  protected boolean flowOrChildrenHasError() {
    return !FlowStatusPolicy.flowAndOneChildSucceeded().test(this);
  }
}
