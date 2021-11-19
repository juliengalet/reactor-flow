package io.github.juliengalet.reactorflow.flow;

import io.github.juliengalet.reactorflow.builder.ConditionalFlowBuilder;
import io.github.juliengalet.reactorflow.exception.FlowBuilderException;
import io.github.juliengalet.reactorflow.report.FlowContext;
import io.github.juliengalet.reactorflow.report.Status;
import io.github.juliengalet.reactorflow.testutils.CustomContext;
import io.github.juliengalet.reactorflow.testutils.ErrorMonoStepFlow;
import io.github.juliengalet.reactorflow.testutils.ErrorRawStepFlow;
import io.github.juliengalet.reactorflow.testutils.ErrorStepFlow;
import io.github.juliengalet.reactorflow.testutils.SuccessStepFlow;
import io.github.juliengalet.reactorflow.testutils.WarningStepFlow;
import io.github.juliengalet.reactorflow.testutils.TestUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

final class ConditionalFlowTest {
  @Test
  void givenSuccessCaseTrueFlow_conditionalFlow_shouldSuccess() {
    ConditionalFlow<FlowContext> conditionalFlow = ConditionalFlowBuilder
        .defaultBuilder()
        .named("Test")
        .condition(globalReport -> Boolean.TRUE)
        .caseTrue(SuccessStepFlow.flowNamed("Case True"))
        .caseFalse(SuccessStepFlow.flowNamed("Case False"))
        .build();

    StepVerifier
        .create(conditionalFlow.run(FlowContext.create()))
        .assertNext(TestUtils.assertAndLog(globalReport -> {
          Assertions.assertThat(globalReport.getName()).isEqualTo("Test");
          assertThat(globalReport.getContext().get("Case True")).isEqualTo("Case True");
          assertThat(globalReport.getContext().get("Case False")).isNull();
          assertThat(globalReport.getStatus()).isEqualTo(Status.SUCCESS);
          Assertions.assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          Assertions.assertThat(globalReport.getAllErrors()).isEmpty();
          Assertions.assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  void givenClonedWithNewNameSuccessCaseTrueFlow_conditionalFlow_shouldSuccess() {
    ConditionalFlow<FlowContext> conditionalFlow = ConditionalFlowBuilder
        .defaultBuilder()
        .named("Test")
        .condition(globalReport -> Boolean.TRUE)
        .caseTrue(SuccessStepFlow.flowNamed("Case True"))
        .caseFalse(SuccessStepFlow.flowNamed("Case False"))
        .build();

    StepVerifier
        .create(conditionalFlow.cloneFlow("Test copy").run(FlowContext.create()))
        .assertNext(TestUtils.assertAndLog(globalReport -> {
          Assertions.assertThat(globalReport.getName()).isEqualTo("Test copy");
          assertThat(globalReport.getContext().get("Case True")).isEqualTo("Case True");
          assertThat(globalReport.getContext().get("Case False")).isNull();
          assertThat(globalReport.getStatus()).isEqualTo(Status.SUCCESS);
          Assertions.assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          Assertions.assertThat(globalReport.getAllErrors()).isEmpty();
          Assertions.assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }


  @Test
  void givenClonedSuccessCaseTrueFlow_conditionalFlow_shouldSuccess() {
    ConditionalFlow<FlowContext> conditionalFlow = ConditionalFlowBuilder
        .defaultBuilder()
        .named("Test")
        .condition(globalReport -> Boolean.TRUE)
        .caseTrue(SuccessStepFlow.flowNamed("Case True"))
        .caseFalse(SuccessStepFlow.flowNamed("Case False"))
        .build();

    StepVerifier
        .create(conditionalFlow.cloneFlow().run(FlowContext.create()))
        .assertNext(TestUtils.assertAndLog(globalReport -> {
          Assertions.assertThat(globalReport.getName()).isEqualTo("Test");
          assertThat(globalReport.getContext().get("Case True")).isEqualTo("Case True");
          assertThat(globalReport.getContext().get("Case False")).isNull();
          assertThat(globalReport.getStatus()).isEqualTo(Status.SUCCESS);
          Assertions.assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          Assertions.assertThat(globalReport.getAllErrors()).isEmpty();
          Assertions.assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  void givenSuccessCaseTrueFlowWithCustomContext_conditionalFlow_shouldSuccess() {
    ConditionalFlow<CustomContext> conditionalFlow = ConditionalFlowBuilder
        .builderForContextOfType(CustomContext.class)
        .named("Test")
        .condition(globalReport -> Boolean.TRUE)
        .caseTrue(SuccessStepFlow.flowNamed("Case True"))
        .caseFalse(SuccessStepFlow.flowNamed("Case False"))
        .build();

    StepVerifier
        .create(conditionalFlow.run(new CustomContext()))
        .assertNext(TestUtils.assertAndLog(globalReport -> {
          assertThat(globalReport.getContext().customField).isNotNull();
          assertThat(globalReport.getContext().get("Case True")).isEqualTo("Case True");
          assertThat(globalReport.getContext().get("Case False")).isNull();
          assertThat(globalReport.getStatus()).isEqualTo(Status.SUCCESS);
          Assertions.assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          Assertions.assertThat(globalReport.getAllErrors()).isEmpty();
          Assertions.assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  void givenFailedCaseTrueFlow_conditionalFlow_shouldError() {
    ConditionalFlow<FlowContext> conditionalFlow = ConditionalFlowBuilder
        .defaultBuilder()
        .named("Test")
        .condition(globalReport -> Boolean.TRUE)
        .caseTrue(ErrorStepFlow.flowNamed("Case True"))
        .caseFalse(SuccessStepFlow.flowNamed("Case False"))
        .build();

    StepVerifier
        .create(conditionalFlow.run(FlowContext.create()))
        .assertNext(TestUtils.assertAndLog(globalReport -> {
          assertThat(globalReport.getContext().get("Case True")).isNull();
          assertThat(globalReport.getContext().get("Case False")).isNull();
          assertThat(globalReport.getStatus()).isEqualTo(Status.ERROR);
          Assertions.assertThat(globalReport.getAllErrors()).hasSize(1);
          Assertions.assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          Assertions.assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  void givenRawFailedCaseTrueFlow_conditionalFlow_shouldError() {
    ConditionalFlow<FlowContext> conditionalFlow = ConditionalFlowBuilder
        .defaultBuilder()
        .named("Test")
        .condition(globalReport -> Boolean.TRUE)
        .caseTrue(ErrorRawStepFlow.flowNamed("Case True"))
        .caseFalse(SuccessStepFlow.flowNamed("Case False"))
        .build();

    StepVerifier
        .create(conditionalFlow.run(FlowContext.create()))
        .assertNext(TestUtils.assertAndLog(globalReport -> {
          assertThat(globalReport.getContext().get("Case True")).isNull();
          assertThat(globalReport.getContext().get("Case False")).isNull();
          assertThat(globalReport.getStatus()).isEqualTo(Status.ERROR);
          Assertions.assertThat(globalReport.getAllErrors()).hasSize(1);
          Assertions.assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          Assertions.assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  void givenMonoFailedCaseTrueFlow_conditionalFlow_shouldError() {
    ConditionalFlow<FlowContext> conditionalFlow = ConditionalFlowBuilder
        .defaultBuilder()
        .named("Test")
        .condition(globalReport -> Boolean.TRUE)
        .caseTrue(ErrorMonoStepFlow.flowNamed("Case True"))
        .caseFalse(SuccessStepFlow.flowNamed("Case False"))
        .build();

    StepVerifier
        .create(conditionalFlow.run(FlowContext.create()))
        .assertNext(TestUtils.assertAndLog(globalReport -> {
          assertThat(globalReport.getContext().get("Case True")).isNull();
          assertThat(globalReport.getContext().get("Case False")).isNull();
          assertThat(globalReport.getStatus()).isEqualTo(Status.ERROR);
          Assertions.assertThat(globalReport.getAllErrors()).hasSize(1);
          Assertions.assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          Assertions.assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  void givenWarningCaseTrueFlow_conditionalFlow_shouldWarning() {
    ConditionalFlow<FlowContext> conditionalFlow = ConditionalFlowBuilder
        .defaultBuilder()
        .named("Test")
        .condition(globalReport -> Boolean.TRUE)
        .caseTrue(WarningStepFlow.flowNamed("Case True"))
        .caseFalse(SuccessStepFlow.flowNamed("Case False"))
        .build();

    StepVerifier
        .create(conditionalFlow.run(FlowContext.create()))
        .assertNext(TestUtils.assertAndLog(globalReport -> {
          assertThat(globalReport.getContext().get("Case True")).isEqualTo("Case True");
          assertThat(globalReport.getContext().get("Case False")).isNull();
          assertThat(globalReport.getStatus()).isEqualTo(Status.WARNING);
          Assertions.assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          Assertions.assertThat(globalReport.getAllErrors()).isEmpty();
          Assertions.assertThat(globalReport.getAllWarnings()).hasSize(1);
        }))
        .verifyComplete();
  }

  @Test
  void givenSuccessCaseFalseFlow_conditionalFlow_shouldSuccess() {
    ConditionalFlow<FlowContext> conditionalFlow = ConditionalFlowBuilder
        .defaultBuilder()
        .named("Test")
        .condition(globalReport -> Boolean.FALSE)
        .caseTrue(SuccessStepFlow.flowNamed("Case True"))
        .caseFalse(SuccessStepFlow.flowNamed("Case False"))
        .build();

    StepVerifier
        .create(conditionalFlow.run(FlowContext.create()))
        .assertNext(TestUtils.assertAndLog(globalReport -> {
          Assertions.assertThat(globalReport.getName()).isEqualTo("Test");
          assertThat(globalReport.getContext().get("Case False")).isEqualTo("Case False");
          assertThat(globalReport.getContext().get("Case True")).isNull();
          assertThat(globalReport.getStatus()).isEqualTo(Status.SUCCESS);
          Assertions.assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          Assertions.assertThat(globalReport.getAllErrors()).isEmpty();
          Assertions.assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  void givenFailedCaseFalseFlow_conditionalFlow_shouldError() {
    ConditionalFlow<FlowContext> conditionalFlow = ConditionalFlowBuilder
        .defaultBuilder()
        .named("Test")
        .condition(globalReport -> Boolean.FALSE)
        .caseTrue(SuccessStepFlow.flowNamed("Case True"))
        .caseFalse(ErrorStepFlow.flowNamed("Case False"))
        .build();

    StepVerifier
        .create(conditionalFlow.run(FlowContext.create()))
        .assertNext(TestUtils.assertAndLog(globalReport -> {
          assertThat(globalReport.getContext().get("Case True")).isNull();
          assertThat(globalReport.getContext().get("Case False")).isNull();
          assertThat(globalReport.getStatus()).isEqualTo(Status.ERROR);
          Assertions.assertThat(globalReport.getAllErrors()).hasSize(1);
          Assertions.assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          Assertions.assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  void givenWarningCaseFalseFlow_conditionalFlow_shouldWarning() {
    ConditionalFlow<FlowContext> conditionalFlow = ConditionalFlowBuilder
        .defaultBuilder()
        .named("Test")
        .condition(globalReport -> Boolean.FALSE)
        .caseTrue(SuccessStepFlow.flowNamed("Case True"))
        .caseFalse(WarningStepFlow.flowNamed("Case False"))
        .build();

    StepVerifier
        .create(conditionalFlow.run(FlowContext.create()))
        .assertNext(TestUtils.assertAndLog(globalReport -> {
          assertThat(globalReport.getContext().get("Case False")).isEqualTo("Case False");
          assertThat(globalReport.getContext().get("Case True")).isNull();
          assertThat(globalReport.getStatus()).isEqualTo(Status.WARNING);
          Assertions.assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          Assertions.assertThat(globalReport.getAllErrors()).isEmpty();
          Assertions.assertThat(globalReport.getAllWarnings()).hasSize(1);
        }))
        .verifyComplete();
  }

  @Test
  void givenErrorInCondition_conditionalFlow_shouldWarning() {
    ConditionalFlow<FlowContext> conditionalFlow = ConditionalFlowBuilder
        .defaultBuilder()
        .named("Test")
        .condition(globalReport -> {
          throw new RuntimeException("Raw error");
        })
        .caseTrue(SuccessStepFlow.flowNamed("Case True"))
        .caseFalse(SuccessStepFlow.flowNamed("Case False"))
        .build();

    StepVerifier
        .create(conditionalFlow.run(FlowContext.create()))
        .assertNext(TestUtils.assertAndLog(globalReport -> {
          assertThat(globalReport.getContext().get("Case False")).isNull();
          assertThat(globalReport.getContext().get("Case True")).isNull();
          assertThat(globalReport.getStatus()).isEqualTo(Status.ERROR);
          Assertions.assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          Assertions.assertThat(globalReport.getAllErrors()).hasSize(1);
          Assertions.assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  void givenNullName_conditionalFlow_shouldNotBuild() {
    assertThatExceptionOfType(FlowBuilderException.class).isThrownBy(() -> ConditionalFlowBuilder
        .defaultBuilder()
        .named(null)
        .condition(globalReport -> Boolean.TRUE)
        .caseTrue(SuccessStepFlow.flowNamed("Case True"))
        .caseFalse(SuccessStepFlow.flowNamed("Case False"))
        .build()
    );
  }

  @Test
  void givenNullCaseFalse_conditionalFlow_shouldNotBuild() {
    assertThatExceptionOfType(FlowBuilderException.class).isThrownBy(() -> ConditionalFlowBuilder
        .defaultBuilder()
        .named("Test")
        .condition(globalReport -> Boolean.TRUE)
        .caseTrue(SuccessStepFlow.flowNamed("Case True"))
        .caseFalse(null)
        .build()
    );
  }

  @Test
  void givenNullCaseTrue_conditionalFlow_shouldNotBuild() {
    assertThatExceptionOfType(FlowBuilderException.class).isThrownBy(() -> ConditionalFlowBuilder
        .defaultBuilder()
        .named("Test")
        .condition(globalReport -> Boolean.TRUE)
        .caseTrue(null)
        .caseFalse(SuccessStepFlow.flowNamed("Case False"))
        .build()
    );
  }

  @Test
  void givenNullCondition_conditionalFlow_shouldNotBuild() {
    assertThatExceptionOfType(FlowBuilderException.class).isThrownBy(() -> ConditionalFlowBuilder
        .defaultBuilder()
        .named("Test")
        .condition(null)
        .caseTrue(SuccessStepFlow.flowNamed("Case True"))
        .caseFalse(SuccessStepFlow.flowNamed("Case False"))
        .build()
    );
  }
}
