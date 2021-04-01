package fr.jtools.reactorflow;

import fr.jtools.reactorflow.builder.SwitchFlowBuilder;
import fr.jtools.reactorflow.exception.FlowBuilderException;
import fr.jtools.reactorflow.flow.SwitchFlow;
import fr.jtools.reactorflow.state.FlowContext;
import fr.jtools.reactorflow.state.State;
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
        .switchCondition(state -> "Case 1")
        .switchCase("Case 1", SuccessStepFlow.flowNamed("Case 1"))
        .switchCase("Case 2", SuccessStepFlow.flowNamed("Case 2"))
        .defaultCase(SuccessStepFlow.flowNamed("Default Case"))
        .build();

    StepVerifier
        .create(switchFlow.run(FlowContext.create()))
        .assertNext(assertAndLog(state -> {
          assertThat(state.getName()).isEqualTo("Test");
          assertThat(state.getContext().get("Case 1")).isEqualTo("Case 1");
          assertThat(state.getContext().get("Case 2")).isNull();
          assertThat(state.getContext().get("Default Case")).isNull();
          assertThat(state.getStatus()).isEqualTo(State.Status.SUCCESS);
          assertThat(state.getAllRecoveredErrors()).isEmpty();
          assertThat(state.getAllErrors()).isEmpty();
          assertThat(state.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  final void givenClonedWithNewNameSuccessCaseOneFlow_switchFlow_shouldSuccess() {
    SwitchFlow<FlowContext> switchFlow = SwitchFlowBuilder
        .defaultBuilder()
        .named("Test")
        .switchCondition(state -> "Case 1")
        .switchCase("Case 1", SuccessStepFlow.flowNamed("Case 1"))
        .switchCase("Case 2", SuccessStepFlow.flowNamed("Case 2"))
        .defaultCase(SuccessStepFlow.flowNamed("Default Case"))
        .build();

    StepVerifier
        .create(switchFlow.cloneFlow("Test copy").run(FlowContext.create()))
        .assertNext(assertAndLog(state -> {
          assertThat(state.getName()).isEqualTo("Test copy");
          assertThat(state.getContext().get("Case 1")).isEqualTo("Case 1");
          assertThat(state.getContext().get("Case 2")).isNull();
          assertThat(state.getContext().get("Default Case")).isNull();
          assertThat(state.getStatus()).isEqualTo(State.Status.SUCCESS);
          assertThat(state.getAllRecoveredErrors()).isEmpty();
          assertThat(state.getAllErrors()).isEmpty();
          assertThat(state.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }


  @Test
  final void givenClonedSuccessCaseOneFlow_switchFlow_shouldSuccess() {
    SwitchFlow<FlowContext> switchFlow = SwitchFlowBuilder
        .defaultBuilder()
        .named("Test")
        .switchCondition(state -> "Case 1")
        .switchCase("Case 1", SuccessStepFlow.flowNamed("Case 1"))
        .switchCase("Case 2", SuccessStepFlow.flowNamed("Case 2"))
        .defaultCase(SuccessStepFlow.flowNamed("Default Case"))
        .build();

    StepVerifier
        .create(switchFlow.cloneFlow().run(FlowContext.create()))
        .assertNext(assertAndLog(state -> {
          assertThat(state.getName()).isEqualTo("Test");
          assertThat(state.getContext().get("Case 1")).isEqualTo("Case 1");
          assertThat(state.getContext().get("Case 2")).isNull();
          assertThat(state.getContext().get("Default Case")).isNull();
          assertThat(state.getStatus()).isEqualTo(State.Status.SUCCESS);
          assertThat(state.getAllRecoveredErrors()).isEmpty();
          assertThat(state.getAllErrors()).isEmpty();
          assertThat(state.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  final void givenSuccessCaseOneFlowWithCustomContext_switchFlow_shouldSuccess() {
    SwitchFlow<CustomContext> switchFlow = SwitchFlowBuilder
        .builderForContextOfType(CustomContext.class)
        .named("Test")
        .switchCondition(state -> "Case 1")
        .switchCase("Case 1", SuccessStepFlow.flowNamed("Case 1"))
        .switchCase("Case 2", SuccessStepFlow.flowNamed("Case 2"))
        .defaultCase(SuccessStepFlow.flowNamed("Default Case"))
        .build();

    StepVerifier
        .create(switchFlow.run(new CustomContext()))
        .assertNext(assertAndLog(state -> {
          assertThat(state.getContext().customField).isNotNull();
          assertThat(state.getContext().get("Case 1")).isEqualTo("Case 1");
          assertThat(state.getContext().get("Case 2")).isNull();
          assertThat(state.getContext().get("Default Case")).isNull();
          assertThat(state.getStatus()).isEqualTo(State.Status.SUCCESS);
          assertThat(state.getAllRecoveredErrors()).isEmpty();
          assertThat(state.getAllErrors()).isEmpty();
          assertThat(state.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  final void givenFailedCaseOneFlow_switchFlow_shouldError() {
    SwitchFlow<FlowContext> switchFlow = SwitchFlowBuilder
        .defaultBuilder()
        .named("Test")
        .switchCondition(state -> "Case 1")
        .switchCase("Case 1", ErrorStepFlow.flowNamed("Case 1"))
        .switchCase("Case 2", SuccessStepFlow.flowNamed("Case 2"))
        .defaultCase(SuccessStepFlow.flowNamed("Default Case"))
        .build();

    StepVerifier
        .create(switchFlow.run(FlowContext.create()))
        .assertNext(assertAndLog(state -> {
          assertThat(state.getContext().get("Case 1")).isNull();
          assertThat(state.getContext().get("Case 2")).isNull();
          assertThat(state.getContext().get("Default Case")).isNull();
          assertThat(state.getStatus()).isEqualTo(State.Status.ERROR);
          assertThat(state.getAllErrors()).hasSize(1);
          assertThat(state.getAllRecoveredErrors()).isEmpty();
          assertThat(state.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  final void givenRawFailedCaseOneFlow_switchFlow_shouldError() {
    SwitchFlow<FlowContext> switchFlow = SwitchFlowBuilder
        .defaultBuilder()
        .named("Test")
        .switchCondition(state -> "Case 1")
        .switchCase("Case 1", ErrorRawStepFlow.flowNamed("Case 1"))
        .switchCase("Case 2", SuccessStepFlow.flowNamed("Case 2"))
        .defaultCase(SuccessStepFlow.flowNamed("Default Case"))
        .build();

    StepVerifier
        .create(switchFlow.run(FlowContext.create()))
        .assertNext(assertAndLog(state -> {
          assertThat(state.getContext().get("Case 1")).isNull();
          assertThat(state.getContext().get("Case 2")).isNull();
          assertThat(state.getContext().get("Default Case")).isNull();
          assertThat(state.getStatus()).isEqualTo(State.Status.ERROR);
          assertThat(state.getAllErrors()).hasSize(1);
          assertThat(state.getAllRecoveredErrors()).isEmpty();
          assertThat(state.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  final void givenMonoFailedCaseOneFlow_switchFlow_shouldError() {
    SwitchFlow<FlowContext> switchFlow = SwitchFlowBuilder
        .defaultBuilder()
        .named("Test")
        .switchCondition(state -> "Case 1")
        .switchCase("Case 1", ErrorMonoStepFlow.flowNamed("Case 1"))
        .switchCase("Case 2", SuccessStepFlow.flowNamed("Case 2"))
        .defaultCase(SuccessStepFlow.flowNamed("Default Case"))
        .build();

    StepVerifier
        .create(switchFlow.run(FlowContext.create()))
        .assertNext(assertAndLog(state -> {
          assertThat(state.getContext().get("Case 1")).isNull();
          assertThat(state.getContext().get("Case 2")).isNull();
          assertThat(state.getContext().get("Default Case")).isNull();
          assertThat(state.getStatus()).isEqualTo(State.Status.ERROR);
          assertThat(state.getAllErrors()).hasSize(1);
          assertThat(state.getAllRecoveredErrors()).isEmpty();
          assertThat(state.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  final void givenWarningCaseOneFlow_switchFlow_shouldWarning() {
    SwitchFlow<FlowContext> switchFlow = SwitchFlowBuilder
        .defaultBuilder()
        .named("Test")
        .switchCondition(state -> "Case 1")
        .switchCase("Case 1", WarningStepFlow.flowNamed("Case 1"))
        .switchCase("Case 2", SuccessStepFlow.flowNamed("Case 2"))
        .defaultCase(SuccessStepFlow.flowNamed("Default Case"))
        .build();

    StepVerifier
        .create(switchFlow.run(FlowContext.create()))
        .assertNext(assertAndLog(state -> {
          assertThat(state.getContext().get("Case 1")).isEqualTo("Case 1");
          assertThat(state.getContext().get("Case 2")).isNull();
          assertThat(state.getContext().get("Default Case")).isNull();
          assertThat(state.getStatus()).isEqualTo(State.Status.WARNING);
          assertThat(state.getAllRecoveredErrors()).isEmpty();
          assertThat(state.getAllErrors()).isEmpty();
          assertThat(state.getAllWarnings()).hasSize(1);
        }))
        .verifyComplete();
  }

  @Test
  final void givenSuccessCaseTwoFlow_switchFlow_shouldSuccess() {
    SwitchFlow<FlowContext> switchFlow = SwitchFlowBuilder
        .defaultBuilder()
        .named("Test")
        .switchCondition(state -> "Case 2")
        .switchCase("Case 1", SuccessStepFlow.flowNamed("Case 1"))
        .switchCase("Case 2", SuccessStepFlow.flowNamed("Case 2"))
        .defaultCase(SuccessStepFlow.flowNamed("Default Case"))
        .build();

    StepVerifier
        .create(switchFlow.run(FlowContext.create()))
        .assertNext(assertAndLog(state -> {
          assertThat(state.getName()).isEqualTo("Test");
          assertThat(state.getContext().get("Case 2")).isEqualTo("Case 2");
          assertThat(state.getContext().get("Case 1")).isNull();
          assertThat(state.getContext().get("Default Case")).isNull();
          assertThat(state.getStatus()).isEqualTo(State.Status.SUCCESS);
          assertThat(state.getAllRecoveredErrors()).isEmpty();
          assertThat(state.getAllErrors()).isEmpty();
          assertThat(state.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  final void givenFailedCaseTwoFlow_switchFlow_shouldError() {
    SwitchFlow<FlowContext> switchFlow = SwitchFlowBuilder
        .defaultBuilder()
        .named("Test")
        .switchCondition(state -> "Case 2")
        .switchCase("Case 1", SuccessStepFlow.flowNamed("Case 1"))
        .switchCase("Case 2", ErrorStepFlow.flowNamed("Case 2"))
        .defaultCase(SuccessStepFlow.flowNamed("Default Case"))
        .build();

    StepVerifier
        .create(switchFlow.run(FlowContext.create()))
        .assertNext(assertAndLog(state -> {
          assertThat(state.getContext().get("Case 1")).isNull();
          assertThat(state.getContext().get("Case 2")).isNull();
          assertThat(state.getContext().get("Default Case")).isNull();
          assertThat(state.getStatus()).isEqualTo(State.Status.ERROR);
          assertThat(state.getAllErrors()).hasSize(1);
          assertThat(state.getAllRecoveredErrors()).isEmpty();
          assertThat(state.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  final void givenWarningCaseTwoFlow_switchFlow_shouldWarning() {
    SwitchFlow<FlowContext> switchFlow = SwitchFlowBuilder
        .defaultBuilder()
        .named("Test")
        .switchCondition(state -> "Case 2")
        .switchCase("Case 1", SuccessStepFlow.flowNamed("Case 1"))
        .switchCase("Case 2", WarningStepFlow.flowNamed("Case 2"))
        .defaultCase(SuccessStepFlow.flowNamed("Default Case"))
        .build();

    StepVerifier
        .create(switchFlow.run(FlowContext.create()))
        .assertNext(assertAndLog(state -> {
          assertThat(state.getContext().get("Case 2")).isEqualTo("Case 2");
          assertThat(state.getContext().get("Case 1")).isNull();
          assertThat(state.getContext().get("Default Case")).isNull();
          assertThat(state.getStatus()).isEqualTo(State.Status.WARNING);
          assertThat(state.getAllRecoveredErrors()).isEmpty();
          assertThat(state.getAllErrors()).isEmpty();
          assertThat(state.getAllWarnings()).hasSize(1);
        }))
        .verifyComplete();
  }

  @Test
  final void givenSuccessDefaultCaseFlow_switchFlow_shouldSuccess() {
    SwitchFlow<FlowContext> switchFlow = SwitchFlowBuilder
        .defaultBuilder()
        .named("Test")
        .switchCondition(state -> "Case 3")
        .switchCase("Case 1", SuccessStepFlow.flowNamed("Case 1"))
        .switchCase("Case 2", SuccessStepFlow.flowNamed("Case 2"))
        .defaultCase(SuccessStepFlow.flowNamed("Default Case"))
        .build();

    StepVerifier
        .create(switchFlow.run(FlowContext.create()))
        .assertNext(assertAndLog(state -> {
          assertThat(state.getName()).isEqualTo("Test");
          assertThat(state.getContext().get("Case 2")).isNull();
          assertThat(state.getContext().get("Case 1")).isNull();
          assertThat(state.getContext().get("Default Case")).isEqualTo("Default Case");
          assertThat(state.getStatus()).isEqualTo(State.Status.SUCCESS);
          assertThat(state.getAllRecoveredErrors()).isEmpty();
          assertThat(state.getAllErrors()).isEmpty();
          assertThat(state.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  final void givenFailedDefaultCaseFlow_switchFlow_shouldError() {
    SwitchFlow<FlowContext> switchFlow = SwitchFlowBuilder
        .defaultBuilder()
        .named("Test")
        .switchCondition(state -> "Case 3")
        .switchCase("Case 1", SuccessStepFlow.flowNamed("Case 1"))
        .switchCase("Case 2", SuccessStepFlow.flowNamed("Case 2"))
        .defaultCase(ErrorStepFlow.flowNamed("Default Case"))
        .build();

    StepVerifier
        .create(switchFlow.run(FlowContext.create()))
        .assertNext(assertAndLog(state -> {
          assertThat(state.getContext().get("Case 1")).isNull();
          assertThat(state.getContext().get("Case 2")).isNull();
          assertThat(state.getContext().get("Default Case")).isNull();
          assertThat(state.getStatus()).isEqualTo(State.Status.ERROR);
          assertThat(state.getAllErrors()).hasSize(1);
          assertThat(state.getAllRecoveredErrors()).isEmpty();
          assertThat(state.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  final void givenWarningDefaultCaseFlow_switchFlow_shouldWarning() {
    SwitchFlow<FlowContext> switchFlow = SwitchFlowBuilder
        .defaultBuilder()
        .named("Test")
        .switchCondition(state -> "Case 3")
        .switchCase("Case 1", SuccessStepFlow.flowNamed("Case 1"))
        .switchCase("Case 2", SuccessStepFlow.flowNamed("Case 2"))
        .defaultCase(WarningStepFlow.flowNamed("Default Case"))
        .build();

    StepVerifier
        .create(switchFlow.run(FlowContext.create()))
        .assertNext(assertAndLog(state -> {
          assertThat(state.getContext().get("Case 2")).isNull();
          assertThat(state.getContext().get("Case 1")).isNull();
          assertThat(state.getContext().get("Default Case")).isEqualTo("Default Case");
          assertThat(state.getStatus()).isEqualTo(State.Status.WARNING);
          assertThat(state.getAllRecoveredErrors()).isEmpty();
          assertThat(state.getAllErrors()).isEmpty();
          assertThat(state.getAllWarnings()).hasSize(1);
        }))
        .verifyComplete();
  }

  @Test
  final void givenErrorInSwitchCondition_switchFlow_shouldWarning() {
    SwitchFlow<FlowContext> switchFlow = SwitchFlowBuilder
        .defaultBuilder()
        .named("Test")
        .switchCondition(state -> {
          throw new RuntimeException("Raw error");
        })
        .switchCase("Case 1", SuccessStepFlow.flowNamed("Case 1"))
        .switchCase("Case 2", WarningStepFlow.flowNamed("Case 2"))
        .defaultCase(SuccessStepFlow.flowNamed("Default Case"))
        .build();

    StepVerifier
        .create(switchFlow.run(FlowContext.create()))
        .assertNext(assertAndLog(state -> {
          assertThat(state.getContext().get("Case 1")).isNull();
          assertThat(state.getContext().get("Case 2")).isNull();
          assertThat(state.getContext().get("Default Case")).isNull();
          assertThat(state.getStatus()).isEqualTo(State.Status.ERROR);
          assertThat(state.getAllRecoveredErrors()).isEmpty();
          assertThat(state.getAllErrors()).hasSize(1);
          assertThat(state.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  final void givenNullName_switchFlow_shouldNotBuild() {
    assertThatExceptionOfType(FlowBuilderException.class).isThrownBy(() -> SwitchFlowBuilder
        .defaultBuilder()
        .named(null)
        .switchCondition(state -> "Case 1")
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
        .switchCondition(state -> "Case 1")
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
        .switchCondition(state -> "Case 1")
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
        .switchCondition(state -> "Case 1")
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
