package fr.jtools.reactorflow.builder;

import fr.jtools.reactorflow.exception.FlowBuilderException;
import fr.jtools.reactorflow.flow.Flow;
import fr.jtools.reactorflow.flow.SwitchFlow;
import fr.jtools.reactorflow.report.FlowContext;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * Class used to build a {@link SwitchFlow}.
 */
public final class SwitchFlowBuilder {
  /**
   * Get a default builder for {@link FlowContext} context.
   *
   * @param <T> Context type
   * @return {@link SwitchFlowBuilder.Named} builder step
   */
  public static <T extends FlowContext> SwitchFlowBuilder.Named<T> defaultBuilder() {
    return new SwitchFlowBuilder.BuildSteps<>();
  }

  /**
   * Get a builder for a specified context class.
   *
   * @param contextClass Context class that will be inferred to {@link T}
   * @param <T>          Context type
   * @return {@link SwitchFlowBuilder.Named} builder step
   */
  public static <T extends FlowContext> SwitchFlowBuilder.Named<T> builderForContextOfType(Class<T> contextClass) {
    return new SwitchFlowBuilder.BuildSteps<>();
  }

  private static final class BuildSteps<T extends FlowContext> implements SwitchFlowBuilder.Named<T>,
      SwitchFlowBuilder.SwitchCondition<T>,
      SwitchFlowBuilder.SwitchCase<T>,
      SwitchFlowBuilder.DefaultCase<T>,
      SwitchFlowBuilder.Build<T> {
    /**
     * The name.
     */
    private String name;
    /**
     * A {@link Map} containing switch case keys and {@link Flow}s to execute.
     */
    private final Map<String, Flow<T>> flows = new HashMap<>();
    /**
     * The default {@link Flow} is no switch case matches.
     */
    private Flow<T> defaultFlow;
    /**
     * The switch condition.
     */
    private Function<T, String> switchCondition;

    private BuildSteps() {
    }

    public final SwitchCondition<T> named(String name) {
      if (Objects.isNull(name)) {
        throw new FlowBuilderException(SwitchFlowBuilder.class, "name is mandatory");
      }
      this.name = name;
      return this;
    }

    public final SwitchCase<T> switchCondition(Function<T, String> switchCondition) {
      if (Objects.isNull(switchCondition)) {
        throw new FlowBuilderException(SwitchFlowBuilder.class, "switchCondition is mandatory");
      }
      this.switchCondition = switchCondition;
      return this;
    }

    public final SwitchFlowBuilder.SwitchCase<T> switchCase(String key, Flow<T> flow) {
      if (Objects.isNull(flow) || Objects.isNull(key)) {
        throw new FlowBuilderException(SwitchFlowBuilder.class, "key and switchCase flow are mandatory");
      }
      this.flows.put(key, flow);
      return this;
    }

    public final SwitchFlowBuilder.Build<T> defaultCase(Flow<T> flow) {
      if (Objects.isNull(flow)) {
        throw new FlowBuilderException(SwitchFlowBuilder.class, "default flow is mandatory");
      }
      this.defaultFlow = flow;
      return this;
    }

    public final SwitchFlow<T> build() {
      return SwitchFlow.create(this.name, this.switchCondition, this.flows, this.defaultFlow);
    }
  }

  public interface SwitchCase<T extends FlowContext> extends DefaultCase<T> {
    /**
     * Define a switch case flow, executed if {@link SwitchFlowBuilder.SwitchCondition} returns the matching key.
     *
     * @param key  A key
     * @param flow A {@link Flow}
     * @return {@link SwitchFlowBuilder.SwitchCase} builder step
     */
    SwitchFlowBuilder.SwitchCase<T> switchCase(String key, Flow<T> flow);
  }

  public interface DefaultCase<T extends FlowContext> {
    /**
     * Define the default case, if switch condition match none of the switch cases.
     *
     * @param flow A {@link Flow}
     * @return {@link SwitchFlowBuilder.Build} builder step
     */
    SwitchFlowBuilder.Build<T> defaultCase(Flow<T> flow);
  }

  public interface Build<T extends FlowContext> {
    /**
     * Build the {@link SwitchFlow}.
     *
     * @return Built {@link SwitchFlow}
     */
    SwitchFlow<T> build();
  }

  public interface Named<T extends FlowContext> {
    /**
     * Define flow name.
     *
     * @param name The name
     * @return {@link SwitchFlowBuilder.SwitchCondition} builder step
     */
    SwitchFlowBuilder.SwitchCondition<T> named(String name);
  }

  public interface SwitchCondition<T extends FlowContext> {
    /**
     * Define the switch condition that will decide which flow should be executed.
     *
     * @param switchCondition The switchCondition, a function returning a {@link String} mapped from {@link T} context
     * @return {@link SwitchFlowBuilder.SwitchCase} builder step
     */
    SwitchFlowBuilder.SwitchCase<T> switchCondition(Function<T, String> switchCondition);
  }
}
