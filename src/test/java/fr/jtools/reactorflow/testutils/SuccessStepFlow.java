package fr.jtools.reactorflow.testutils;

import fr.jtools.reactorflow.builder.StepFlowBuilder;
import fr.jtools.reactorflow.flow.StepExecution;
import fr.jtools.reactorflow.flow.StepFlow;
import fr.jtools.reactorflow.state.FlowContext;
import fr.jtools.reactorflow.state.Metadata;
import fr.jtools.reactorflow.state.State;
import reactor.core.publisher.Mono;

public final class SuccessStepFlow<T extends FlowContext, M> extends StepExecution<T, M> {
  private final String name;

  public static <T extends FlowContext, M> StepFlow<T, M> flowNamed(String name) {
    return StepFlowBuilder
        .<T, M>defaultBuilder()
        .named(name)
        .execution(new SuccessStepFlow<>(name))
        .build();
  }

  public static <T extends FlowContext, M> SuccessStepFlow<T, M> named(String name) {
    return new SuccessStepFlow<>(name);
  }

  private SuccessStepFlow(String name) {
    this.name = name;
  }

  @Override
  public Mono<State<T>> apply(StepFlow<T, M> thisFlow, State<T> state, Metadata<M> metadata) {
    state.getContext().put(this.name, this.name);
    return Mono.just(state);
  }
}
