package fr.jtools.reactorflow.builder;

import fr.jtools.reactorflow.exception.FlowBuilderException;
import fr.jtools.reactorflow.flow.Flow;
import fr.jtools.reactorflow.flow.SequentialFlow;
import fr.jtools.reactorflow.report.FlowContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Class used to build a {@link SequentialFlow}.
 */
public final class SequentialFlowBuilder {
  /**
   * Get a default builder for {@link FlowContext} context.
   *
   * @param <T> Context type
   * @return {@link SequentialFlowBuilder.Named} builder step
   */
  public static <T extends FlowContext> SequentialFlowBuilder.Named<T> defaultBuilder() {
    return new SequentialFlowBuilder.BuildSteps<>();
  }

  /**
   * Get a builder for a specified context class.
   *
   * @param contextClass Context class that will be inferred to {@link T}
   * @param <T>          Context type
   * @return {@link SequentialFlowBuilder.Named} builder step
   */
  public static <T extends FlowContext> SequentialFlowBuilder.Named<T> builderForContextOfType(Class<T> contextClass) {
    return new SequentialFlowBuilder.BuildSteps<>();
  }

  private static final class BuildSteps<T extends FlowContext> implements SequentialFlowBuilder.Named<T>,
      SequentialFlowBuilder.Then<T>,
      SequentialFlowBuilder.Finally<T>,
      SequentialFlowBuilder.Build<T> {
    /**
     * The ordered list of {@link Flow}s to execute.
     */
    private final List<Flow<T>> flows = new ArrayList<>();
    /**
     * The final {@link Flow} to execute
     * (will be executed even if an error occurs during {@link BuildSteps#flows} execution).
     */
    private Flow<T> finalFlow;
    /**
     * The name.
     */
    private String name;

    private BuildSteps() {
    }

    public final SequentialFlowBuilder.Then<T> named(String name) {
      if (Objects.isNull(name)) {
        throw new FlowBuilderException(SequentialFlowBuilder.class, "name is mandatory");
      }
      this.name = name;
      return this;
    }

    public final SequentialFlowBuilder.Then<T> then(Flow<T> nextFlow) {
      if (Objects.isNull(nextFlow)) {
        throw new FlowBuilderException(SequentialFlowBuilder.class, "then flow is mandatory");
      }
      this.flows.add(nextFlow);
      return this;
    }

    public final SequentialFlowBuilder.Build<T> doFinally(Flow<T> finalFlow) {
      if (Objects.isNull(finalFlow)) {
        throw new FlowBuilderException(SequentialFlowBuilder.class, "final flow is mandatory");
      }
      this.finalFlow = finalFlow;
      return this;
    }

    public final SequentialFlow<T> build() {
      return SequentialFlow.create(this.name, this.flows, this.finalFlow);
    }
  }

  public interface Then<T extends FlowContext> extends Finally<T>, Build<T> {
    /**
     * Define the next {@link Flow} to execute.
     *
     * @param flow A {@link Flow}
     * @return {@link SequentialFlowBuilder.Then} builder step
     */
    SequentialFlowBuilder.Then<T> then(Flow<T> flow);
  }

  public interface Finally<T extends FlowContext> {
    /**
     * Define the final {@link Flow} to execute
     * (will be executed even if an error occurs during {@link BuildSteps#flows} execution).
     *
     * @param flow A {@link Flow}
     * @return {@link SequentialFlowBuilder.Build} builder step
     */
    SequentialFlowBuilder.Build<T> doFinally(Flow<T> flow);
  }

  public interface Build<T extends FlowContext> {
    /**
     * Build the {@link SequentialFlow}.
     *
     * @return Built {@link SequentialFlow}
     */
    SequentialFlow<T> build();
  }

  public interface Named<T extends FlowContext> {
    /**
     * Define flow name.
     *
     * @param name The name
     * @return {@link SequentialFlowBuilder.Then} builder step
     */
    SequentialFlowBuilder.Then<T> named(String name);
  }
}
