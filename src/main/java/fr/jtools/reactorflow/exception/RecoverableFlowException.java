package fr.jtools.reactorflow.exception;

/**
 * Enum used to know if a {@link fr.jtools.reactorflow.flow.RecoverableFlow} or a {@link fr.jtools.reactorflow.flow.RetryableFlow}
 * can recover or retry.
 */
public enum RecoverableFlowException {
  /**
   * Retry or recover on all {@link FlowException} (so, {@link FlowTechnicalException} and {@link FlowFunctionalException}).
   */
  ALL,
  /**
   * Retry or recover only on {@link FlowTechnicalException}.
   */
  TECHNICAL,
  /**
   * Retry or recover only on {@link FlowFunctionalException}.
   */
  FUNCTIONAL,
  /**
   * Never retry or recover.
   */
  NONE
}
