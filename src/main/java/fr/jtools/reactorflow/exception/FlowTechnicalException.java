package fr.jtools.reactorflow.exception;

/**
 * Exception used to represent a technical exception
 * (example: a random failure during a database access, due to the network).
 * Those exceptions are typically the ones you will want to retry in {@link fr.jtools.reactorflow.flow.RetryableFlow}s.
 * You can inherit this class to build custom technical exceptions if you want.
 */
public class FlowTechnicalException extends FlowException {
  @Override
  public final FlowExceptionType getType() {
    return FlowExceptionType.TECHNICAL;
  }

  public FlowTechnicalException(String message) {
    super(message);
  }

  public FlowTechnicalException(Throwable cause, String message) {
    super(cause, message);
  }
}
