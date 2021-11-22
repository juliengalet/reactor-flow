package io.github.juliengalet.reactorflow.builder;

import io.github.juliengalet.reactorflow.exception.FlowBuilderException;
import io.github.juliengalet.reactorflow.exception.RecoverableFlowException;
import io.github.juliengalet.reactorflow.flow.Flow;
import io.github.juliengalet.reactorflow.flow.RecoverableFlow;
import io.github.juliengalet.reactorflow.report.FlowContext;

import java.util.Objects;

/**
 * Class used to build a {@link RecoverableFlow}.
 */
public final class RecoverableFlowBuilder {
  /**
   * Get a default builder for {@link FlowContext} context.
   *
   * @param <T> Context type
   * @return {@link RecoverableFlowBuilder.Named} builder step
   */
  public static <T extends FlowContext> RecoverableFlowBuilder.Named<T> defaultBuilder() {
    return new RecoverableFlowBuilder.BuildSteps<>();
  }

  /**
   * Get a builder for a specified context class.
   *
   * @param contextClass Context class that will be inferred to {@link T}
   * @param <T>          Context type
   * @return {@link RecoverableFlowBuilder.Named} builder step
   */
  public static <T extends FlowContext> RecoverableFlowBuilder.Named<T> builderForContextOfType(Class<T> contextClass) {
    return new RecoverableFlowBuilder.BuildSteps<>();
  }

  private static final class BuildSteps<T extends FlowContext> implements RecoverableFlowBuilder.Named<T>,
      RecoverableFlowBuilder.Try<T>,
      RecoverableFlowBuilder.Recover<T>,
      RecoverableFlowBuilder.RecoverOn<T>,
      RecoverableFlowBuilder.Build<T> {

    /**
     * The name.
     */
    private String name;
    /**
     * The {@link Flow} to execute.
     */
    private Flow<T> flow;
    /**
     * The {@link Flow} used for recover.
     */
    private Flow<T> recover;
    /**
     * The type of exception that should be recovered.
     */
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
    /**
     * Define the {@link Flow} that is executed as recover.
     *
     * @param flow A {@link Flow}
     * @return {@link RecoverableFlowBuilder.RecoverOn} builder step
     */
    RecoverableFlowBuilder.RecoverOn<T> recover(Flow<T> flow);
  }

  public interface RecoverOn<T extends FlowContext> {
    /**
     * Define the type of exception to recover
     *
     * @param recoverOn The type of exception to recover
     * @return {@link RecoverableFlowBuilder.Build} builder step
     */
    RecoverableFlowBuilder.Build<T> recoverOn(RecoverableFlowException recoverOn);
  }

  public interface Try<T extends FlowContext> {
    /**
     * Define the {@link Flow} to try.
     *
     * @param flow A {@link Flow}
     * @return {@link RecoverableFlowBuilder.Recover} builder step
     */
    RecoverableFlowBuilder.Recover<T> tryFlow(Flow<T> flow);
  }

  public interface Build<T extends FlowContext> {
    /**
     * Build the {@link RecoverableFlow}.
     *
     * @return Built {@link RecoverableFlow}
     */
    RecoverableFlow<T> build();
  }

  public interface Named<T extends FlowContext> {
    /**
     * Define flow name.
     *
     * @param name The name
     * @return {@link RecoverableFlowBuilder.Try} builder step
     */
    RecoverableFlowBuilder.Try<T> named(String name);
  }
}
