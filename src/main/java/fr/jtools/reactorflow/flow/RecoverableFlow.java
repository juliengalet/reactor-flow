package fr.jtools.reactorflow.flow;

import fr.jtools.reactorflow.exception.FlowException;
import fr.jtools.reactorflow.exception.RecoverableFlowException;
import fr.jtools.reactorflow.state.FlowContext;
import fr.jtools.reactorflow.state.Metadata;
import fr.jtools.reactorflow.state.State;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;

public final class RecoverableFlow<T extends FlowContext> extends Flow<T> {
  private final String name;
  private final Flow<T> flow;
  private Flow<T> recover = NoOpFlow.named("Default");
  private RecoverableFlowException recoverOn = RecoverableFlowException.TECHNICAL;

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

  @Override
  public final String getName() {
    return this.name;
  }

  @Override
  protected final Mono<State<T>> execution(State<T> previousState, Metadata<?> metadata) {
    return this.flow.execute(previousState, Metadata.from(metadata))
        .flatMap(state -> {
          List<FlowException> exceptionsForFlow = this.flow.getErrorsForFlowAndChildren();
          if (
              !exceptionsForFlow.isEmpty() &&
                  exceptionsForFlow.stream().allMatch(exception -> exception.isRecoverable(this.recoverOn))
          ) {
            this.flow.cleanErrorsForFlowAndChildren();
            return this.recover.execute(state, Metadata.from(metadata));
          }
          return Mono.just(state);
        });
  }

  @Override
  public final RecoverableFlow<T> cloneFlow(String newName) {
    return RecoverableFlow.create(newName, this.flow.cloneFlow(), this.recover.cloneFlow(), this.recoverOn);
  }

  @Override
  public final RecoverableFlow<T> cloneFlow() {
    return this.cloneFlow(this.getName());
  }

  @Override
  protected final List<Flow<T>> getChildren() {
    return List.of(this.flow, this.recover);
  }

  @Override
  protected boolean flowOrChildrenHasError() {
    return !FlowStatusPolicy.flowAndOneChildSucceeded().test(this);
  }
}
