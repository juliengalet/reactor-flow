package fr.jtools.reactorflow.builder;

import fr.jtools.reactorflow.exception.FlowBuilderException;
import fr.jtools.reactorflow.flow.StepFlow;
import fr.jtools.reactorflow.flow.StepWithMetadata;
import fr.jtools.reactorflow.report.FlowContext;

import java.util.Objects;

/**
 * Class used to build a {@link StepFlow}.
 */
public final class StepFlowBuilder {
  /**
   * Get a default builder for {@link FlowContext} context.
   *
   * @param <T> Context type
   * @param <M> Metadata type
   * @return {@link StepFlowBuilder.Named} builder step
   */
  public static <T extends FlowContext, M> StepFlowBuilder.Named<T, M> defaultBuilder() {
    return new StepFlowBuilder.BuildSteps<>();
  }

  /**
   * Get a builder for a specified context class.
   *
   * @param contextClass Context class that will be inferred to {@link T}
   * @param <T>          Context type
   * @param <M>          Metadata type
   * @return {@link StepFlowBuilder.Named} builder step
   */
  public static <T extends FlowContext, M> StepFlowBuilder.Named<T, M> builderForContextOfType(Class<T> contextClass) {
    return new StepFlowBuilder.BuildSteps<>();
  }

  /**
   * Get a builder for a specified metadata class.
   *
   * @param metadataClass Context class that will be inferred to {@link M}
   * @param <M>           Context type
   * @param <M>           Metadata type
   * @return {@link StepFlowBuilder.Named} builder step
   */
  public static <T extends FlowContext, M> StepFlowBuilder.Named<T, M> builderForMetadataType(Class<M> metadataClass) {
    return new StepFlowBuilder.BuildSteps<>();
  }

  /**
   * Get a builder for a specified context and metadata class.
   *
   * @param contextClass  Context class that will be inferred to {@link T}
   * @param metadataClass Context class that will be inferred to {@link M}
   * @param <T>           Context type
   * @param <M>           Metadata type
   * @return {@link StepFlowBuilder.Named} builder step
   */
  public static <T extends FlowContext, M> StepFlowBuilder.Named<T, M> builderForTypes(Class<T> contextClass, Class<M> metadataClass) {
    return new StepFlowBuilder.BuildSteps<>();
  }

  private static final class BuildSteps<T extends FlowContext, M> implements StepFlowBuilder.Named<T, M>,
      StepFlowBuilder.Execution<T, M>,
      StepFlowBuilder.Build<T, M> {
    /**
     * The unit of work to execute.
     */
    private StepWithMetadata<T, M> execution;
    /**
     * The name.
     */
    private String name;

    private BuildSteps() {
    }

    public final StepFlowBuilder.Execution<T, M> named(String name) {
      if (Objects.isNull(name)) {
        throw new FlowBuilderException(StepFlowBuilder.class, "name is mandatory");
      }
      this.name = name;
      return this;
    }

    public final StepFlowBuilder.Build<T, M> execution(StepWithMetadata<T, M> execution) {
      if (Objects.isNull(execution)) {
        throw new FlowBuilderException(StepFlowBuilder.class, "execution is mandatory");
      }
      this.execution = execution;
      return this;
    }

    public final StepFlow<T, M> build() {
      return StepFlow.create(this.name, this.execution);
    }
  }

  public interface Build<T extends FlowContext, M> {
    /**
     * Build the {@link StepFlow}.
     *
     * @return Built {@link StepFlow}
     */
    StepFlow<T, M> build();
  }

  public interface Named<T extends FlowContext, M> {
    /**
     * Define flow name.
     *
     * @param name The name
     * @return {@link StepFlowBuilder.Execution} builder step
     */
    StepFlowBuilder.Execution<T, M> named(String name);
  }

  public interface Execution<T extends FlowContext, M> {
    /**
     * Define the unit of work to execute.
     *
     * @param execution The unit of work
     * @return {@link StepFlowBuilder.Build} builder step
     */
    StepFlowBuilder.Build<T, M> execution(StepWithMetadata<T, M> execution);
  }
}
