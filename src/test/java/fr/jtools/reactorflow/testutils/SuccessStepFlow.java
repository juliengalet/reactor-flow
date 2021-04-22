package fr.jtools.reactorflow.testutils;

import fr.jtools.reactorflow.builder.StepFlowBuilder;
import fr.jtools.reactorflow.flow.Step;
import fr.jtools.reactorflow.flow.StepFlow;
import fr.jtools.reactorflow.report.FlowContext;
import fr.jtools.reactorflow.report.Metadata;
import fr.jtools.reactorflow.report.Report;
import reactor.core.publisher.Mono;

public final class SuccessStepFlow<T extends FlowContext, M> implements Step<T, M> {
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
  public Mono<Report<T>> apply(T context, Metadata<M> metadata) {
    context.put(this.name, this.name);
    return Mono.just(Report.success(context));
  }
}
