package io.github.juliengalet.reactorflow.report;

import io.github.juliengalet.reactorflow.exception.FlowException;
import io.github.juliengalet.reactorflow.flow.Flow;

import java.util.Collections;
import java.util.List;

/**
 * This class is used as the return of a {@link Flow}.
 * It contains the context, and the errors and warnings, that result from it.
 *
 * @param <T> Context type
 */
public class Report<T extends FlowContext> {
  private final List<FlowException> errors;
  private final List<FlowException> warnings;
  private final T context;

  public static <T extends FlowContext> Report<T> success(T context) {
    return new Report<>(context, Collections.emptyList(), Collections.emptyList());
  }

  public static <T extends FlowContext> Report<T> successWithWarning(T context, List<FlowException> warnings) {
    return new Report<>(context, Collections.emptyList(), warnings);
  }

  public static <T extends FlowContext> Report<T> successWithWarning(T context, FlowException warning) {
    return new Report<>(context, Collections.emptyList(), Collections.singletonList(warning));
  }

  public static <T extends FlowContext> Report<T> error(T context, List<FlowException> errors) {
    return new Report<>(context, errors, Collections.emptyList());
  }

  public static <T extends FlowContext> Report<T> error(T context, FlowException error) {
    return new Report<>(context, Collections.singletonList(error), Collections.emptyList());
  }

  public static <T extends FlowContext> Report<T> errorWithWarning(T context, List<FlowException> errors, List<FlowException> warnings) {
    return new Report<>(context, errors, warnings);
  }

  public static <T extends FlowContext> Report<T> errorWithWarning(T context, FlowException error, List<FlowException> warnings) {
    return new Report<>(context, Collections.singletonList(error), warnings);
  }

  public static <T extends FlowContext> Report<T> errorWithWarning(T context, List<FlowException> errors, FlowException warning) {
    return new Report<>(context, errors, Collections.singletonList(warning));
  }

  public static <T extends FlowContext> Report<T> errorWithWarning(T context, FlowException error, FlowException warning) {
    return new Report<>(context, Collections.singletonList(error), Collections.singletonList(warning));
  }

  private Report(T context, List<FlowException> errors, List<FlowException> warnings) {
    this.context = context;
    this.warnings = warnings;
    this.errors = errors;
  }

  public List<FlowException> getErrors() {
    return List.copyOf(this.errors);
  }

  public List<FlowException> getWarnings() {
    return List.copyOf(this.warnings);
  }

  public T getContext() {
    return this.context;
  }
}
