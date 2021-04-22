package fr.jtools.reactorflow.exception;

/**
 * Exception used to represent a functional exception
 * (example: an error due something you can handle in a business rule).
 * Those exceptions are typically the ones you will want to recover in {@link fr.jtools.reactorflow.flow.RecoverableFlow}s.
 * You can inherit this class to build custom functional exceptions if you want.
 */
public class FlowFunctionalException extends FlowException {
  @Override
  public final FlowExceptionType getType() {
    return FlowExceptionType.FUNCTIONAL;
  }

  public FlowFunctionalException(String message) {
    super(message);
  }

  public FlowFunctionalException(Throwable cause, String message) {
    super(cause, message);
  }
}
