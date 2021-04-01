package fr.jtools.reactorflow.exception;

import fr.jtools.reactorflow.flow.Flow;
import fr.jtools.reactorflow.utils.ConsoleStyle;
import fr.jtools.reactorflow.utils.PrettyPrint;

import java.util.Objects;

import static fr.jtools.reactorflow.utils.LoggerUtils.colorize;

public abstract class FlowException extends RuntimeException implements PrettyPrint {
  public abstract FlowExceptionType getType();

  private transient Flow<?> flowConcerned;

  public Flow<?> getFlowConcerned() {
    return this.flowConcerned;
  }

  protected void setFlowConcerned(Flow<?> flow) {
    this.flowConcerned = flow;
  }

  public FlowException(String message) {
    super(message);
  }

  public FlowException(Throwable cause, String message) {
    super(message, cause);
  }

  public FlowException(Flow<?> flow, String message) {
    super(message);
    this.setFlowConcerned(flow);
  }

  public FlowException(Flow<?> flow, Throwable cause, String message) {
    super(message, cause);
    this.setFlowConcerned(flow);
  }


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

  public final boolean isRecoverable(RecoverableFlowException recoverable) {
    if (recoverable == RecoverableFlowException.ALL) {
      return true;
    }

    if (recoverable == RecoverableFlowException.FUNCTIONAL && this.getType() == FlowExceptionType.FUNCTIONAL) {
      return true;
    }

    if (recoverable == RecoverableFlowException.TECHNICAL && this.getType() == FlowExceptionType.TECHNICAL) {
      return true;
    }

    return false;
  }
}
