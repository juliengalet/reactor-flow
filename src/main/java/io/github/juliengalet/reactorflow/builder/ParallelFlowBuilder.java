package io.github.juliengalet.reactorflow.builder;

import io.github.juliengalet.reactorflow.exception.FlowBuilderException;
import io.github.juliengalet.reactorflow.flow.Flow;
import io.github.juliengalet.reactorflow.flow.ParallelFlow;
import io.github.juliengalet.reactorflow.report.FlowContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BinaryOperator;
import java.util.function.Function;

/**
 * Class used to build a {@link ParallelFlow}.
 */
public final class ParallelFlowBuilder {
  /**
   * The default merge strategy, to use if the parallelized {@link Flow}s write data to the same {@link FlowContext},
   * that should be thread safe.
   * Note that it should generally be the one you will need.
   *
   * @param <T> Context type
   * @return The default merge strategy
   */
  public static <T extends FlowContext> BinaryOperator<T> defaultMergeStrategy() {
    return (context1, context2) -> context1;
  }

  /**
   * Get a default builder for {@link FlowContext} context.
   *
   * @param <T> Context type
   * @param <M> Metadata type
   * @return {@link ParallelFlowBuilder.Named} builder step
   */
  public static <T extends FlowContext, M> ParallelFlowBuilder.Named<T, M> defaultBuilder() {
    return new ParallelFlowBuilder.BuildSteps<>();
  }

  /**
   * Get a builder for a specified context class.
   *
   * @param contextClass Context class that will be inferred to {@link T}
   * @param <T>          Context type
   * @param <M>          Metadata type
   * @return {@link ParallelFlowBuilder.Named} builder step
   */
  public static <T extends FlowContext, M> ParallelFlowBuilder.Named<T, M> builderForContextOfType(Class<T> contextClass) {
    return new ParallelFlowBuilder.BuildSteps<>();
  }

  /**
   * Get a builder for a specified metadata class.
   *
   * @param metadataClass Context class that will be inferred to {@link M}
   * @param <T>           Context type
   * @param <M>           Metadata type
   * @return {@link ParallelFlowBuilder.Named} builder step
   */
  public static <T extends FlowContext, M> ParallelFlowBuilder.Named<T, M> builderForMetadataType(Class<M> metadataClass) {
    return new ParallelFlowBuilder.BuildSteps<>();
  }

  /**
   * Get a builder for a specified context and metadata class.
   *
   * @param contextClass  Context class that will be inferred to {@link T}
   * @param metadataClass Context class that will be inferred to {@link M}
   * @param <T>           Context type
   * @param <M>           Metadata type
   * @return {@link ParallelFlowBuilder.Named} builder step
   */
  public static <T extends FlowContext, M> ParallelFlowBuilder.Named<T, M> builderForTypes(Class<T> contextClass, Class<M> metadataClass) {
    return new ParallelFlowBuilder.BuildSteps<>();
  }

