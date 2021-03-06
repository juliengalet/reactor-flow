package io.github.juliengalet.reactorflow.report;

import io.github.juliengalet.reactorflow.flow.Flow;
import io.github.juliengalet.reactorflow.flow.FlowStatusPolicy;

/**
 * The status that a {@link Flow} can have after its execution or when it is not executed.
 */
public enum Status {
  /**
   * Non executed {@link Flow}.
   */
  IGNORED,
  /**
   * {@link Flow} executed with at least one warning in itself or in its children.
   */
  WARNING,
  /**
   * {@link Flow} executed with no warning or error in itself or in its children.
   */
  SUCCESS,
  /**
   * {@link Flow} executed with at least one valid error in itself or in its children.
   * An error is valid if it has not be recovered, or if it is not ignore by a {@link FlowStatusPolicy}.
   */
  ERROR
}