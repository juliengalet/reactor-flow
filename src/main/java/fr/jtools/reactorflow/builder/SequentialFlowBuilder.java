package fr.jtools.reactorflow.builder;

import fr.jtools.reactorflow.exception.FlowBuilderException;
import fr.jtools.reactorflow.flow.Flow;
import fr.jtools.reactorflow.flow.SequentialFlow;
import fr.jtools.reactorflow.state.FlowContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class SequentialFlowBuilder {
  public static <T extends FlowContext> SequentialFlowBuilder.Named<T> defaultBuilder() {
    return new SequentialFlowBuilder.BuildSteps<>();
  }

  public static <T extends FlowContext> SequentialFlowBuilder.Named<T> builderForContextOfType(Class<T> contextClass) {
    return new SequentialFlowBuilder.BuildSteps<>();
  }

  private static final class BuildSteps<T extends FlowContext> implements SequentialFlowBuilder.Named<T>,
      SequentialFlowBuilder.Then<T>,
      SequentialFlowBuilder.Finally<T>,
      SequentialFlowBuilder.Build<T> {

    private final List<Flow<T>> flows = new ArrayList<>();

    private Flow<T> finalFlow;
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
    SequentialFlowBuilder.Then<T> then(Flow<T> flow);
  }

  public interface Finally<T extends FlowContext> {
    SequentialFlowBuilder.Build<T> doFinally(Flow<T> flow);
  }

  public interface Build<T extends FlowContext> {
    SequentialFlow<T> build();
  }

  public interface Named<T extends FlowContext> {
    SequentialFlowBuilder.Then<T> named(String name);
  }
}
