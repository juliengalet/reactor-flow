package fr.jtools.reactorflow.flow;

import fr.jtools.reactorflow.report.Status;

import java.util.function.Predicate;

/**
 * Class used to manage {@link Flow#status}.
 */
public final class FlowStatusPolicy implements Predicate<Flow<?>> {
  /**
   * A {@link Predicate} that returns true or false.
   */
  private final Predicate<Flow<?>> hasStatus;

  /**
   * {@link FlowStatusPolicy} that checks {@link Flow} and at least one of its children have succeeded.
   *
   * @return A {@link FlowStatusPolicy}
   */
  public static FlowStatusPolicy flowAndOneChildSucceeded() {
    return new FlowStatusPolicy(
        flow -> {
          Predicate<Flow<?>> isSuccessOrWarning = f -> (f.getStatus().equals(Status.SUCCESS) || f.getStatus().equals(Status.WARNING));
          return flow.getErrors().isEmpty() && flow.getChildren().stream().anyMatch(isSuccessOrWarning);
        }
    );
  }

  /**
   * {@link FlowStatusPolicy} that checks {@link Flow} and all its children have succeeded.
   *
   * @return A {@link FlowStatusPolicy}
   */
  public static FlowStatusPolicy flowAndAllChildrenSucceeded() {
    return new FlowStatusPolicy(
        flow -> {
          Predicate<Flow<?>> isSuccessOrWarning = f -> (f.getStatus().equals(Status.SUCCESS) || f.getStatus().equals(Status.WARNING));
          return flow.getErrors().isEmpty() && flow.getChildren().stream().allMatch(isSuccessOrWarning);
        }
    );
  }

  /**
   * {@link FlowStatusPolicy} that checks {@link Flow} or at least one of its children have a warning.
   *
   * @return A {@link FlowStatusPolicy}
   */
  public static FlowStatusPolicy flowOrChildHasWarning() {
    return new FlowStatusPolicy(
        flow -> !flow.getWarnings().isEmpty() || flow.getChildren().stream().anyMatch(f -> f.getStatus().equals(Status.WARNING))
    );
  }

  /**
   * Construct a {@link FlowStatusPolicy}.
   *
   * @param hasStatus A {@link Predicate}
   */
  public FlowStatusPolicy(Predicate<Flow<?>> hasStatus) {
    this.hasStatus = hasStatus;
  }

  @Override
  public boolean test(Flow<?> flow) {
    return this.hasStatus.test(flow);
  }
}
