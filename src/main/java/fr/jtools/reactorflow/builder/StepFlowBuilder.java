package fr.jtools.reactorflow.builder;

import fr.jtools.reactorflow.exception.FlowBuilderException;
import fr.jtools.reactorflow.flow.Step;
import fr.jtools.reactorflow.flow.StepFlow;
import fr.jtools.reactorflow.state.FlowContext;

import java.util.Objects;

public final class StepFlowBuilder {
  public static <T extends FlowContext, M> StepFlowBuilder.Named<T, M> defaultBuilder() {
    return new StepFlowBuilder.BuildSteps<>();
  }

  public static <T extends FlowContext, M>  StepFlowBuilder.Named<T, M> builderForContextOfType(Class<T> contextClass) {
    return new StepFlowBuilder.BuildSteps<>();
  }

  public static <T extends FlowContext, M>  StepFlowBuilder.Named<T, M> builderForMetadataType(Class<M> metadataClass) {
    return new StepFlowBuilder.BuildSteps<>();
  }

  public static <T extends FlowContext, M>  StepFlowBuilder.Named<T, M> builderForTypes(Class<T> contextClass, Class<M> metadataClass) {
    return new StepFlowBuilder.BuildSteps<>();
  }

  private static final class BuildSteps<T extends FlowContext, M> implements StepFlowBuilder.Named<T, M>,
      StepFlowBuilder.Execution<T, M>,
      StepFlowBuilder.Build<T, M> {
    private Step<T, M> execution;
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

    public final StepFlowBuilder.Build<T, M> execution(Step<T, M> execution) {
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
    StepFlow<T, M> build();
  }

  public interface Named<T extends FlowContext, M> {
    StepFlowBuilder.Execution<T, M> named(String name);
  }

  public interface Execution<T extends FlowContext, M> {
    StepFlowBuilder.Build<T, M> execution(Step<T, M> execution);
  }
}
