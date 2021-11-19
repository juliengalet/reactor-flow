package io.github.juliengalet.reactorflow.exception;

/**
 * Enum used to list the {@link FlowException} types.
 */
public enum FlowExceptionType {
  /**
   * Technical exception (see {@link FlowTechnicalException}).
   */
  TECHNICAL,
  /**
   * Functional exception (see {@link FlowFunctionalException}).
   */
  FUNCTIONAL,
  /**
   * Builder exception (see {@link FlowBuilderException}).
   */
  BUILDER
}
