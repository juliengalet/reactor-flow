package io.github.juliengalet.reactorflow.flow;

import io.github.juliengalet.reactorflow.builder.SwitchFlowBuilder;
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

final class SwitchFlowTest {
  @Test
  void givenSuccessCaseOneFlow_switchFlow_shouldSuccess() {
    SwitchFlow<FlowContext> switchFlow = SwitchFlowBuilder
        .defaultBuilder()
        .named("Test")
        .switchCondition(globalReport -> "Case 1")
        .switchCase("Case 1", SuccessStepFlow.flowNamed("Case 1"))
        .switchCase("Case 2", SuccessStepFlow.flowNamed("Case 2"))
        .defaultCase(SuccessStepFlow.flowNamed("Default Case"))
        .build();

    StepVerifier
        .create(switchFlow.run(FlowContext.create()))
        .assertNext(TestUtils.assertAndLog(globalReport -> {
          Assertions.assertThat(globalReport.getName()).isEqualTo("Test");
          assertThat(globalReport.getContext().get("Case 1")).isEqualTo("Case 1");
          assertThat(globalReport.getContext().get("Case 2")).isNull();
          assertThat(globalReport.getContext().get("Default Case")).isNull();
          assertThat(globalReport.getStatus()).isEqualTo(Status.SUCCESS);
          Assertions.assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          Assertions.assertThat(globalReport.getAllErrors()).isEmpty();
          Assertions.assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  void givenClonedWithNewNameSuccessCaseOneFlow_switchFlow_shouldSuccess() {
    SwitchFlow<FlowContext> switchFlow = SwitchFlowBuilder
        .defaultBuilder()
        .named("Test")
        .switchCondition(globalReport -> "Case 1")
        .switchCase("Case 1", SuccessStepFlow.flowNamed("Case 1"))
        .switchCase("Case 2", SuccessStepFlow.flowNamed("Case 2"))
        .defaultCase(SuccessStepFlow.flowNamed("Default Case"))
        .build();

    StepVerifier
        .create(switchFlow.cloneFlow("Test copy").run(FlowContext.create()))
        .assertNext(TestUtils.assertAndLog(globalReport -> {
          Assertions.assertThat(globalReport.getName()).isEqualTo("Test copy");
          assertThat(globalReport.getContext().get("Case 1")).isEqualTo("Case 1");
          assertThat(globalReport.getContext().get("Case 2")).isNull();
          assertThat(globalReport.getContext().get("Default Case")).isNull();
          assertThat(globalReport.getStatus()).isEqualTo(Status.SUCCESS);
          Assertions.assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          Assertions.assertThat(globalReport.getAllErrors()).isEmpty();
          Assertions.assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }


  @Test
  void givenClonedSuccessCaseOneFlow_switchFlow_shouldSuccess() {
    SwitchFlow<FlowContext> switchFlow = SwitchFlowBuilder
        .defaultBuilder()
        .named("Test")
        .switchCondition(globalReport -> "Case 1")
        .switchCase("Case 1", SuccessStepFlow.flowNamed("Case 1"))
        .switchCase("Case 2", SuccessStepFlow.flowNamed("Case 2"))
        .defaultCase(SuccessStepFlow.flowNamed("Default Case"))
        .build();

    StepVerifier
        .create(switchFlow.cloneFlow().run(FlowContext.create()))
        .assertNext(TestUtils.assertAndLog(globalReport -> {
          Assertions.assertThat(globalReport.getName()).isEqualTo("Test");
          assertThat(globalReport.getContext().get("Case 1")).isEqualTo("Case 1");
          assertThat(globalReport.getContext().get("Case 2")).isNull();
          assertThat(globalReport.getContext().get("Default Case")).isNull();
          assertThat(globalReport.getStatus()).isEqualTo(Status.SUCCESS);
          Assertions.assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          Assertions.assertThat(globalReport.getAllErrors()).isEmpty();
          Assertions.assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  void givenSuccessCaseOneFlowWithCustomContext_switchFlow_shouldSuccess() {
    SwitchFlow<CustomContext> switchFlow = SwitchFlowBuilder
        .builderForContextOfType(CustomContext.class)
        .named("Test")
        .switchCondition(globalReport -> "Case 1")
        .switchCase("Case 1", SuccessStepFlow.flowNamed("Case 1"))
        .switchCase("Case 2", SuccessStepFlow.flowNamed("Case 2"))
        .defaultCase(SuccessStepFlow.flowNamed("Default Case"))
        .build();

    StepVerifier
        .create(switchFlow.run(new CustomContext()))
        .assertNext(TestUtils.assertAndLog(globalReport -> {
          Assertions.assertThat(globalReport.getContext().customField).isNotNull();
          assertThat(globalReport.getContext().get("Case 1")).isEqualTo("Case 1");
          assertThat(globalReport.getContext().get("Case 2")).isNull();
          assertThat(globalReport.getContext().get("Default Case")).isNull();
          assertThat(globalReport.getStatus()).isEqualTo(Status.SUCCESS);
          Assertions.assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          Assertions.assertThat(globalReport.getAllErrors()).isEmpty();
          Assertions.assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  void givenFailedCaseOneFlow_switchFlow_shouldError() {
    SwitchFlow<FlowContext> switchFlow = SwitchFlowBuilder
        .defaultBuilder()
        .named("Test")
        .switchCondition(globalReport -> "Case 1")
        .switchCase("Case 1", ErrorStepFlow.flowNamed("Case 1"))
        .switchCase("Case 2", SuccessStepFlow.flowNamed("Case 2"))
        .defaultCase(SuccessStepFlow.flowNamed("Default Case"))
        .build();

    StepVerifier
        .create(switchFlow.run(FlowContext.create()))
        .assertNext(TestUtils.assertAndLog(globalReport -> {
          assertThat(globalReport.getContext().get("Case 1")).isNull();
          assertThat(globalReport.getContext().get("Case 2")).isNull();
          assertThat(globalReport.getContext().get("Default Case")).isNull();
          assertThat(globalReport.getStatus()).isEqualTo(Status.ERROR);
          Assertions.assertThat(globalReport.getAllErrors()).hasSize(1);
          Assertions.assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          Assertions.assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  void givenRawFailedCaseOneFlow_switchFlow_shouldError() {
    SwitchFlow<FlowContext> switchFlow = SwitchFlowBuilder
        .defaultBuilder()
        .named("Test")
        .switchCondition(globalReport -> "Case 1")
        .switchCase("Case 1", ErrorRawStepFlow.flowNamed("Case 1"))
        .switchCase("Case 2", SuccessStepFlow.flowNamed("Case 2"))
        .defaultCase(SuccessStepFlow.flowNamed("Default Case"))
        .build();

    StepVerifier
        .create(switchFlow.run(FlowContext.create()))
        .assertNext(TestUtils.assertAndLog(globalReport -> {
          assertThat(globalReport.getContext().get("Case 1")).isNull();
          assertThat(globalReport.getContext().get("Case 2")).isNull();
          assertThat(globalReport.getContext().get("Default Case")).isNull();
          assertThat(globalReport.getStatus()).isEqualTo(Status.ERROR);
          Assertions.assertThat(globalReport.getAllErrors()).hasSize(1);
          Assertions.assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          Assertions.assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  void givenMonoFailedCaseOneFlow_switchFlow_shouldError() {
    SwitchFlow<FlowContext> switchFlow = SwitchFlowBuilder
        .defaultBuilder()
        .named("Test")
        .switchCondition(globalReport -> "Case 1")
        .switchCase("Case 1", ErrorMonoStepFlow.flowNamed("Case 1"))
        .switchCase("Case 2", SuccessStepFlow.flowNamed("Case 2"))
        .defaultCase(SuccessStepFlow.flowNamed("Default Case"))
        .build();

    StepVerifier
        .create(switchFlow.run(FlowContext.create()))
        .assertNext(TestUtils.assertAndLog(globalReport -> {
          assertThat(globalReport.getContext().get("Case 1")).isNull();
          assertThat(globalReport.getContext().get("Case 2")).isNull();
          assertThat(globalReport.getContext().get("Default Case")).isNull();
          assertThat(globalReport.getStatus()).isEqualTo(Status.ERROR);
          Assertions.assertThat(globalReport.getAllErrors()).hasSize(1);
          Assertions.assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          Assertions.assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  void givenWarningCaseOneFlow_switchFlow_shouldWarning() {
    SwitchFlow<FlowContext> switchFlow = SwitchFlowBuilder
        .defaultBuilder()
        .named("Test")
        .switchCondition(globalReport -> "Case 1")
        .switchCase("Case 1", WarningStepFlow.flowNamed("Case 1"))
        .switchCase("Case 2", SuccessStepFlow.flowNamed("Case 2"))
        .defaultCase(SuccessStepFlow.flowNamed("Default Case"))
        .build();

    StepVerifier
        .create(switchFlow.run(FlowContext.create()))
        .assertNext(TestUtils.assertAndLog(globalReport -> {
          assertThat(globalReport.getContext().get("Case 1")).isEqualTo("Case 1");
          assertThat(globalReport.getContext().get("Case 2")).isNull();
          assertThat(globalReport.getContext().get("Default Case")).isNull();
          assertThat(globalReport.getStatus()).isEqualTo(Status.WARNING);
          Assertions.assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          Assertions.assertThat(globalReport.getAllErrors()).isEmpty();
          Assertions.assertThat(globalReport.getAllWarnings()).hasSize(1);
        }))
        .verifyComplete();
  }

  @Test
  void givenSuccessCaseTwoFlow_switchFlow_shouldSuccess() {
    SwitchFlow<FlowContext> switchFlow = SwitchFlowBuilder
        .defaultBuilder()
        .named("Test")
        .switchCondition(globalReport -> "Case 2")
        .switchCase("Case 1", SuccessStepFlow.flowNamed("Case 1"))
        .switchCase("Case 2", SuccessStepFlow.flowNamed("Case 2"))
        .defaultCase(SuccessStepFlow.flowNamed("Default Case"))
        .build();

    StepVerifier
        .create(switchFlow.run(FlowContext.create()))
        .assertNext(TestUtils.assertAndLog(globalReport -> {
          Assertions.assertThat(globalReport.getName()).isEqualTo("Test");
          assertThat(globalReport.getContext().get("Case 2")).isEqualTo("Case 2");
          assertThat(globalReport.getContext().get("Case 1")).isNull();
          assertThat(globalReport.getContext().get("Default Case")).isNull();
          assertThat(globalReport.getStatus()).isEqualTo(Status.SUCCESS);
          Assertions.assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          Assertions.assertThat(globalReport.getAllErrors()).isEmpty();
          Assertions.assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  void givenFailedCaseTwoFlow_switchFlow_shouldError() {
    SwitchFlow<FlowContext> switchFlow = SwitchFlowBuilder
        .defaultBuilder()
        .named("Test")
        .switchCondition(globalReport -> "Case 2")
        .switchCase("Case 1", SuccessStepFlow.flowNamed("Case 1"))
        .switchCase("Case 2", ErrorStepFlow.flowNamed("Case 2"))
        .defaultCase(SuccessStepFlow.flowNamed("Default Case"))
        .build();

    StepVerifier
        .create(switchFlow.run(FlowContext.create()))
        .assertNext(TestUtils.assertAndLog(globalReport -> {
          assertThat(globalReport.getContext().get("Case 1")).isNull();
          assertThat(globalReport.getContext().get("Case 2")).isNull();
          assertThat(globalReport.getContext().get("Default Case")).isNull();
          assertThat(globalReport.getStatus()).isEqualTo(Status.ERROR);
          Assertions.assertThat(globalReport.getAllErrors()).hasSize(1);
          Assertions.assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          Assertions.assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  void givenWarningCaseTwoFlow_switchFlow_shouldWarning() {
    SwitchFlow<FlowContext> switchFlow = SwitchFlowBuilder
        .defaultBuilder()
        .named("Test")
        .switchCondition(globalReport -> "Case 2")
        .switchCase("Case 1", SuccessStepFlow.flowNamed("Case 1"))
        .switchCase("Case 2", WarningStepFlow.flowNamed("Case 2"))
        .defaultCase(SuccessStepFlow.flowNamed("Default Case"))
        .build();

    StepVerifier
        .create(switchFlow.run(FlowContext.create()))
        .assertNext(TestUtils.assertAndLog(globalReport -> {
          assertThat(globalReport.getContext().get("Case 2")).isEqualTo("Case 2");
          assertThat(globalReport.getContext().get("Case 1")).isNull();
          assertThat(globalReport.getContext().get("Default Case")).isNull();
          assertThat(globalReport.getStatus()).isEqualTo(Status.WARNING);
          Assertions.assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          Assertions.assertThat(globalReport.getAllErrors()).isEmpty();
          Assertions.assertThat(globalReport.getAllWarnings()).hasSize(1);
        }))
        .verifyComplete();
  }

  @Test
  void givenSuccessDefaultCaseFlow_switchFlow_shouldSuccess() {
    SwitchFlow<FlowContext> switchFlow = SwitchFlowBuilder
        .defaultBuilder()
        .named("Test")
        .switchCondition(globalReport -> "Case 3")
        .switchCase("Case 1", SuccessStepFlow.flowNamed("Case 1"))
        .switchCase("Case 2", SuccessStepFlow.flowNamed("Case 2"))
        .defaultCase(SuccessStepFlow.flowNamed("Default Case"))
        .build();

    StepVerifier
        .create(switchFlow.run(FlowContext.create()))
        .assertNext(TestUtils.assertAndLog(globalReport -> {
          Assertions.assertThat(globalReport.getName()).isEqualTo("Test");
          assertThat(globalReport.getContext().get("Case 2")).isNull();
          assertThat(globalReport.getContext().get("Case 1")).isNull();
          assertThat(globalReport.getContext().get("Default Case")).isEqualTo("Default Case");
          assertThat(globalReport.getStatus()).isEqualTo(Status.SUCCESS);
          Assertions.assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          Assertions.assertThat(globalReport.getAllErrors()).isEmpty();
          Assertions.assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  void givenFailedDefaultCaseFlow_switchFlow_shouldError() {
    SwitchFlow<FlowContext> switchFlow = SwitchFlowBuilder
        .defaultBuilder()
        .named("Test")
        .switchCondition(globalReport -> "Case 3")
        .switchCase("Case 1", SuccessStepFlow.flowNamed("Case 1"))
        .switchCase("Case 2", SuccessStepFlow.flowNamed("Case 2"))
        .defaultCase(ErrorStepFlow.flowNamed("Default Case"))
        .build();

    StepVerifier
        .create(switchFlow.run(FlowContext.create()))
        .assertNext(TestUtils.assertAndLog(globalReport -> {
          assertThat(globalReport.getContext().get("Case 1")).isNull();
          assertThat(globalReport.getContext().get("Case 2")).isNull();
          assertThat(globalReport.getContext().get("Default Case")).isNull();
          assertThat(globalReport.getStatus()).isEqualTo(Status.ERROR);
          Assertions.assertThat(globalReport.getAllErrors()).hasSize(1);
          Assertions.assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          Assertions.assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  void givenWarningDefaultCaseFlow_switchFlow_shouldWarning() {
    SwitchFlow<FlowContext> switchFlow = SwitchFlowBuilder
        .defaultBuilder()
        .named("Test")
        .switchCondition(globalReport -> "Case 3")
        .switchCase("Case 1", SuccessStepFlow.flowNamed("Case 1"))
        .switchCase("Case 2", SuccessStepFlow.flowNamed("Case 2"))
        .defaultCase(WarningStepFlow.flowNamed("Default Case"))
        .build();

    StepVerifier
        .create(switchFlow.run(FlowContext.create()))
        .assertNext(TestUtils.assertAndLog(globalReport -> {
          assertThat(globalReport.getContext().get("Case 2")).isNull();
          assertThat(globalReport.getContext().get("Case 1")).isNull();
          assertThat(globalReport.getContext().get("Default Case")).isEqualTo("Default Case");
          assertThat(globalReport.getStatus()).isEqualTo(Status.WARNING);
          Assertions.assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          Assertions.assertThat(globalReport.getAllErrors()).isEmpty();
          Assertions.assertThat(globalReport.getAllWarnings()).hasSize(1);
        }))
        .verifyComplete();
  }

  @Test
  void givenErrorInSwitchCondition_switchFlow_shouldWarning() {
    SwitchFlow<FlowContext> switchFlow = SwitchFlowBuilder
        .defaultBuilder()
        .named("Test")
        .switchCondition(globalReport -> {
          throw new RuntimeException("Raw error");
        })
        .switchCase("Case 1", SuccessStepFlow.flowNamed("Case 1"))
        .switchCase("Case 2", WarningStepFlow.flowNamed("Case 2"))
        .defaultCase(SuccessStepFlow.flowNamed("Default Case"))
        .build();

    StepVerifier
        .create(switchFlow.run(FlowContext.create()))
        .assertNext(TestUtils.assertAndLog(globalReport -> {
          assertThat(globalReport.getContext().get("Case 1")).isNull();
          assertThat(globalReport.getContext().get("Case 2")).isNull();
          assertThat(globalReport.getContext().get("Default Case")).isNull();
          assertThat(globalReport.getStatus()).isEqualTo(Status.ERROR);
          Assertions.assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          Assertions.assertThat(globalReport.getAllErrors()).hasSize(1);
          Assertions.assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  void givenNullName_switchFlow_shouldNotBuild() {
    assertThatExceptionOfType(FlowBuilderException.class).isThrownBy(() -> SwitchFlowBuilder
        .defaultBuilder()
        .named(null)
        .switchCondition(globalReport -> "Case 1")
        .switchCase("Case 1", SuccessStepFlow.flowNamed("Case 1"))
        .defaultCase(SuccessStepFlow.flowNamed("Default False"))
        .build()
    );
  }

  @Test
  void givenNullDefaultCase_switchFlow_shouldNotBuild() {
    assertThatExceptionOfType(FlowBuilderException.class).isThrownBy(() -> SwitchFlowBuilder
        .defaultBuilder()
        .named("Test")
        .switchCondition(globalReport -> "Case 1")
        .switchCase("Case 1", SuccessStepFlow.flowNamed("Case 1"))
        .defaultCase(null)
        .build()
    );
  }

  @Test
  void givenNullSwitchCaseKey_switchFlow_shouldNotBuild() {
    assertThatExceptionOfType(FlowBuilderException.class).isThrownBy(() -> SwitchFlowBuilder
        .defaultBuilder()
        .named("Test")
        .switchCondition(globalReport -> "Case 1")
        .switchCase(null, SuccessStepFlow.flowNamed("Case 1"))
        .defaultCase(SuccessStepFlow.flowNamed("Default False"))
        .build()
    );
  }

  @Test
  void givenNullSwitchCaseFlow_switchFlow_shouldNotBuild() {
    assertThatExceptionOfType(FlowBuilderException.class).isThrownBy(() -> SwitchFlowBuilder
        .defaultBuilder()
        .named("Test")
        .switchCondition(globalReport -> "Case 1")
        .switchCase("Case 1", null)
        .defaultCase(SuccessStepFlow.flowNamed("Default False"))
        .build()
    );
  }

  @Test
  void givenNullSwitchCondition_switchFlow_shouldNotBuild() {
    assertThatExceptionOfType(FlowBuilderException.class).isThrownBy(() -> SwitchFlowBuilder
        .defaultBuilder()
        .named("Test")
        .switchCondition(null)
        .switchCase("Case 1", SuccessStepFlow.flowNamed("Case 1"))
        .defaultCase(SuccessStepFlow.flowNamed("Default False"))
        .build()
    );
  }
}