  private static final class BuildSteps<T extends FlowContext, M> implements ParallelFlowBuilder.Named<T, M>,
      ParallelFlowBuilder.Parallelize<T, M>,
      ParallelFlowBuilder.ParallelizedFlow<T, M>,
      ParallelFlowBuilder.MergeStrategy<T, M>,
      ParallelFlowBuilder.Build<T, M> {
    /**
     * A list of {@link Flow}s to parallelize.
     */
    private final List<Flow<T>> flows = new ArrayList<>();
    /**
     * A function that should return an array, in order to parallelize {@link Flow} when iterate on it.
     */
    private Function<T, ? extends Iterable<M>> parallelizeFromArray;
    /**
     * The {@link Flow} to parallelize if {@link BuildSteps#parallelizeFromArray} is used.
     */
    private Flow<T> flowToParallelize;
    /**
     * The merge {@link FlowContext} strategy.
     */
    private BinaryOperator<T> mergeStrategy;
    /**
     * The name.
     */
    private String name;

    private BuildSteps() {
    }

    public final ParallelFlowBuilder.Parallelize<T, M> named(String name) {
      if (Objects.isNull(name)) {
        throw new FlowBuilderException(ParallelFlowBuilder.class, "name is mandatory");
      }
      this.name = name;
      return this;
    }

    public final ParallelFlowBuilder.MergeStrategy<T, M> parallelize(List<Flow<T>> flows) {
      if (Objects.isNull(flows)) {
        throw new FlowBuilderException(ParallelFlowBuilder.class, "flows are mandatory");
      }
      this.flows.addAll(flows);
      return this;
    }

    public final ParallelFlowBuilder.ParallelizedFlow<T, M> parallelizeFromArray(Function<T, ? extends Iterable<M>> parallelizeFromArray) {
      if (Objects.isNull(parallelizeFromArray)) {
        throw new FlowBuilderException(ParallelFlowBuilder.class, "parallelizeFromArray is mandatory");
      }
      this.parallelizeFromArray = parallelizeFromArray;
      return this;
    }

    public final ParallelFlowBuilder.MergeStrategy<T, M> parallelizedFlow(Flow<T> parallelizedFlow) {
      if (Objects.isNull(parallelizedFlow)) {
        throw new FlowBuilderException(ParallelFlowBuilder.class, "parallelizedFlow is mandatory");
      }
      this.flowToParallelize = parallelizedFlow;
      return this;
    }

    public final ParallelFlowBuilder.Build<T, M> mergeStrategy(BinaryOperator<T> mergeStrategy) {
      if (Objects.isNull(mergeStrategy)) {
        throw new FlowBuilderException(ParallelFlowBuilder.class, "mergeStrategy is mandatory");
      }
      this.mergeStrategy = mergeStrategy;
      return this;
    }

    public final ParallelFlow<T, M> build() {
      return ParallelFlow.create(this.name, this.flows, this.mergeStrategy, this.parallelizeFromArray, this.flowToParallelize);
    }
  }

  public interface Named<T extends FlowContext, M> {
    /**
     * Define flow name.
     *
     * @param name The name
     * @return {@link ParallelFlowBuilder.Parallelize} builder step
     */
    ParallelFlowBuilder.Parallelize<T, M> named(String name);
  }

  public interface Parallelize<T extends FlowContext, M> {
    /**
     * Define a list of {@link Flow}s to parallelize.
     *
     * @param flows {@link Flow}s
     * @return {@link ParallelFlowBuilder.MergeStrategy} builder step
     */
    ParallelFlowBuilder.MergeStrategy<T, M> parallelize(List<Flow<T>> flows);

    /**
     * Define the function that should return an array, in order to parallelize {@link Flow} when iterate on it.
     *
     * @param parallelizeFromArray A function
     * @return {@link ParallelFlowBuilder.ParallelizedFlow} builder step
     */
    ParallelFlowBuilder.ParallelizedFlow<T, M> parallelizeFromArray(Function<T, ? extends Iterable<M>> parallelizeFromArray);

  }

  public interface ParallelizedFlow<T extends FlowContext, M> {
    /**
     * Define the {@link Flow} to parallelize if {@link BuildSteps#parallelizeFromArray} is used.
     *
     * @param flow A {@link Flow}
     * @return {@link ParallelFlowBuilder.MergeStrategy} builder step
     */
    ParallelFlowBuilder.MergeStrategy<T, M> parallelizedFlow(Flow<T> flow);
  }

  public interface MergeStrategy<T extends FlowContext, M> {
    /**
     * Define the merge strategy.
     *
     * @param mergeStrategy The merge strategy
     * @return {@link ParallelFlowBuilder.Build} builder step
     */
    ParallelFlowBuilder.Build<T, M> mergeStrategy(BinaryOperator<T> mergeStrategy);
  }

  public interface Build<T extends FlowContext, M> {
    /**
     * Build the {@link ParallelFlow}.
     *
     * @return Built {@link ParallelFlow}
     */
    ParallelFlow<T, M> build();
  }
}
