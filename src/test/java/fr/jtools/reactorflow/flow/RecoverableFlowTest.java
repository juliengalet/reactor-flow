package fr.jtools.reactorflow.flow;

import fr.jtools.reactorflow.builder.RecoverableFlowBuilder;
import fr.jtools.reactorflow.exception.FlowBuilderException;
import fr.jtools.reactorflow.exception.RecoverableFlowException;
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

final class RecoverableFlowTest {
  @Test
  final void givenSuccessTryFlow_recoverableFlow_shouldSuccess() {
    RecoverableFlow<FlowContext> recoverableFlow = RecoverableFlowBuilder
        .defaultBuilder()
        .named("Test")
        .tryFlow(SuccessStepFlow.flowNamed("Tried"))
        .recover(SuccessStepFlow.flowNamed("Recover"))
        .recoverOn(RecoverableFlowException.ALL)
        .build();

    StepVerifier
        .create(recoverableFlow.run(FlowContext.create()))
        .assertNext(assertAndLog(globalReport -> {
          assertThat(globalReport.getName()).isEqualTo("Test");
          assertThat(globalReport.getContext().get("Tried")).isEqualTo("Tried");
          assertThat(globalReport.getContext().get("Recover")).isNull();
          assertThat(globalReport.getStatus()).isEqualTo(Status.SUCCESS);
          assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          assertThat(globalReport.getAllErrors()).isEmpty();
          assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  final void givenClonedWithNewNameSuccessTryFlow_recoverableFlow_shouldSuccess() {
    RecoverableFlow<FlowContext> recoverableFlow = RecoverableFlowBuilder
        .defaultBuilder()
        .named("Test")
        .tryFlow(SuccessStepFlow.flowNamed("Tried"))
        .recover(SuccessStepFlow.flowNamed("Recover"))
        .recoverOn(RecoverableFlowException.ALL)
        .build();

    StepVerifier
        .create(recoverableFlow.cloneFlow("Test copy").run(FlowContext.create()))
        .assertNext(assertAndLog(globalReport -> {
          assertThat(globalReport.getName()).isEqualTo("Test copy");
          assertThat(globalReport.getContext().get("Tried")).isEqualTo("Tried");
          assertThat(globalReport.getContext().get("Recover")).isNull();
          assertThat(globalReport.getStatus()).isEqualTo(Status.SUCCESS);
          assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          assertThat(globalReport.getAllErrors()).isEmpty();
          assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }


  @Test
  final void givenClonedSuccessTryFlow_recoverableFlow_shouldSuccess() {
    RecoverableFlow<FlowContext> recoverableFlow = RecoverableFlowBuilder
        .defaultBuilder()
        .named("Test")
        .tryFlow(SuccessStepFlow.flowNamed("Tried"))
        .recover(SuccessStepFlow.flowNamed("Recover"))
        .recoverOn(RecoverableFlowException.ALL)
        .build();

    StepVerifier
        .create(recoverableFlow.cloneFlow().run(FlowContext.create()))
        .assertNext(assertAndLog(globalReport -> {
          assertThat(globalReport.getName()).isEqualTo("Test");
          assertThat(globalReport.getContext().get("Tried")).isEqualTo("Tried");
          assertThat(globalReport.getContext().get("Recover")).isNull();
          assertThat(globalReport.getStatus()).isEqualTo(Status.SUCCESS);
          assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          assertThat(globalReport.getAllErrors()).isEmpty();
          assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  final void givenSuccessTryFlowWithCustomContext_recoverableFlow_shouldSuccess() {
    RecoverableFlow<CustomContext> recoverableFlow = RecoverableFlowBuilder
        .builderForContextOfType(CustomContext.class)
        .named("Test")
        .tryFlow(SuccessStepFlow.flowNamed("Tried"))
        .recover(SuccessStepFlow.flowNamed("Recover"))
        .recoverOn(RecoverableFlowException.ALL)
        .build();

    StepVerifier
        .create(recoverableFlow.run(new CustomContext()))
        .assertNext(assertAndLog(globalReport -> {
          assertThat(globalReport.getContext().customField).isNotNull();
          assertThat(globalReport.getContext().get("Tried")).isEqualTo("Tried");
          assertThat(globalReport.getContext().get("Recover")).isNull();
          assertThat(globalReport.getStatus()).isEqualTo(Status.SUCCESS);
          assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          assertThat(globalReport.getAllErrors()).isEmpty();
          assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  final void givenFailedTryFlowAndRecoverMatching_recoverableFlow_shouldSuccess() {
    RecoverableFlow<FlowContext> recoverableFlow = RecoverableFlowBuilder
        .defaultBuilder()
        .named("Test")
        .tryFlow(ErrorStepFlow.flowNamed("Tried"))
        .recover(SuccessStepFlow.flowNamed("Recover"))
        .recoverOn(RecoverableFlowException.ALL)
        .build();

    StepVerifier
        .create(recoverableFlow.run(FlowContext.create()))
        .assertNext(assertAndLog(globalReport -> {
          assertThat(globalReport.getContext().get("Tried")).isNull();
          assertThat(globalReport.getContext().get("Recover")).isEqualTo("Recover");
          assertThat(globalReport.getStatus()).isEqualTo(Status.SUCCESS);
          assertThat(globalReport.getAllRecoveredErrors()).hasSize(1);
          assertThat(globalReport.getAllErrors()).isEmpty();
          assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  final void givenRawFailedTryFlowAndRecoverMatching_recoverableFlow_shouldSuccess() {
    RecoverableFlow<FlowContext> recoverableFlow = RecoverableFlowBuilder
        .defaultBuilder()
        .named("Test")
        .tryFlow(ErrorRawStepFlow.flowNamed("Tried"))
        .recover(SuccessStepFlow.flowNamed("Recover"))
        .recoverOn(RecoverableFlowException.ALL)
        .build();

    StepVerifier
        .create(recoverableFlow.run(FlowContext.create()))
        .assertNext(assertAndLog(globalReport -> {
          assertThat(globalReport.getContext().get("Tried")).isNull();
          assertThat(globalReport.getContext().get("Recover")).isEqualTo("Recover");
          assertThat(globalReport.getStatus()).isEqualTo(Status.SUCCESS);
          assertThat(globalReport.getAllRecoveredErrors()).hasSize(1);
          assertThat(globalReport.getAllErrors()).isEmpty();
          assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  final void givenMonoFailedTryFlowAndRecoverMatching_recoverableFlow_shouldSuccess() {
    RecoverableFlow<FlowContext> recoverableFlow = RecoverableFlowBuilder
        .defaultBuilder()
        .named("Test")
        .tryFlow(ErrorMonoStepFlow.flowNamed("Tried"))
        .recover(SuccessStepFlow.flowNamed("Recover"))
        .recoverOn(RecoverableFlowException.ALL)
        .build();

    StepVerifier
        .create(recoverableFlow.run(FlowContext.create()))
        .assertNext(assertAndLog(globalReport -> {
          assertThat(globalReport.getContext().get("Tried")).isNull();
          assertThat(globalReport.getContext().get("Recover")).isEqualTo("Recover");
          assertThat(globalReport.getStatus()).isEqualTo(Status.SUCCESS);
          assertThat(globalReport.getAllRecoveredErrors()).hasSize(1);
          assertThat(globalReport.getAllErrors()).isEmpty();
          assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  final void givenFailedTryFlowAndRecoverMatchingWithWarning_recoverableFlow_shouldWarning() {
    RecoverableFlow<FlowContext> recoverableFlow = RecoverableFlowBuilder
        .defaultBuilder()
        .named("Test")
        .tryFlow(ErrorStepFlow.flowNamed("Tried"))
        .recover(WarningStepFlow.flowNamed("Recover"))
        .recoverOn(RecoverableFlowException.ALL)
        .build();

    StepVerifier
        .create(recoverableFlow.run(FlowContext.create()))
        .assertNext(assertAndLog(globalReport -> {
          assertThat(globalReport.getContext().get("Tried")).isNull();
          assertThat(globalReport.getContext().get("Recover")).isEqualTo("Recover");
          assertThat(globalReport.getStatus()).isEqualTo(Status.WARNING);
          assertThat(globalReport.getAllRecoveredErrors()).hasSize(1);
          assertThat(globalReport.getAllErrors()).isEmpty();
          assertThat(globalReport.getAllWarnings()).hasSize(1);
        }))
        .verifyComplete();
  }

  @Test
  final void givenFailedTryFlowAndRecoverNotMatching_recoverableFlow_shouldError() {
    RecoverableFlow<FlowContext> recoverableFlow = RecoverableFlowBuilder
        .defaultBuilder()
        .named("Test")
        .tryFlow(ErrorStepFlow.flowNamed("Tried"))
        .recover(SuccessStepFlow.flowNamed("Recover"))
        .recoverOn(RecoverableFlowException.FUNCTIONAL)
        .build();

    StepVerifier
        .create(recoverableFlow.run(FlowContext.create()))
        .assertNext(assertAndLog(globalReport -> {
          assertThat(globalReport.getContext().get("Tried")).isNull();
          assertThat(globalReport.getContext().get("Recover")).isNull();
          assertThat(globalReport.getStatus()).isEqualTo(Status.ERROR);
          assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          assertThat(globalReport.getAllErrors()).hasSize(1);
          assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  final void givenNullName_recoverableFlow_shouldNotBuild() {
    assertThatExceptionOfType(FlowBuilderException.class).isThrownBy(() -> RecoverableFlowBuilder
        .defaultBuilder()
        .named(null)
        .tryFlow(SuccessStepFlow.flowNamed("Tried"))
        .recover(SuccessStepFlow.flowNamed("Recover"))
        .recoverOn(RecoverableFlowException.ALL)
        .build()
    );
  }

  @Test
  final void givenNullTryFlow_recoverableFlow_shouldNotBuild() {
    assertThatExceptionOfType(FlowBuilderException.class).isThrownBy(() -> RecoverableFlowBuilder
        .defaultBuilder()
        .named("Test")
        .tryFlow(null)
        .recover(SuccessStepFlow.flowNamed("Recover"))
        .recoverOn(RecoverableFlowException.ALL)
        .build()
    );
  }

  @Test
  final void givenNullRecover_recoverableFlow_shouldNotBuild() {
    assertThatExceptionOfType(FlowBuilderException.class).isThrownBy(() -> RecoverableFlowBuilder
        .defaultBuilder()
        .named("Test")
        .tryFlow(SuccessStepFlow.flowNamed("Tried"))
        .recover(null)
        .recoverOn(RecoverableFlowException.ALL)
        .build()
    );
  }

  @Test
  final void givenNullRecoverOn_recoverableFlow_shouldNotBuild() {
    assertThatExceptionOfType(FlowBuilderException.class).isThrownBy(() -> RecoverableFlowBuilder
        .defaultBuilder()
        .named("Test")
        .tryFlow(SuccessStepFlow.flowNamed("Tried"))
        .recover(SuccessStepFlow.flowNamed("Recover"))
        .recoverOn(null)
        .build()
    );
  }
}
