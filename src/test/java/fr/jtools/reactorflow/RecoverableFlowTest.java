package fr.jtools.reactorflow;

import fr.jtools.reactorflow.builder.RecoverableFlowBuilder;
import fr.jtools.reactorflow.exception.FlowBuilderException;
import fr.jtools.reactorflow.exception.RecoverableFlowException;
import fr.jtools.reactorflow.flow.RecoverableFlow;
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
        .assertNext(assertAndLog(state -> {
          assertThat(state.getName()).isEqualTo("Test");
          assertThat(state.getContext().get("Tried")).isEqualTo("Tried");
          assertThat(state.getContext().get("Recover")).isNull();
          assertThat(state.getStatus()).isEqualTo(State.Status.SUCCESS);
          assertThat(state.getAllRecoveredErrors()).isEmpty();
          assertThat(state.getAllErrors()).isEmpty();
          assertThat(state.getAllWarnings()).isEmpty();
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
        .assertNext(assertAndLog(state -> {
          assertThat(state.getName()).isEqualTo("Test copy");
          assertThat(state.getContext().get("Tried")).isEqualTo("Tried");
          assertThat(state.getContext().get("Recover")).isNull();
          assertThat(state.getStatus()).isEqualTo(State.Status.SUCCESS);
          assertThat(state.getAllRecoveredErrors()).isEmpty();
          assertThat(state.getAllErrors()).isEmpty();
          assertThat(state.getAllWarnings()).isEmpty();
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
        .assertNext(assertAndLog(state -> {
          assertThat(state.getName()).isEqualTo("Test");
          assertThat(state.getContext().get("Tried")).isEqualTo("Tried");
          assertThat(state.getContext().get("Recover")).isNull();
          assertThat(state.getStatus()).isEqualTo(State.Status.SUCCESS);
          assertThat(state.getAllRecoveredErrors()).isEmpty();
          assertThat(state.getAllErrors()).isEmpty();
          assertThat(state.getAllWarnings()).isEmpty();
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
        .assertNext(assertAndLog(state -> {
          assertThat(state.getContext().customField).isNotNull();
          assertThat(state.getContext().get("Tried")).isEqualTo("Tried");
          assertThat(state.getContext().get("Recover")).isNull();
          assertThat(state.getStatus()).isEqualTo(State.Status.SUCCESS);
          assertThat(state.getAllRecoveredErrors()).isEmpty();
          assertThat(state.getAllErrors()).isEmpty();
          assertThat(state.getAllWarnings()).isEmpty();
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
        .assertNext(assertAndLog(state -> {
          assertThat(state.getContext().get("Tried")).isNull();
          assertThat(state.getContext().get("Recover")).isEqualTo("Recover");
          assertThat(state.getStatus()).isEqualTo(State.Status.SUCCESS);
          assertThat(state.getAllRecoveredErrors()).hasSize(1);
          assertThat(state.getAllErrors()).isEmpty();
          assertThat(state.getAllWarnings()).isEmpty();
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
        .assertNext(assertAndLog(state -> {
          assertThat(state.getContext().get("Tried")).isNull();
          assertThat(state.getContext().get("Recover")).isEqualTo("Recover");
          assertThat(state.getStatus()).isEqualTo(State.Status.SUCCESS);
          assertThat(state.getAllRecoveredErrors()).hasSize(1);
          assertThat(state.getAllErrors()).isEmpty();
          assertThat(state.getAllWarnings()).isEmpty();
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
        .assertNext(assertAndLog(state -> {
          assertThat(state.getContext().get("Tried")).isNull();
          assertThat(state.getContext().get("Recover")).isEqualTo("Recover");
          assertThat(state.getStatus()).isEqualTo(State.Status.SUCCESS);
          assertThat(state.getAllRecoveredErrors()).hasSize(1);
          assertThat(state.getAllErrors()).isEmpty();
          assertThat(state.getAllWarnings()).isEmpty();
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
        .assertNext(assertAndLog(state -> {
          assertThat(state.getContext().get("Tried")).isNull();
          assertThat(state.getContext().get("Recover")).isEqualTo("Recover");
          assertThat(state.getStatus()).isEqualTo(State.Status.WARNING);
          assertThat(state.getAllRecoveredErrors()).hasSize(1);
          assertThat(state.getAllErrors()).isEmpty();
          assertThat(state.getAllWarnings()).hasSize(1);
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
        .assertNext(assertAndLog(state -> {
          assertThat(state.getContext().get("Tried")).isNull();
          assertThat(state.getContext().get("Recover")).isNull();
          assertThat(state.getStatus()).isEqualTo(State.Status.ERROR);
          assertThat(state.getAllRecoveredErrors()).isEmpty();
          assertThat(state.getAllErrors()).hasSize(1);
          assertThat(state.getAllWarnings()).isEmpty();
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
