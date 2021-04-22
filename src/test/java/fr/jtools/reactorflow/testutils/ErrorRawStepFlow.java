package fr.jtools.reactorflow.testutils;

import fr.jtools.reactorflow.builder.StepFlowBuilder;
import fr.jtools.reactorflow.flow.StepWithMetadata;
import fr.jtools.reactorflow.flow.StepFlow;
import fr.jtools.reactorflow.report.FlowContext;
import fr.jtools.reactorflow.report.Metadata;
import fr.jtools.reactorflow.report.Report;
import reactor.core.publisher.Mono;

public final class ErrorRawStepFlow<T extends FlowContext, M> implements StepWithMetadata<T, M> {
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
  public Mono<Report<T>> apply(T context, Metadata<M> metadata) {
    throw new RuntimeException(this.name + " raw error");
  }
}
