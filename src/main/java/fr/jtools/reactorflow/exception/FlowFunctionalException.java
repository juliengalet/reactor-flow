package fr.jtools.reactorflow.exception;

import fr.jtools.reactorflow.flow.Flow;

public final class FlowFunctionalException extends FlowException {
  @Override
  public FlowExceptionType getType() {
    return FlowExceptionType.FUNCTIONAL;
  }

  public FlowFunctionalException(Flow<?> flow, String message) {
    super(message);
    this.setFlowConcerned(flow);
  }

  public FlowFunctionalException(Flow<?> flow, Throwable cause, String message) {
    super(cause, message);
    this.setFlowConcerned(flow);
  }
}
