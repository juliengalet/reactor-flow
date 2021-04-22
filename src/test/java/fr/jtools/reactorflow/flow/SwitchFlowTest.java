package fr.jtools.reactorflow.flow;

import fr.jtools.reactorflow.builder.SwitchFlowBuilder;
import fr.jtools.reactorflow.exception.FlowBuilderException;
import fr.jtools.reactorflow.report.FlowContext;
import fr.jtools.reactorflow.report.Status;
import fr.jtools.reactorflow.testutils.CustomContext;
import fr.jtools.reactorflow.testutils.ErrorMonoStepFlow;
import fr.jtools.reactorflow.testutils.ErrorRawStepFlow;
import fr.jtools.reactorflow.testutils.ErrorStepFlow;
import fr.jtools.reactorflow.testutils.SuccessStepFlow;
import fr.jtools.reactorflow.testutils.WarningStepFlow;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import static fr.jtools.reactorflow.testutils.TestUtils.assertAndLog;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

final class SwitchFlowTest {
  @Test
  final void givenSuccessCaseOneFlow_switchFlow_shouldSuccess() {
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
        .assertNext(assertAndLog(globalReport -> {
          assertThat(globalReport.getName()).isEqualTo("Test");
          assertThat(globalReport.getContext().get("Case 1")).isEqualTo("Case 1");
          assertThat(globalReport.getContext().get("Case 2")).isNull();
          assertThat(globalReport.getContext().get("Default Case")).isNull();
          assertThat(globalReport.getStatus()).isEqualTo(Status.SUCCESS);
          assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          assertThat(globalReport.getAllErrors()).isEmpty();
          assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  final void givenClonedWithNewNameSuccessCaseOneFlow_switchFlow_shouldSuccess() {
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
        .assertNext(assertAndLog(globalReport -> {
          assertThat(globalReport.getName()).isEqualTo("Test copy");
          assertThat(globalReport.getContext().get("Case 1")).isEqualTo("Case 1");
          assertThat(globalReport.getContext().get("Case 2")).isNull();
          assertThat(globalReport.getContext().get("Default Case")).isNull();
          assertThat(globalReport.getStatus()).isEqualTo(Status.SUCCESS);
          assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          assertThat(globalReport.getAllErrors()).isEmpty();
          assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }


  @Test
  final void givenClonedSuccessCaseOneFlow_switchFlow_shouldSuccess() {
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
        .assertNext(assertAndLog(globalReport -> {
          assertThat(globalReport.getName()).isEqualTo("Test");
          assertThat(globalReport.getContext().get("Case 1")).isEqualTo("Case 1");
          assertThat(globalReport.getContext().get("Case 2")).isNull();
          assertThat(globalReport.getContext().get("Default Case")).isNull();
          assertThat(globalReport.getStatus()).isEqualTo(Status.SUCCESS);
          assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          assertThat(globalReport.getAllErrors()).isEmpty();
          assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  final void givenSuccessCaseOneFlowWithCustomContext_switchFlow_shouldSuccess() {
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
        .assertNext(assertAndLog(globalReport -> {
          assertThat(globalReport.getContext().customField).isNotNull();
          assertThat(globalReport.getContext().get("Case 1")).isEqualTo("Case 1");
          assertThat(globalReport.getContext().get("Case 2")).isNull();
          assertThat(globalReport.getContext().get("Default Case")).isNull();
          assertThat(globalReport.getStatus()).isEqualTo(Status.SUCCESS);
          assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          assertThat(globalReport.getAllErrors()).isEmpty();
          assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  final void givenFailedCaseOneFlow_switchFlow_shouldError() {
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
        .assertNext(assertAndLog(globalReport -> {
          assertThat(globalReport.getContext().get("Case 1")).isNull();
          assertThat(globalReport.getContext().get("Case 2")).isNull();
          assertThat(globalReport.getContext().get("Default Case")).isNull();
          assertThat(globalReport.getStatus()).isEqualTo(Status.ERROR);
          assertThat(globalReport.getAllErrors()).hasSize(1);
          assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  final void givenRawFailedCaseOneFlow_switchFlow_shouldError() {
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
        .assertNext(assertAndLog(globalReport -> {
          assertThat(globalReport.getContext().get("Case 1")).isNull();
          assertThat(globalReport.getContext().get("Case 2")).isNull();
          assertThat(globalReport.getContext().get("Default Case")).isNull();
          assertThat(globalReport.getStatus()).isEqualTo(Status.ERROR);
          assertThat(globalReport.getAllErrors()).hasSize(1);
          assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  final void givenMonoFailedCaseOneFlow_switchFlow_shouldError() {
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
        .assertNext(assertAndLog(globalReport -> {
          assertThat(globalReport.getContext().get("Case 1")).isNull();
          assertThat(globalReport.getContext().get("Case 2")).isNull();
          assertThat(globalReport.getContext().get("Default Case")).isNull();
          assertThat(globalReport.getStatus()).isEqualTo(Status.ERROR);
          assertThat(globalReport.getAllErrors()).hasSize(1);
          assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  final void givenWarningCaseOneFlow_switchFlow_shouldWarning() {
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
        .assertNext(assertAndLog(globalReport -> {
          assertThat(globalReport.getContext().get("Case 1")).isEqualTo("Case 1");
          assertThat(globalReport.getContext().get("Case 2")).isNull();
          assertThat(globalReport.getContext().get("Default Case")).isNull();
          assertThat(globalReport.getStatus()).isEqualTo(Status.WARNING);
          assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          assertThat(globalReport.getAllErrors()).isEmpty();
          assertThat(globalReport.getAllWarnings()).hasSize(1);
        }))
        .verifyComplete();
  }

  @Test
  final void givenSuccessCaseTwoFlow_switchFlow_shouldSuccess() {
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
        .assertNext(assertAndLog(globalReport -> {
          assertThat(globalReport.getName()).isEqualTo("Test");
          assertThat(globalReport.getContext().get("Case 2")).isEqualTo("Case 2");
          assertThat(globalReport.getContext().get("Case 1")).isNull();
          assertThat(globalReport.getContext().get("Default Case")).isNull();
          assertThat(globalReport.getStatus()).isEqualTo(Status.SUCCESS);
          assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          assertThat(globalReport.getAllErrors()).isEmpty();
          assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  final void givenFailedCaseTwoFlow_switchFlow_shouldError() {
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
        .assertNext(assertAndLog(globalReport -> {
          assertThat(globalReport.getContext().get("Case 1")).isNull();
          assertThat(globalReport.getContext().get("Case 2")).isNull();
          assertThat(globalReport.getContext().get("Default Case")).isNull();
          assertThat(globalReport.getStatus()).isEqualTo(Status.ERROR);
          assertThat(globalReport.getAllErrors()).hasSize(1);
          assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  final void givenWarningCaseTwoFlow_switchFlow_shouldWarning() {
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
        .assertNext(assertAndLog(globalReport -> {
          assertThat(globalReport.getContext().get("Case 2")).isEqualTo("Case 2");
          assertThat(globalReport.getContext().get("Case 1")).isNull();
          assertThat(globalReport.getContext().get("Default Case")).isNull();
          assertThat(globalReport.getStatus()).isEqualTo(Status.WARNING);
          assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          assertThat(globalReport.getAllErrors()).isEmpty();
          assertThat(globalReport.getAllWarnings()).hasSize(1);
        }))
        .verifyComplete();
  }

  @Test
  final void givenSuccessDefaultCaseFlow_switchFlow_shouldSuccess() {
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
        .assertNext(assertAndLog(globalReport -> {
          assertThat(globalReport.getName()).isEqualTo("Test");
          assertThat(globalReport.getContext().get("Case 2")).isNull();
          assertThat(globalReport.getContext().get("Case 1")).isNull();
          assertThat(globalReport.getContext().get("Default Case")).isEqualTo("Default Case");
          assertThat(globalReport.getStatus()).isEqualTo(Status.SUCCESS);
          assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          assertThat(globalReport.getAllErrors()).isEmpty();
          assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  final void givenFailedDefaultCaseFlow_switchFlow_shouldError() {
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
        .assertNext(assertAndLog(globalReport -> {
          assertThat(globalReport.getContext().get("Case 1")).isNull();
          assertThat(globalReport.getContext().get("Case 2")).isNull();
          assertThat(globalReport.getContext().get("Default Case")).isNull();
          assertThat(globalReport.getStatus()).isEqualTo(Status.ERROR);
          assertThat(globalReport.getAllErrors()).hasSize(1);
          assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  final void givenWarningDefaultCaseFlow_switchFlow_shouldWarning() {
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
        .assertNext(assertAndLog(globalReport -> {
          assertThat(globalReport.getContext().get("Case 2")).isNull();
          assertThat(globalReport.getContext().get("Case 1")).isNull();
          assertThat(globalReport.getContext().get("Default Case")).isEqualTo("Default Case");
          assertThat(globalReport.getStatus()).isEqualTo(Status.WARNING);
          assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          assertThat(globalReport.getAllErrors()).isEmpty();
          assertThat(globalReport.getAllWarnings()).hasSize(1);
        }))
        .verifyComplete();
  }

  @Test
  final void givenErrorInSwitchCondition_switchFlow_shouldWarning() {
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
        .assertNext(assertAndLog(globalReport -> {
          assertThat(globalReport.getContext().get("Case 1")).isNull();
          assertThat(globalReport.getContext().get("Case 2")).isNull();
          assertThat(globalReport.getContext().get("Default Case")).isNull();
          assertThat(globalReport.getStatus()).isEqualTo(Status.ERROR);
          assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          assertThat(globalReport.getAllErrors()).hasSize(1);
          assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  final void givenNullName_switchFlow_shouldNotBuild() {
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
  final void givenNullDefaultCase_switchFlow_shouldNotBuild() {
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
  final void givenNullSwitchCaseKey_switchFlow_shouldNotBuild() {
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
  final void givenNullSwitchCaseFlow_switchFlow_shouldNotBuild() {
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
  final void givenNullSwitchCondition_switchFlow_shouldNotBuild() {
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
