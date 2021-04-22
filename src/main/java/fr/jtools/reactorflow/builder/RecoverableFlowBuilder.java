package fr.jtools.reactorflow.builder;

import fr.jtools.reactorflow.exception.FlowBuilderException;
import fr.jtools.reactorflow.exception.RecoverableFlowException;
import fr.jtools.reactorflow.flow.Flow;
import fr.jtools.reactorflow.flow.RecoverableFlow;
import fr.jtools.reactorflow.report.FlowContext;

import java.util.Objects;

public final class RecoverableFlowBuilder {
  public static <T extends FlowContext> RecoverableFlowBuilder.Named<T> defaultBuilder() {
    return new RecoverableFlowBuilder.BuildSteps<>();
  }

  public static <T extends FlowContext> RecoverableFlowBuilder.Named<T> builderForContextOfType(Class<T> contextClass) {
    return new RecoverableFlowBuilder.BuildSteps<>();
  }

  private static final class BuildSteps<T extends FlowContext> implements RecoverableFlowBuilder.Named<T>,
      RecoverableFlowBuilder.Try<T>,
      RecoverableFlowBuilder.Recover<T>,
      RecoverableFlowBuilder.RecoverOn<T>,
      RecoverableFlowBuilder.Build<T> {

    private String name;
    private Flow<T> flow;
    private Flow<T> recover;
    private RecoverableFlowException recoverOn;


    private BuildSteps() {
    }

    public final RecoverableFlowBuilder.Try<T> named(String name) {
      if (Objects.isNull(name)) {
        throw new FlowBuilderException(RecoverableFlowBuilder.class, "name is mandatory");
      }
      this.name = name;
      return this;
    }

    public final RecoverableFlowBuilder.Recover<T> tryFlow(Flow<T> flow) {
      if (Objects.isNull(flow)) {
        throw new FlowBuilderException(RecoverableFlowBuilder.class, "try flow is mandatory");
      }
      this.flow = flow;
      return this;
    }

    public final RecoverableFlowBuilder.RecoverOn<T> recover(Flow<T> recoveredFlow) {
      if (Objects.isNull(recoveredFlow)) {
        throw new FlowBuilderException(RecoverableFlowBuilder.class, "recovered flow is mandatory");
      }
      this.recover = recoveredFlow;
      return this;
    }

    public final RecoverableFlowBuilder.Build<T> recoverOn(RecoverableFlowException recoverOn) {
      if (Objects.isNull(recoverOn)) {
        throw new FlowBuilderException(RecoverableFlowBuilder.class, "recoverOn is mandatory");
      }
      this.recoverOn = recoverOn;
      return this;
    }

    public final RecoverableFlow<T> build() {
      return RecoverableFlow.create(this.name, this.flow, this.recover, this.recoverOn);
    }
  }

  public interface Recover<T extends FlowContext> {
    RecoverableFlowBuilder.RecoverOn<T> recover(Flow<T> flow);
  }

  public interface RecoverOn<T extends FlowContext> {
    RecoverableFlowBuilder.Build<T> recoverOn(RecoverableFlowException recoverOn);
  }

  public interface Try<T extends FlowContext> {
    RecoverableFlowBuilder.Recover<T> tryFlow(Flow<T> flow);
  }

  public interface Build<T extends FlowContext> {
    RecoverableFlow<T> build();
  }

  public interface Named<T extends FlowContext> {
    RecoverableFlowBuilder.Try<T> named(String name);
  }
}
