package fr.jtools.reactorflow;

import fr.jtools.reactorflow.builder.ConditionalFlowBuilder;
import fr.jtools.reactorflow.exception.FlowBuilderException;
import fr.jtools.reactorflow.flow.ConditionalFlow;
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

final class ConditionalFlowTest {
  @Test
  final void givenSuccessCaseTrueFlow_conditionalFlow_shouldSuccess() {
    ConditionalFlow<FlowContext> conditionalFlow = ConditionalFlowBuilder
        .defaultBuilder()
        .named("Test")
        .condition(state -> Boolean.TRUE)
        .caseTrue(SuccessStepFlow.flowNamed("Case True"))
        .caseFalse(SuccessStepFlow.flowNamed("Case False"))
        .build();

    StepVerifier
        .create(conditionalFlow.run(FlowContext.create()))
        .assertNext(assertAndLog(state -> {
          assertThat(state.getName()).isEqualTo("Test");
          assertThat(state.getContext().get("Case True")).isEqualTo("Case True");
          assertThat(state.getContext().get("Case False")).isNull();
          assertThat(state.getStatus()).isEqualTo(State.Status.SUCCESS);
          assertThat(state.getAllRecoveredErrors()).isEmpty();
          assertThat(state.getAllErrors()).isEmpty();
          assertThat(state.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  final void givenClonedWithNewNameSuccessCaseTrueFlow_conditionalFlow_shouldSuccess() {
    ConditionalFlow<FlowContext> conditionalFlow = ConditionalFlowBuilder
        .defaultBuilder()
        .named("Test")
        .condition(state -> Boolean.TRUE)
        .caseTrue(SuccessStepFlow.flowNamed("Case True"))
        .caseFalse(SuccessStepFlow.flowNamed("Case False"))
        .build();

    StepVerifier
        .create(conditionalFlow.cloneFlow("Test copy").run(FlowContext.create()))
        .assertNext(assertAndLog(state -> {
          assertThat(state.getName()).isEqualTo("Test copy");
          assertThat(state.getContext().get("Case True")).isEqualTo("Case True");
          assertThat(state.getContext().get("Case False")).isNull();
          assertThat(state.getStatus()).isEqualTo(State.Status.SUCCESS);
          assertThat(state.getAllRecoveredErrors()).isEmpty();
          assertThat(state.getAllErrors()).isEmpty();
          assertThat(state.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }


  @Test
  final void givenClonedSuccessCaseTrueFlow_conditionalFlow_shouldSuccess() {
    ConditionalFlow<FlowContext> conditionalFlow = ConditionalFlowBuilder
        .defaultBuilder()
        .named("Test")
        .condition(state -> Boolean.TRUE)
        .caseTrue(SuccessStepFlow.flowNamed("Case True"))
        .caseFalse(SuccessStepFlow.flowNamed("Case False"))
        .build();

    StepVerifier
        .create(conditionalFlow.cloneFlow().run(FlowContext.create()))
        .assertNext(assertAndLog(state -> {
          assertThat(state.getName()).isEqualTo("Test");
          assertThat(state.getContext().get("Case True")).isEqualTo("Case True");
          assertThat(state.getContext().get("Case False")).isNull();
          assertThat(state.getStatus()).isEqualTo(State.Status.SUCCESS);
          assertThat(state.getAllRecoveredErrors()).isEmpty();
          assertThat(state.getAllErrors()).isEmpty();
          assertThat(state.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  final void givenSuccessCaseTrueFlowWithCustomContext_conditionalFlow_shouldSuccess() {
    ConditionalFlow<CustomContext> conditionalFlow = ConditionalFlowBuilder
        .builderForContextOfType(CustomContext.class)
        .named("Test")
        .condition(state -> Boolean.TRUE)
        .caseTrue(SuccessStepFlow.flowNamed("Case True"))
        .caseFalse(SuccessStepFlow.flowNamed("Case False"))
        .build();

    StepVerifier
        .create(conditionalFlow.run(new CustomContext()))
        .assertNext(assertAndLog(state -> {
          assertThat(state.getContext().customField).isNotNull();
          assertThat(state.getContext().get("Case True")).isEqualTo("Case True");
          assertThat(state.getContext().get("Case False")).isNull();
          assertThat(state.getStatus()).isEqualTo(State.Status.SUCCESS);
          assertThat(state.getAllRecoveredErrors()).isEmpty();
          assertThat(state.getAllErrors()).isEmpty();
          assertThat(state.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  final void givenFailedCaseTrueFlow_conditionalFlow_shouldError() {
    ConditionalFlow<FlowContext> conditionalFlow = ConditionalFlowBuilder
        .defaultBuilder()
        .named("Test")
        .condition(state -> Boolean.TRUE)
        .caseTrue(ErrorStepFlow.flowNamed("Case True"))
        .caseFalse(SuccessStepFlow.flowNamed("Case False"))
        .build();

    StepVerifier
        .create(conditionalFlow.run(FlowContext.create()))
        .assertNext(assertAndLog(state -> {
          assertThat(state.getContext().get("Case True")).isNull();
          assertThat(state.getContext().get("Case False")).isNull();
          assertThat(state.getStatus()).isEqualTo(State.Status.ERROR);
          assertThat(state.getAllErrors()).hasSize(1);
          assertThat(state.getAllRecoveredErrors()).isEmpty();
          assertThat(state.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  final void givenRawFailedCaseTrueFlow_conditionalFlow_shouldError() {
    ConditionalFlow<FlowContext> conditionalFlow = ConditionalFlowBuilder
        .defaultBuilder()
        .named("Test")
        .condition(state -> Boolean.TRUE)
        .caseTrue(ErrorRawStepFlow.flowNamed("Case True"))
        .caseFalse(SuccessStepFlow.flowNamed("Case False"))
        .build();

    StepVerifier
        .create(conditionalFlow.run(FlowContext.create()))
        .assertNext(assertAndLog(state -> {
          assertThat(state.getContext().get("Case True")).isNull();
          assertThat(state.getContext().get("Case False")).isNull();
          assertThat(state.getStatus()).isEqualTo(State.Status.ERROR);
          assertThat(state.getAllErrors()).hasSize(1);
          assertThat(state.getAllRecoveredErrors()).isEmpty();
          assertThat(state.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  final void givenMonoFailedCaseTrueFlow_conditionalFlow_shouldError() {
    ConditionalFlow<FlowContext> conditionalFlow = ConditionalFlowBuilder
        .defaultBuilder()
        .named("Test")
        .condition(state -> Boolean.TRUE)
        .caseTrue(ErrorMonoStepFlow.flowNamed("Case True"))
        .caseFalse(SuccessStepFlow.flowNamed("Case False"))
        .build();

    StepVerifier
        .create(conditionalFlow.run(FlowContext.create()))
        .assertNext(assertAndLog(state -> {
          assertThat(state.getContext().get("Case True")).isNull();
          assertThat(state.getContext().get("Case False")).isNull();
          assertThat(state.getStatus()).isEqualTo(State.Status.ERROR);
          assertThat(state.getAllErrors()).hasSize(1);
          assertThat(state.getAllRecoveredErrors()).isEmpty();
          assertThat(state.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  final void givenWarningCaseTrueFlow_conditionalFlow_shouldWarning() {
    ConditionalFlow<FlowContext> conditionalFlow = ConditionalFlowBuilder
        .defaultBuilder()
        .named("Test")
        .condition(state -> Boolean.TRUE)
        .caseTrue(WarningStepFlow.flowNamed("Case True"))
        .caseFalse(SuccessStepFlow.flowNamed("Case False"))
        .build();

    StepVerifier
        .create(conditionalFlow.run(FlowContext.create()))
        .assertNext(assertAndLog(state -> {
          assertThat(state.getContext().get("Case True")).isEqualTo("Case True");
          assertThat(state.getContext().get("Case False")).isNull();
          assertThat(state.getStatus()).isEqualTo(State.Status.WARNING);
          assertThat(state.getAllRecoveredErrors()).isEmpty();
          assertThat(state.getAllErrors()).isEmpty();
          assertThat(state.getAllWarnings()).hasSize(1);
        }))
        .verifyComplete();
  }

  @Test
  final void givenSuccessCaseFalseFlow_conditionalFlow_shouldSuccess() {
    ConditionalFlow<FlowContext> conditionalFlow = ConditionalFlowBuilder
        .defaultBuilder()
        .named("Test")
        .condition(state -> Boolean.FALSE)
        .caseTrue(SuccessStepFlow.flowNamed("Case True"))
        .caseFalse(SuccessStepFlow.flowNamed("Case False"))
        .build();

    StepVerifier
        .create(conditionalFlow.run(FlowContext.create()))
        .assertNext(assertAndLog(state -> {
          assertThat(state.getName()).isEqualTo("Test");
          assertThat(state.getContext().get("Case False")).isEqualTo("Case False");
          assertThat(state.getContext().get("Case True")).isNull();
          assertThat(state.getStatus()).isEqualTo(State.Status.SUCCESS);
          assertThat(state.getAllRecoveredErrors()).isEmpty();
          assertThat(state.getAllErrors()).isEmpty();
          assertThat(state.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  final void givenFailedCaseFalseFlow_conditionalFlow_shouldError() {
    ConditionalFlow<FlowContext> conditionalFlow = ConditionalFlowBuilder
        .defaultBuilder()
        .named("Test")
        .condition(state -> Boolean.FALSE)
        .caseTrue(SuccessStepFlow.flowNamed("Case True"))
        .caseFalse(ErrorStepFlow.flowNamed("Case False"))
        .build();

    StepVerifier
        .create(conditionalFlow.run(FlowContext.create()))
        .assertNext(assertAndLog(state -> {
          assertThat(state.getContext().get("Case True")).isNull();
          assertThat(state.getContext().get("Case False")).isNull();
          assertThat(state.getStatus()).isEqualTo(State.Status.ERROR);
          assertThat(state.getAllErrors()).hasSize(1);
          assertThat(state.getAllRecoveredErrors()).isEmpty();
          assertThat(state.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  final void givenWarningCaseFalseFlow_conditionalFlow_shouldWarning() {
    ConditionalFlow<FlowContext> conditionalFlow = ConditionalFlowBuilder
        .defaultBuilder()
        .named("Test")
        .condition(state -> Boolean.FALSE)
        .caseTrue(SuccessStepFlow.flowNamed("Case True"))
        .caseFalse(WarningStepFlow.flowNamed("Case False"))
        .build();

    StepVerifier
        .create(conditionalFlow.run(FlowContext.create()))
        .assertNext(assertAndLog(state -> {
          assertThat(state.getContext().get("Case False")).isEqualTo("Case False");
          assertThat(state.getContext().get("Case True")).isNull();
          assertThat(state.getStatus()).isEqualTo(State.Status.WARNING);
          assertThat(state.getAllRecoveredErrors()).isEmpty();
          assertThat(state.getAllErrors()).isEmpty();
          assertThat(state.getAllWarnings()).hasSize(1);
        }))
        .verifyComplete();
  }

  @Test
  final void givenErrorInCondition_conditionalFlow_shouldWarning() {
    ConditionalFlow<FlowContext> conditionalFlow = ConditionalFlowBuilder
        .defaultBuilder()
        .named("Test")
        .condition(state -> {
          throw new RuntimeException("Raw error");
        })
        .caseTrue(SuccessStepFlow.flowNamed("Case True"))
        .caseFalse(SuccessStepFlow.flowNamed("Case False"))
        .build();

    StepVerifier
        .create(conditionalFlow.run(FlowContext.create()))
        .assertNext(assertAndLog(state -> {
          assertThat(state.getContext().get("Case False")).isNull();
          assertThat(state.getContext().get("Case True")).isNull();
          assertThat(state.getStatus()).isEqualTo(State.Status.ERROR);
          assertThat(state.getAllRecoveredErrors()).isEmpty();
          assertThat(state.getAllErrors()).hasSize(1);
          assertThat(state.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  final void givenNullName_conditionalFlow_shouldNotBuild() {
    assertThatExceptionOfType(FlowBuilderException.class).isThrownBy(() -> ConditionalFlowBuilder
        .defaultBuilder()
        .named(null)
        .condition(state -> Boolean.TRUE)
        .caseTrue(SuccessStepFlow.flowNamed("Case True"))
        .caseFalse(SuccessStepFlow.flowNamed("Case False"))
        .build()
    );
  }

  @Test
  final void givenNullCaseFalse_conditionalFlow_shouldNotBuild() {
    assertThatExceptionOfType(FlowBuilderException.class).isThrownBy(() -> ConditionalFlowBuilder
        .defaultBuilder()
        .named("Test")
        .condition(state -> Boolean.TRUE)
        .caseTrue(SuccessStepFlow.flowNamed("Case True"))
        .caseFalse(null)
        .build()
    );
  }

  @Test
  final void givenNullCaseTrue_conditionalFlow_shouldNotBuild() {
    assertThatExceptionOfType(FlowBuilderException.class).isThrownBy(() -> ConditionalFlowBuilder
        .defaultBuilder()
        .named("Test")
        .condition(state -> Boolean.TRUE)
        .caseTrue(null)
        .caseFalse(SuccessStepFlow.flowNamed("Case False"))
        .build()
    );
  }

  @Test
  final void givenNullCondition_conditionalFlow_shouldNotBuild() {
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
