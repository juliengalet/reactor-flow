package fr.jtools.reactorflow.testutils;

import fr.jtools.reactorflow.builder.StepFlowBuilder;
import fr.jtools.reactorflow.flow.Step;
import fr.jtools.reactorflow.flow.StepFlow;
import fr.jtools.reactorflow.state.FlowContext;
import fr.jtools.reactorflow.state.Metadata;
import fr.jtools.reactorflow.state.State;
import reactor.core.publisher.Mono;

public final class ErrorRawStepFlow<T extends FlowContext, M> implements Step<T, M> {
  private final String name;

  public static <T extends FlowContext, M> StepFlow<T, M> flowNamed(String name) {
    return StepFlowBuilder
        .<T, M>defaultBuilder()
        .named(name)
        .execution(new ErrorRawStepFlow<>(name))
        .build();
  }

  public static <T extends FlowContext, M> ErrorRawStepFlow<T, M> named(String name) {
    return new ErrorRawStepFlow<>(name);
  }

  private ErrorRawStepFlow(String name) {
    this.name = name;
  }

  @Override
  public Mono<State<T>> apply(StepFlow<T, M> thisFlow, State<T> state, Metadata<M> metadata) {
    throw new RuntimeException(this.name + " raw error");
  }
}
