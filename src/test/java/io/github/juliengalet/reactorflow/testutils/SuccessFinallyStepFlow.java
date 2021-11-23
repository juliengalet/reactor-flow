package io.github.juliengalet.reactorflow.testutils;

import io.github.juliengalet.reactorflow.builder.StepFlowBuilder;
import io.github.juliengalet.reactorflow.exception.FlowException;
import io.github.juliengalet.reactorflow.flow.StepFlow;
import io.github.juliengalet.reactorflow.flow.StepWithMetadata;
import io.github.juliengalet.reactorflow.report.FlowContext;
import io.github.juliengalet.reactorflow.report.Metadata;
import io.github.juliengalet.reactorflow.report.Report;
import reactor.core.publisher.Mono;

import java.util.UUID;
import java.util.stream.Collectors;

public final class SuccessFinallyStepFlow<T extends FlowContext, M> implements StepWithMetadata<T, M> {
  private final String name;

  public static <T extends FlowContext, M> StepFlow<T, M> flowNamed(String name) {
    return StepFlowBuilder
        .<T, M>defaultBuilder()
        .named(name)
        .execution(new SuccessFinallyStepFlow<>(name))
        .build();
  }

  public static <T extends FlowContext, M> SuccessFinallyStepFlow<T, M> named(String name) {
    return new SuccessFinallyStepFlow<>(name);
  }

  private SuccessFinallyStepFlow(String name) {
    this.name = name;
  }

  @Override
  public Mono<Report<T>> apply(T context, Metadata<M> metadata) {
    String errorEntry = String.format(
        "%s | %s (%s %s) | %s",
        this.name,
        metadata.getErrors().stream().map(FlowException::getMessage).collect(Collectors.joining(", ")),
        metadata.getData().getClass().getSimpleName(),
        metadata.getData(),
        UUID.randomUUID().toString()
    );
    context.put(errorEntry, errorEntry);
    return Mono.just(Report.success(context));
  }
}
