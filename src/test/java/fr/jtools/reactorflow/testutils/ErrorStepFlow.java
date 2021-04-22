package fr.jtools.reactorflow.testutils;

import fr.jtools.reactorflow.builder.StepFlowBuilder;
import fr.jtools.reactorflow.exception.FlowTechnicalException;
import fr.jtools.reactorflow.flow.Step;
import fr.jtools.reactorflow.flow.StepFlow;
import fr.jtools.reactorflow.report.FlowContext;
import fr.jtools.reactorflow.report.Metadata;
import fr.jtools.reactorflow.report.Report;
import reactor.core.publisher.Mono;

public final class ErrorStepFlow<T extends FlowContext, M> implements Step<T, M> {
  private final String name;

  public static <T extends FlowContext, M> StepFlow<T, M> flowNamed(String name) {
    return StepFlowBuilder
        .<T, M>defaultBuilder()
        .named(name)
        .execution(new ErrorStepFlow<>(name))
        .build();
  }

  public static <T extends FlowContext, M> ErrorStepFlow<T, M> named(String name) {
    return new ErrorStepFlow<>(name);
  }

  private ErrorStepFlow(String name) {
    this.name = name;
  }

  @Override
  public Mono<Report<T>> apply(T context, Metadata<M> metadata) {
    return Mono.just(Report.error(context, new FlowTechnicalException(this.name)));
  }
}