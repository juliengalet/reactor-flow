package io.github.juliengalet.reactorflow.testutils;

import io.github.juliengalet.reactorflow.builder.StepFlowBuilder;
import io.github.juliengalet.reactorflow.flow.StepFlow;
import io.github.juliengalet.reactorflow.flow.StepWithMetadata;
import io.github.juliengalet.reactorflow.report.FlowContext;
import io.github.juliengalet.reactorflow.report.Metadata;
import io.github.juliengalet.reactorflow.report.Report;
import reactor.core.publisher.Mono;

import java.util.UUID;

public final class SuccessWithStringMetadataStepFlow<T extends FlowContext> implements StepWithMetadata<T, String> {
  private final String name;

  public static <T extends FlowContext> StepFlow<T, String> flowNamed(String name) {
    return StepFlowBuilder
        .<T, String>defaultBuilder()
        .named(name)
        .execution(new SuccessWithStringMetadataStepFlow<>(name))
        .build();
  }

  public static <T extends FlowContext> SuccessWithStringMetadataStepFlow<T> named(String name) {
    return new SuccessWithStringMetadataStepFlow<>(name);
  }

  private SuccessWithStringMetadataStepFlow(String name) {
    this.name = name;
  }

  @Override
  public Mono<Report<T>> apply(T context, Metadata<String> metadata) {
    String metadataEntry = String.format("%s | %s | %s", this.name, metadata.getData(), UUID.randomUUID().toString());
    context.put(metadataEntry, metadataEntry);
    return Mono.just(Report.success(context));
  }
}
