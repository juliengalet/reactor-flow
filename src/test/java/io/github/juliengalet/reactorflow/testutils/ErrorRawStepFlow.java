package io.github.juliengalet.reactorflow.testutils;

import io.github.juliengalet.reactorflow.builder.StepFlowBuilder;
import io.github.juliengalet.reactorflow.flow.StepWithMetadata;
import io.github.juliengalet.reactorflow.flow.StepFlow;
import io.github.juliengalet.reactorflow.report.FlowContext;
import io.github.juliengalet.reactorflow.report.Metadata;
import io.github.juliengalet.reactorflow.report.Report;
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
