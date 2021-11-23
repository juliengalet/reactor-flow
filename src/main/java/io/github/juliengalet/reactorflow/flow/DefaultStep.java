package io.github.juliengalet.reactorflow.flow;

import io.github.juliengalet.reactorflow.builder.StepFlowBuilder;
import io.github.juliengalet.reactorflow.report.FlowContext;
import io.github.juliengalet.reactorflow.report.Metadata;
import io.github.juliengalet.reactorflow.report.Report;
import reactor.core.publisher.Mono;

/**
 * Class that should be extended, in order to be able to create {@link StepFlow}, with injecting services possibility
 * (see {@link DefaultMetadataStep} for the version allowing metadata type customization).
 *
 * @param <T> The context type
 */
public class DefaultStep<T extends FlowContext> {
  private static final String DEFAULT_NAME = "Default";

  /**
   * Overridable method that should return the name of your step.
   *
   * @return The name
   */
  protected String getName() {
    return DEFAULT_NAME;
  }

  /**
   * Overridable method that should implement the logic of the step.
   *
   * @param context  The current {@link T} flow context
   * @param metadata The metadata instance
   * @return A {@link Report} inside a Mono
   */
  protected Mono<Report<T>> getExecution(T context, Metadata<Object> metadata) {
    return Mono.just(Report.success(context));
  }

  /**
   * This method build the step. You should call it to plug your step as a {@link StepFlow} inside flows.
   *
   * @return The built {@link StepFlow}
   */
  public final StepFlow<T, Object> getStep() {
    return StepFlowBuilder
        .<T, Object>defaultBuilder()
        .named(getName())
        .execution(this::getExecution)
        .build();
  }
}
