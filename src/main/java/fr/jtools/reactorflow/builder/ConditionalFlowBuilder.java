package fr.jtools.reactorflow.builder;

import fr.jtools.reactorflow.exception.FlowBuilderException;
import fr.jtools.reactorflow.flow.ConditionalFlow;
import fr.jtools.reactorflow.flow.Flow;
import fr.jtools.reactorflow.report.FlowContext;

import java.util.Objects;
import java.util.function.Predicate;

public final class ConditionalFlowBuilder {
  public static <T extends FlowContext> ConditionalFlowBuilder.Named<T> defaultBuilder() {
    return new ConditionalFlowBuilder.BuildSteps<>();
  }

  public static <T extends FlowContext> ConditionalFlowBuilder.Named<T> builderForContextOfType(Class<T> contextClass) {
    return new ConditionalFlowBuilder.BuildSteps<>();
  }

  private static final class BuildSteps<T extends FlowContext> implements ConditionalFlowBuilder.Named<T>,
      ConditionalFlowBuilder.Condition<T>,
      ConditionalFlowBuilder.CaseTrue<T>,
      ConditionalFlowBuilder.CaseFalse<T>,
      ConditionalFlowBuilder.Build<T> {
    private String name;
    private Flow<T> flowCaseTrue;
    private Flow<T> flowCaseFalse;

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
    ConditionalFlowBuilder.CaseFalse<T> caseTrue(Flow<T> flow);
  }

  public interface CaseFalse<T extends FlowContext> {
    ConditionalFlowBuilder.Build<T> caseFalse(Flow<T> flow);
  }

  public interface Build<T extends FlowContext> {
    ConditionalFlow<T> build();
  }

  public interface Named<T extends FlowContext> {
    ConditionalFlowBuilder.Condition<T> named(String name);
  }

  public interface Condition<T extends FlowContext> {
    ConditionalFlowBuilder.CaseTrue<T> condition(Predicate<T> condition);
  }
}

