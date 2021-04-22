package fr.jtools.reactorflow.builder;

import fr.jtools.reactorflow.exception.FlowBuilderException;
import fr.jtools.reactorflow.flow.Flow;
import fr.jtools.reactorflow.flow.ParallelFlow;
import fr.jtools.reactorflow.report.FlowContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BinaryOperator;
import java.util.function.Function;

public final class ParallelFlowBuilder {
  public static <T extends FlowContext> BinaryOperator<T> defaultMergeStrategy() {
    return (context1, context2) -> context1;
  }

  public static <T extends FlowContext, M> ParallelFlowBuilder.Named<T, M> defaultBuilder() {
    return new ParallelFlowBuilder.BuildSteps<>();
  }

  public static <T extends FlowContext, M> ParallelFlowBuilder.Named<T, M> builderForContextOfType(Class<T> contextClass) {
    return new ParallelFlowBuilder.BuildSteps<>();
  }

  public static <T extends FlowContext, M> ParallelFlowBuilder.Named<T, M> builderForMetadataType(Class<M> metadataClass) {
    return new ParallelFlowBuilder.BuildSteps<>();
  }

  public static <T extends FlowContext, M> ParallelFlowBuilder.Named<T, M> builderForTypes(Class<T> contextClass, Class<M> metadataClass) {
    return new ParallelFlowBuilder.BuildSteps<>();
  }

  private static final class BuildSteps<T extends FlowContext, M> implements ParallelFlowBuilder.Named<T, M>,
      ParallelFlowBuilder.Parallelize<T, M>,
      ParallelFlowBuilder.ParallelizedFlow<T, M>,
      ParallelFlowBuilder.MergeStrategy<T, M>,
      ParallelFlowBuilder.Build<T, M> {
    private final List<Flow<T>> flows = new ArrayList<>();
    private Function<T, ? extends Iterable<M>> parallelizeFromArray;
    private Flow<T> flowToParallelize;
    private BinaryOperator<T> mergeStrategy;
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
    ParallelFlowBuilder.Parallelize<T, M> named(String name);
  }

  public interface Parallelize<T extends FlowContext, M> {
    ParallelFlowBuilder.MergeStrategy<T, M> parallelize(List<Flow<T>> flows);

    ParallelFlowBuilder.ParallelizedFlow<T, M> parallelizeFromArray(Function<T, ? extends Iterable<M>> parallelizeFromArray);

  }

  public interface ParallelizedFlow<T extends FlowContext, M> {
    ParallelFlowBuilder.MergeStrategy<T, M> parallelizedFlow(Flow<T> flow);
  }

  public interface MergeStrategy<T extends FlowContext, M> {
    ParallelFlowBuilder.Build<T, M> mergeStrategy(BinaryOperator<T> mergeStrategy);
  }

  public interface Build<T extends FlowContext, M> {
    ParallelFlow<T, M> build();
  }
}
