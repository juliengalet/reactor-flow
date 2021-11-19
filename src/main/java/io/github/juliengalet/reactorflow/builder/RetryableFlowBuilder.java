package io.github.juliengalet.reactorflow.builder;

import io.github.juliengalet.reactorflow.exception.FlowBuilderException;
import io.github.juliengalet.reactorflow.exception.RecoverableFlowException;
import io.github.juliengalet.reactorflow.flow.Flow;
import io.github.juliengalet.reactorflow.flow.RetryableFlow;
import io.github.juliengalet.reactorflow.report.FlowContext;
import io.github.juliengalet.reactorflow.exception.FlowException;

import java.util.Objects;

/**
 * Class used to build a {@link RetryableFlow}.
 */
public final class RetryableFlowBuilder {
  /**
   * Get a default builder for {@link FlowContext} context.
   *
   * @param <T> Context type
   * @return {@link RetryableFlowBuilder.Named} builder step
   */
  public static <T extends FlowContext> RetryableFlowBuilder.Named<T> defaultBuilder() {
    return new RetryableFlowBuilder.BuildSteps<>();
  }

  /**
   * Get a builder for a specified context class.
   *
   * @param contextClass Context class that will be inferred to {@link T}
   * @param <T>          Context type
   * @return {@link RetryableFlowBuilder.Named} builder step
   */
  public static <T extends FlowContext> RetryableFlowBuilder.Named<T> builderForContextOfType(Class<T> contextClass) {
    return new RetryableFlowBuilder.BuildSteps<>();
  }

  private static final class BuildSteps<T extends FlowContext> implements RetryableFlowBuilder.Named<T>,
      RetryableFlowBuilder.Try<T>,
      RetryableFlowBuilder.RetryOn<T>,
      RetryableFlowBuilder.RetryTimes<T>,
      RetryableFlowBuilder.Delay<T>,
      RetryableFlowBuilder.Build<T> {
    /**
     * The name.
     */
    private String name;
    /**
     * The {@link Flow} to execute.
     */
    private Flow<T> flow;
    /**
     * The type of {@link FlowException} that should be retried.
     */
    private RecoverableFlowException retryOn = RecoverableFlowException.TECHNICAL;
    /**
     * The number of retries.
     */
    private Integer retryTimes = 1;
    /**
     * The delay between two retries in ms.
     */
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
    /**
     * Define the delay between failure and the next retry.
     *
     * @param delay The delay
     * @return {@link RetryableFlowBuilder.Build} builder step
     */
    RetryableFlowBuilder.Build<T> delay(Integer delay);
  }

  public interface RetryOn<T extends FlowContext> {
    /**
     * Define the type of {@link FlowException} that should be retried.
     *
     * @param retryOn The type of exception (using enum {@link RecoverableFlowException})
     * @return {@link RetryableFlowBuilder.RetryTimes} builder step
     */
    RetryableFlowBuilder.RetryTimes<T> retryOn(RecoverableFlowException retryOn);
  }

  public interface RetryTimes<T extends FlowContext> {
    /**
     * Define the number of retries.
     *
     * @param retryTimes The number of retries
     * @return {@link RetryableFlowBuilder.Delay} builder step
     */
    RetryableFlowBuilder.Delay<T> retryTimes(Integer retryTimes);
  }

  public interface Try<T extends FlowContext> {
    /**
     * Define the {@link Flow} to try.
     *
     * @param flow A {@link Flow}
     * @return {@link RetryableFlowBuilder.RetryOn} builder step
     */
    RetryableFlowBuilder.RetryOn<T> tryFlow(Flow<T> flow);
  }

  public interface Build<T extends FlowContext> {
    /**
     * Build the {@link RetryableFlow}.
     *
     * @return Built {@link RetryableFlow}
     */
    RetryableFlow<T> build();
  }

  public interface Named<T extends FlowContext> {
    /**
     * Define flow name.
     *
     * @param name The name
     * @return {@link RetryableFlowBuilder.Try} builder step
     */
    RetryableFlowBuilder.Try<T> named(String name);
  }
}
