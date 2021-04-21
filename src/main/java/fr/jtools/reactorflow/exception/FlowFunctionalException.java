package fr.jtools.reactorflow.exception;

import fr.jtools.reactorflow.flow.Flow;

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

  public FlowFunctionalException(Flow<?> flowConcerned, String message) {
    super(flowConcerned, message);
  }

  public FlowFunctionalException(Flow<?> flowConcerned, Throwable cause, String message) {
    super(cause, flowConcerned, message);
  }
}
