package io.github.juliengalet.reactorflow.flow;

import io.github.juliengalet.reactorflow.builder.StepFlowBuilder;
import io.github.juliengalet.reactorflow.report.FlowContext;
import io.github.juliengalet.reactorflow.report.Metadata;
import io.github.juliengalet.reactorflow.report.Report;
import reactor.core.publisher.Mono;

public class DefaultStep<T extends FlowContext> {
  private static final String DEFAULT_NAME = "Default";

  protected String getName() {
    return DEFAULT_NAME;
  }

  protected Mono<Report<T>> getExecution(T context, Metadata<Object> metadata) {
    return Mono.just(Report.success(context));
  }

  public final StepFlow<T, Object> getStep() {
    return StepFlowBuilder
        .<T, Object>defaultBuilder()
        .named(getName())
        .execution(this::getExecution)
        .build();
  }
}
