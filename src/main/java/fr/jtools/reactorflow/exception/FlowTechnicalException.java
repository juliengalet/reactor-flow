package fr.jtools.reactorflow.exception;

import fr.jtools.reactorflow.flow.Flow;

public final class FlowTechnicalException extends FlowException {
  @Override
  public FlowExceptionType getType() {
    return FlowExceptionType.TECHNICAL;
  }

  public FlowTechnicalException(Flow<?> flow, String message) {
    super(message);
    this.setFlowConcerned(flow);
  }

  public FlowTechnicalException(Flow<?> flow, Throwable cause, String message) {
    super(cause, message);
    this.setFlowConcerned(flow);
  }
}
