package io.github.juliengalet.reactorflow.exception;

import io.github.juliengalet.reactorflow.flow.Flow;
import io.github.juliengalet.reactorflow.flow.StepFlow;

/**
 * Exception used to represent a builder exception
 * (example: a null value is used in a non nullable step of a {@link Flow builder}).
 * Those exceptions should not be used during in {@link StepFlow} executions.
 */
public final class FlowBuilderException extends FlowException {
  public static <T> String mapMessage(Class<T> builder, String message) {
    return String.format("%s: %s", builder.getSimpleName(), message);
  }

  @Override
  public FlowExceptionType getType() {
    return FlowExceptionType.BUILDER;
  }

  public <T> FlowBuilderException(Class<T> builder, String message) {
    super(FlowBuilderException.mapMessage(builder, message));
  }

  public <T> FlowBuilderException(Throwable cause, Class<T> builder, String message) {
    super(cause, FlowBuilderException.mapMessage(builder, message));
  }
}
