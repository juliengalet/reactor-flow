package fr.jtools.reactorflow.builder;

import fr.jtools.reactorflow.exception.FlowBuilderException;
import fr.jtools.reactorflow.exception.RecoverableFlowException;
import fr.jtools.reactorflow.flow.Flow;
import fr.jtools.reactorflow.flow.RetryableFlow;
import fr.jtools.reactorflow.state.FlowContext;

import java.util.Objects;

public final class RetryableFlowBuilder {
  public static <T extends FlowContext> RetryableFlowBuilder.Named<T> defaultBuilder() {
    return new RetryableFlowBuilder.BuildSteps<>();
  }

  public static <T extends FlowContext> RetryableFlowBuilder.Named<T> builderForContextOfType(Class<T> contextClass) {
    return new RetryableFlowBuilder.BuildSteps<>();
  }

  private static final class BuildSteps<T extends FlowContext> implements RetryableFlowBuilder.Named<T>,
      RetryableFlowBuilder.Try<T>,
      RetryableFlowBuilder.RetryOn<T>,
      RetryableFlowBuilder.RetryTimes<T>,
      RetryableFlowBuilder.Delay<T>,
      RetryableFlowBuilder.Build<T> {

    private String name;
    private Flow<T> flow;
    private RecoverableFlowException retryOn = RecoverableFlowException.TECHNICAL;
    private Integer retryTimes = 1;
    private Integer delay = 100;

    private BuildSteps() {
    }

    public final RetryableFlowBuilder.Try<T> named(String name) {
      if (Objects.isNull(name)) {
        throw new FlowBuilderException(RetryableFlowBuilder.class, "name is mandatory");
      }
      this.name = name;
      return this;
    }

    public final RetryableFlowBuilder.RetryOn<T> tryFlow(Flow<T> flow) {
      if (Objects.isNull(flow)) {
        throw new FlowBuilderException(RetryableFlowBuilder.class, "try flow is mandatory");
      }
      this.flow = flow;
      return this;
    }

    public final RetryableFlowBuilder.RetryTimes<T> retryOn(RecoverableFlowException retryOn) {
      if (Objects.isNull(retryOn)) {
        throw new FlowBuilderException(RetryableFlowBuilder.class, "retryOn is mandatory");
      }
      this.retryOn = retryOn;
      return this;
    }

    public final RetryableFlowBuilder.Delay<T> retryTimes(Integer retryTimes) {
      if (Objects.isNull(retryTimes)) {
        throw new FlowBuilderException(RetryableFlowBuilder.class, "retryTimes is mandatory");
      }
      this.retryTimes = retryTimes;
      return this;
    }

    public final RetryableFlowBuilder.Build<T> delay(Integer delay) {
      if (Objects.isNull(delay)) {
        throw new FlowBuilderException(RetryableFlowBuilder.class, "delay is mandatory");
      }
      this.delay = delay;
      return this;
    }

    public final RetryableFlow<T> build() {
      return RetryableFlow.create(this.name, this.flow, this.retryTimes, this.delay, this.retryOn);
    }
  }

  public interface Delay<T extends FlowContext> {
    RetryableFlowBuilder.Build<T> delay(Integer delay);
  }

  public interface RetryOn<T extends FlowContext> {
    RetryableFlowBuilder.RetryTimes<T> retryOn(RecoverableFlowException retryOn);
  }

  public interface RetryTimes<T extends FlowContext> {
    RetryableFlowBuilder.Delay<T> retryTimes(Integer retryTimes);
  }

  public interface Try<T extends FlowContext> {
    RetryableFlowBuilder.RetryOn<T> tryFlow(Flow<T> flow);
  }

  public interface Build<T extends FlowContext> {
    RetryableFlow<T> build();
  }

  public interface Named<T extends FlowContext> {
    RetryableFlowBuilder.Try<T> named(String name);
  }
}
