package io.github.juliengalet.reactorflow.exception;

import io.github.juliengalet.reactorflow.flow.Flow;
import io.github.juliengalet.reactorflow.utils.ConsoleStyle;
import io.github.juliengalet.reactorflow.utils.PrettyPrint;

import java.util.Objects;

import static io.github.juliengalet.reactorflow.utils.LoggerUtils.colorize;

/**
 * Abstract exception used to generate all types of exceptions used in this library.
 * You can inherit this class to build custom exceptions if you want.
 */
public abstract class FlowException extends RuntimeException implements PrettyPrint {
  /**
   * Abstract method that should be implemented, in order to know the {@link FlowExceptionType}.
   *
   * @return A {@link FlowExceptionType}
   */
  public abstract FlowExceptionType getType();

  /**
   * The {@link Flow} concerned by the exception (null if it is a {@link FlowBuilderException}).
   */
  private transient Flow<?> flowConcerned;

  /**
   * Set the {@link FlowException#flowConcerned}.
   *
   * @param flowConcerned The {@link FlowException#flowConcerned}
   * @return The {@link FlowException}
   */
  public FlowException flowConcerned(Flow<?> flowConcerned) {
    this.flowConcerned = flowConcerned;
    return this;
  }

  /**
   * Get the {@link FlowException#flowConcerned}.
   *
   * @return A {@link Flow} or null.
   */
  public Flow<?> getFlowConcerned() {
    return this.flowConcerned;
  }

  /**
   * Construct an exception from a message.
   *
   * @param message The message
   */
  protected FlowException(String message) {
    super(message);
  }

  /**
   * Construct an exception from a message and the original {@link Throwable}.
   *
   * @param cause   The original {@link Throwable}
   * @param message The message
   */
  protected FlowException(Throwable cause, String message) {
    super(message, cause);
  }

  /**
   * Get the string representation of the exception.
   *
   * @return A {@link String} representing the exception
   */
  @Override
  public String toString() {
    if (Objects.isNull(this.getFlowConcerned())) {
      return String.format(
          "%s exception occurred with message %s",
          this.getType().name(),
          this.getMessage()
      );
    }
    return String.format(
        "%s exception occurred on %s named %s with message %s (%s)",
        this.getType().name(),
        this.getFlowConcerned().getClass().getSimpleName(),
        this.getFlowConcerned().getName(),
        this.getMessage(),
        this.getFlowConcerned().hashCode()
    );
  }

  /**
   * Get the colorized string representation of the exception.
   *
   * @return A {@link String} representing the exception
   */
  @Override
  public String toPrettyString() {
    if (Objects.isNull(this.getFlowConcerned())) {
      return String.format(
          "%s exception occurred with message %s",
          colorize(this.getType().name(), ConsoleStyle.CYAN_BOLD),
          colorize(this.getMessage(), ConsoleStyle.MAGENTA_BOLD)
      );
    }
    return String.format(
        "%s exception occurred on %s named %s with message %s (%s)",
        colorize(this.getType().name(), ConsoleStyle.CYAN_BOLD),
        colorize(this.getFlowConcerned().getClass().getSimpleName(), ConsoleStyle.BLUE_BOLD),
        colorize(this.getFlowConcerned().getName(), ConsoleStyle.WHITE_BOLD),
        colorize(this.getMessage(), ConsoleStyle.MAGENTA_BOLD),
        colorize(String.valueOf(this.getFlowConcerned().hashCode()), ConsoleStyle.BLACK_BOLD)
    );
  }

  /**
   * Check if the exception is recoverable or retryable, for a {@link RecoverableFlowException} type.
   *
   * @param recoverable A {@link RecoverableFlowException} type
   * @return A boolean
   */
  public final boolean isRecoverable(RecoverableFlowException recoverable) {
    if (recoverable == RecoverableFlowException.ALL) {
      return true;
    }

    if (recoverable == RecoverableFlowException.FUNCTIONAL && this.getType() == FlowExceptionType.FUNCTIONAL) {
      return true;
    }

    return recoverable == RecoverableFlowException.TECHNICAL && this.getType() == FlowExceptionType.TECHNICAL;
  }
}
