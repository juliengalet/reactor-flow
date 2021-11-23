package io.github.juliengalet.reactorflow.testutils;

import io.github.juliengalet.reactorflow.builder.StepFlowBuilder;
import io.github.juliengalet.reactorflow.flow.StepFlow;
import io.github.juliengalet.reactorflow.flow.StepWithMetadata;
import io.github.juliengalet.reactorflow.report.FlowContext;
import io.github.juliengalet.reactorflow.report.Metadata;
import io.github.juliengalet.reactorflow.report.Report;
import reactor.core.publisher.Mono;

import java.util.UUID;

public final class SuccessWithIntegerMetadataStepFlow<T extends FlowContext> implements StepWithMetadata<T, Integer> {
  private final String name;

  public static <T extends FlowContext> StepFlow<T, Integer> flowNamed(String name) {
    return StepFlowBuilder
        .<T, Integer>defaultBuilder()
        .named(name)
        .execution(new SuccessWithIntegerMetadataStepFlow<>(name))
        .build();
  }

  public static <T extends FlowContext> SuccessWithIntegerMetadataStepFlow<T> named(String name) {
    return new SuccessWithIntegerMetadataStepFlow<>(name);
  }

  private SuccessWithIntegerMetadataStepFlow(String name) {
    this.name = name;
  }

  @Override
  public Mono<Report<T>> apply(T context, Metadata<Integer> metadata) {
    String metadataEntry = String.format("%s | %s | %s", this.name, metadata.getData(), UUID.randomUUID().toString());
    context.put(metadataEntry, metadataEntry);
    return Mono.just(Report.success(context));
  }
}
