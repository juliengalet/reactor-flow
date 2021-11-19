package io.github.juliengalet.reactorflow.builder;

import io.github.juliengalet.reactorflow.exception.FlowBuilderException;
import io.github.juliengalet.reactorflow.flow.ConditionalFlow;
import io.github.juliengalet.reactorflow.flow.Flow;
import io.github.juliengalet.reactorflow.report.FlowContext;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * Class used to build a {@link ConditionalFlow}.
 */
public final class ConditionalFlowBuilder {
  /**
   * Get a default builder for {@link FlowContext} context.
   *
   * @param <T> Context type
   * @return {@link ConditionalFlowBuilder.Named} builder step
   */
  public static <T extends FlowContext> ConditionalFlowBuilder.Named<T> defaultBuilder() {
    return new ConditionalFlowBuilder.BuildSteps<>();
  }

  /**
   * Get a builder for a specified context class.
   *
   * @param contextClass Context class that will be inferred to {@link T}
   * @param <T>          Context type
   * @return {@link ConditionalFlowBuilder.Named} builder step
   */
  public static <T extends FlowContext> ConditionalFlowBuilder.Named<T> builderForContextOfType(Class<T> contextClass) {
    return new ConditionalFlowBuilder.BuildSteps<>();
  }

  private static final class BuildSteps<T extends FlowContext> implements ConditionalFlowBuilder.Named<T>,
      ConditionalFlowBuilder.Condition<T>,
      ConditionalFlowBuilder.CaseTrue<T>,
      ConditionalFlowBuilder.CaseFalse<T>,
      ConditionalFlowBuilder.Build<T> {
    /**
     * The name.
     */
    private String name;
    /**
     * The {@link Flow} to execute if condition is true.
     */
    private Flow<T> flowCaseTrue;
    /**
     * The {@link Flow} to execute if condition is false.
     */
    private Flow<T> flowCaseFalse;
    /**
     * The condition.
     */
    private Predicate<T> condition;

    private BuildSteps() {
    }

    public final ConditionalFlowBuilder.Condition<T> named(String name) {
      if (Objects.isNull(name)) {
        throw new FlowBuilderException(ConditionalFlowBuilder.class, "name is mandatory");
      }
      this.name = name;
      return this;
    }

    public final ConditionalFlowBuilder.CaseTrue<T> condition(Predicate<T> condition) {
      if (Objects.isNull(condition)) {
        throw new FlowBuilderException(ConditionalFlowBuilder.class, "condition is mandatory");
      }
      this.condition = condition;
      return this;
    }

    public final ConditionalFlowBuilder.CaseFalse<T> caseTrue(Flow<T> flow) {
      if (Objects.isNull(flow)) {
        throw new FlowBuilderException(ConditionalFlowBuilder.class, "caseTrue is mandatory");
      }
      this.flowCaseTrue = flow;
      return this;
    }

    public final ConditionalFlowBuilder.Build<T> caseFalse(Flow<T> flow) {
      if (Objects.isNull(flow)) {
        throw new FlowBuilderException(ConditionalFlowBuilder.class, "caseFalse is mandatory");
      }
      this.flowCaseFalse = flow;
      return this;
    }

    public final ConditionalFlow<T> build() {
      return ConditionalFlow.create(this.name, this.condition, this.flowCaseTrue, this.flowCaseFalse);
    }
  }

  public interface CaseTrue<T extends FlowContext> {
    /**
     * The {@link Flow} executed if condition is true.
     *
     * @param flow The flow
     * @return {@link ConditionalFlowBuilder.CaseFalse} builder step
     */
    ConditionalFlowBuilder.CaseFalse<T> caseTrue(Flow<T> flow);
  }

  public interface CaseFalse<T extends FlowContext> {
    /**
     * The {@link Flow} executed if condition is false.
     *
     * @param flow The flow
     * @return {@link ConditionalFlowBuilder.Build} builder step
     */
    ConditionalFlowBuilder.Build<T> caseFalse(Flow<T> flow);
  }

  public interface Build<T extends FlowContext> {
    /**
     * Build the {@link ConditionalFlow}.
     *
     * @return Built {@link ConditionalFlow}
     */
    ConditionalFlow<T> build();
  }

  public interface Named<T extends FlowContext> {
    /**
     * Define flow name.
     *
     * @param name The name
     * @return {@link ConditionalFlowBuilder.Condition} builder step
     */
    ConditionalFlowBuilder.Condition<T> named(String name);
  }

  public interface Condition<T extends FlowContext> {
    /**
     * Define flow condition
     *
     * @param condition The condition
     * @return {@link ConditionalFlowBuilder.CaseTrue} builder step
     */
    ConditionalFlowBuilder.CaseTrue<T> condition(Predicate<T> condition);
  }
}

