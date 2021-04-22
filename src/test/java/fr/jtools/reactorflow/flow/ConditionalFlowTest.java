package fr.jtools.reactorflow.flow;

import fr.jtools.reactorflow.builder.ConditionalFlowBuilder;
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

final class ConditionalFlowTest {
  @Test
  final void givenSuccessCaseTrueFlow_conditionalFlow_shouldSuccess() {
    ConditionalFlow<FlowContext> conditionalFlow = ConditionalFlowBuilder
        .defaultBuilder()
        .named("Test")
        .condition(globalReport -> Boolean.TRUE)
        .caseTrue(SuccessStepFlow.flowNamed("Case True"))
        .caseFalse(SuccessStepFlow.flowNamed("Case False"))
        .build();

    StepVerifier
        .create(conditionalFlow.run(FlowContext.create()))
        .assertNext(assertAndLog(globalReport -> {
          assertThat(globalReport.getName()).isEqualTo("Test");
          assertThat(globalReport.getContext().get("Case True")).isEqualTo("Case True");
          assertThat(globalReport.getContext().get("Case False")).isNull();
          assertThat(globalReport.getStatus()).isEqualTo(Status.SUCCESS);
          assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          assertThat(globalReport.getAllErrors()).isEmpty();
          assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  final void givenClonedWithNewNameSuccessCaseTrueFlow_conditionalFlow_shouldSuccess() {
    ConditionalFlow<FlowContext> conditionalFlow = ConditionalFlowBuilder
        .defaultBuilder()
        .named("Test")
        .condition(globalReport -> Boolean.TRUE)
        .caseTrue(SuccessStepFlow.flowNamed("Case True"))
        .caseFalse(SuccessStepFlow.flowNamed("Case False"))
        .build();

    StepVerifier
        .create(conditionalFlow.cloneFlow("Test copy").run(FlowContext.create()))
        .assertNext(assertAndLog(globalReport -> {
          assertThat(globalReport.getName()).isEqualTo("Test copy");
          assertThat(globalReport.getContext().get("Case True")).isEqualTo("Case True");
          assertThat(globalReport.getContext().get("Case False")).isNull();
          assertThat(globalReport.getStatus()).isEqualTo(Status.SUCCESS);
          assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          assertThat(globalReport.getAllErrors()).isEmpty();
          assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }


  @Test
  final void givenClonedSuccessCaseTrueFlow_conditionalFlow_shouldSuccess() {
    ConditionalFlow<FlowContext> conditionalFlow = ConditionalFlowBuilder
        .defaultBuilder()
        .named("Test")
        .condition(globalReport -> Boolean.TRUE)
        .caseTrue(SuccessStepFlow.flowNamed("Case True"))
        .caseFalse(SuccessStepFlow.flowNamed("Case False"))
        .build();

    StepVerifier
        .create(conditionalFlow.cloneFlow().run(FlowContext.create()))
        .assertNext(assertAndLog(globalReport -> {
          assertThat(globalReport.getName()).isEqualTo("Test");
          assertThat(globalReport.getContext().get("Case True")).isEqualTo("Case True");
          assertThat(globalReport.getContext().get("Case False")).isNull();
          assertThat(globalReport.getStatus()).isEqualTo(Status.SUCCESS);
          assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          assertThat(globalReport.getAllErrors()).isEmpty();
          assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  final void givenSuccessCaseTrueFlowWithCustomContext_conditionalFlow_shouldSuccess() {
    ConditionalFlow<CustomContext> conditionalFlow = ConditionalFlowBuilder
        .builderForContextOfType(CustomContext.class)
        .named("Test")
        .condition(globalReport -> Boolean.TRUE)
        .caseTrue(SuccessStepFlow.flowNamed("Case True"))
        .caseFalse(SuccessStepFlow.flowNamed("Case False"))
        .build();

    StepVerifier
        .create(conditionalFlow.run(new CustomContext()))
        .assertNext(assertAndLog(globalReport -> {
          assertThat(globalReport.getContext().customField).isNotNull();
          assertThat(globalReport.getContext().get("Case True")).isEqualTo("Case True");
          assertThat(globalReport.getContext().get("Case False")).isNull();
          assertThat(globalReport.getStatus()).isEqualTo(Status.SUCCESS);
          assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          assertThat(globalReport.getAllErrors()).isEmpty();
          assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  final void givenFailedCaseTrueFlow_conditionalFlow_shouldError() {
    ConditionalFlow<FlowContext> conditionalFlow = ConditionalFlowBuilder
        .defaultBuilder()
        .named("Test")
        .condition(globalReport -> Boolean.TRUE)
        .caseTrue(ErrorStepFlow.flowNamed("Case True"))
        .caseFalse(SuccessStepFlow.flowNamed("Case False"))
        .build();

    StepVerifier
        .create(conditionalFlow.run(FlowContext.create()))
        .assertNext(assertAndLog(globalReport -> {
          assertThat(globalReport.getContext().get("Case True")).isNull();
          assertThat(globalReport.getContext().get("Case False")).isNull();
          assertThat(globalReport.getStatus()).isEqualTo(Status.ERROR);
          assertThat(globalReport.getAllErrors()).hasSize(1);
          assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  final void givenRawFailedCaseTrueFlow_conditionalFlow_shouldError() {
    ConditionalFlow<FlowContext> conditionalFlow = ConditionalFlowBuilder
        .defaultBuilder()
        .named("Test")
        .condition(globalReport -> Boolean.TRUE)
        .caseTrue(ErrorRawStepFlow.flowNamed("Case True"))
        .caseFalse(SuccessStepFlow.flowNamed("Case False"))
        .build();

    StepVerifier
        .create(conditionalFlow.run(FlowContext.create()))
        .assertNext(assertAndLog(globalReport -> {
          assertThat(globalReport.getContext().get("Case True")).isNull();
          assertThat(globalReport.getContext().get("Case False")).isNull();
          assertThat(globalReport.getStatus()).isEqualTo(Status.ERROR);
          assertThat(globalReport.getAllErrors()).hasSize(1);
          assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  final void givenMonoFailedCaseTrueFlow_conditionalFlow_shouldError() {
    ConditionalFlow<FlowContext> conditionalFlow = ConditionalFlowBuilder
        .defaultBuilder()
        .named("Test")
        .condition(globalReport -> Boolean.TRUE)
        .caseTrue(ErrorMonoStepFlow.flowNamed("Case True"))
        .caseFalse(SuccessStepFlow.flowNamed("Case False"))
        .build();

    StepVerifier
        .create(conditionalFlow.run(FlowContext.create()))
        .assertNext(assertAndLog(globalReport -> {
          assertThat(globalReport.getContext().get("Case True")).isNull();
          assertThat(globalReport.getContext().get("Case False")).isNull();
          assertThat(globalReport.getStatus()).isEqualTo(Status.ERROR);
          assertThat(globalReport.getAllErrors()).hasSize(1);
          assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  final void givenWarningCaseTrueFlow_conditionalFlow_shouldWarning() {
    ConditionalFlow<FlowContext> conditionalFlow = ConditionalFlowBuilder
        .defaultBuilder()
        .named("Test")
        .condition(globalReport -> Boolean.TRUE)
        .caseTrue(WarningStepFlow.flowNamed("Case True"))
        .caseFalse(SuccessStepFlow.flowNamed("Case False"))
        .build();

    StepVerifier
        .create(conditionalFlow.run(FlowContext.create()))
        .assertNext(assertAndLog(globalReport -> {
          assertThat(globalReport.getContext().get("Case True")).isEqualTo("Case True");
          assertThat(globalReport.getContext().get("Case False")).isNull();
          assertThat(globalReport.getStatus()).isEqualTo(Status.WARNING);
          assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          assertThat(globalReport.getAllErrors()).isEmpty();
          assertThat(globalReport.getAllWarnings()).hasSize(1);
        }))
        .verifyComplete();
  }

  @Test
  final void givenSuccessCaseFalseFlow_conditionalFlow_shouldSuccess() {
    ConditionalFlow<FlowContext> conditionalFlow = ConditionalFlowBuilder
        .defaultBuilder()
        .named("Test")
        .condition(globalReport -> Boolean.FALSE)
        .caseTrue(SuccessStepFlow.flowNamed("Case True"))
        .caseFalse(SuccessStepFlow.flowNamed("Case False"))
        .build();

    StepVerifier
        .create(conditionalFlow.run(FlowContext.create()))
        .assertNext(assertAndLog(globalReport -> {
          assertThat(globalReport.getName()).isEqualTo("Test");
          assertThat(globalReport.getContext().get("Case False")).isEqualTo("Case False");
          assertThat(globalReport.getContext().get("Case True")).isNull();
          assertThat(globalReport.getStatus()).isEqualTo(Status.SUCCESS);
          assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          assertThat(globalReport.getAllErrors()).isEmpty();
          assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  final void givenFailedCaseFalseFlow_conditionalFlow_shouldError() {
    ConditionalFlow<FlowContext> conditionalFlow = ConditionalFlowBuilder
        .defaultBuilder()
        .named("Test")
        .condition(globalReport -> Boolean.FALSE)
        .caseTrue(SuccessStepFlow.flowNamed("Case True"))
        .caseFalse(ErrorStepFlow.flowNamed("Case False"))
        .build();

    StepVerifier
        .create(conditionalFlow.run(FlowContext.create()))
        .assertNext(assertAndLog(globalReport -> {
          assertThat(globalReport.getContext().get("Case True")).isNull();
          assertThat(globalReport.getContext().get("Case False")).isNull();
          assertThat(globalReport.getStatus()).isEqualTo(Status.ERROR);
          assertThat(globalReport.getAllErrors()).hasSize(1);
          assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  final void givenWarningCaseFalseFlow_conditionalFlow_shouldWarning() {
    ConditionalFlow<FlowContext> conditionalFlow = ConditionalFlowBuilder
        .defaultBuilder()
        .named("Test")
        .condition(globalReport -> Boolean.FALSE)
        .caseTrue(SuccessStepFlow.flowNamed("Case True"))
        .caseFalse(WarningStepFlow.flowNamed("Case False"))
        .build();

    StepVerifier
        .create(conditionalFlow.run(FlowContext.create()))
        .assertNext(assertAndLog(globalReport -> {
          assertThat(globalReport.getContext().get("Case False")).isEqualTo("Case False");
          assertThat(globalReport.getContext().get("Case True")).isNull();
          assertThat(globalReport.getStatus()).isEqualTo(Status.WARNING);
          assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          assertThat(globalReport.getAllErrors()).isEmpty();
          assertThat(globalReport.getAllWarnings()).hasSize(1);
        }))
        .verifyComplete();
  }

  @Test
  final void givenErrorInCondition_conditionalFlow_shouldWarning() {
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
        .assertNext(assertAndLog(globalReport -> {
          assertThat(globalReport.getContext().get("Case False")).isNull();
          assertThat(globalReport.getContext().get("Case True")).isNull();
          assertThat(globalReport.getStatus()).isEqualTo(Status.ERROR);
          assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          assertThat(globalReport.getAllErrors()).hasSize(1);
          assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  final void givenNullName_conditionalFlow_shouldNotBuild() {
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
  final void givenNullCaseFalse_conditionalFlow_shouldNotBuild() {
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
  final void givenNullCaseTrue_conditionalFlow_shouldNotBuild() {
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
