package io.github.juliengalet.reactorflow.exception;

import io.github.juliengalet.reactorflow.flow.RecoverableFlow;
import io.github.juliengalet.reactorflow.flow.RetryableFlow;

/**
 * Enum used to know if a {@link RecoverableFlow} or a {@link RetryableFlow}
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
