package io.github.juliengalet.reactorflow.flow;

import io.github.juliengalet.reactorflow.builder.RecoverableFlowBuilder;
import io.github.juliengalet.reactorflow.exception.FlowBuilderException;
import io.github.juliengalet.reactorflow.exception.RecoverableFlowException;
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

final class RecoverableFlowTest {
  @Test
  void givenSuccessTryFlow_recoverableFlow_shouldSuccess() {
    RecoverableFlow<FlowContext> recoverableFlow = RecoverableFlowBuilder
        .defaultBuilder()
        .named("Test")
        .tryFlow(SuccessStepFlow.flowNamed("Tried"))
        .recover(SuccessStepFlow.flowNamed("Recover"))
        .recoverOn(RecoverableFlowException.ALL)
        .build();

    StepVerifier
        .create(recoverableFlow.run(FlowContext.create()))
        .assertNext(TestUtils.assertAndLog(globalReport -> {
          Assertions.assertThat(globalReport.getName()).isEqualTo("Test");
          Assertions.assertThat(globalReport.getContext().get("Tried")).isEqualTo("Tried");
          Assertions.assertThat(globalReport.getContext().get("Recover")).isNull();
          assertThat(globalReport.getStatus()).isEqualTo(Status.SUCCESS);
          Assertions.assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          Assertions.assertThat(globalReport.getAllErrors()).isEmpty();
          Assertions.assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  void givenClonedWithNewNameSuccessTryFlow_recoverableFlow_shouldSuccess() {
    RecoverableFlow<FlowContext> recoverableFlow = RecoverableFlowBuilder
        .defaultBuilder()
        .named("Test")
        .tryFlow(SuccessStepFlow.flowNamed("Tried"))
        .recover(SuccessStepFlow.flowNamed("Recover"))
        .recoverOn(RecoverableFlowException.ALL)
        .build();

    StepVerifier
        .create(recoverableFlow.cloneFlow("Test copy").run(FlowContext.create()))
        .assertNext(TestUtils.assertAndLog(globalReport -> {
          Assertions.assertThat(globalReport.getName()).isEqualTo("Test copy");
          Assertions.assertThat(globalReport.getContext().get("Tried")).isEqualTo("Tried");
          Assertions.assertThat(globalReport.getContext().get("Recover")).isNull();
          assertThat(globalReport.getStatus()).isEqualTo(Status.SUCCESS);
          Assertions.assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          Assertions.assertThat(globalReport.getAllErrors()).isEmpty();
          Assertions.assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }


  @Test
  void givenClonedSuccessTryFlow_recoverableFlow_shouldSuccess() {
    RecoverableFlow<FlowContext> recoverableFlow = RecoverableFlowBuilder
        .defaultBuilder()
        .named("Test")
        .tryFlow(SuccessStepFlow.flowNamed("Tried"))
        .recover(SuccessStepFlow.flowNamed("Recover"))
        .recoverOn(RecoverableFlowException.ALL)
        .build();

    StepVerifier
        .create(recoverableFlow.cloneFlow().run(FlowContext.create()))
        .assertNext(TestUtils.assertAndLog(globalReport -> {
          Assertions.assertThat(globalReport.getName()).isEqualTo("Test");
          Assertions.assertThat(globalReport.getContext().get("Tried")).isEqualTo("Tried");
          Assertions.assertThat(globalReport.getContext().get("Recover")).isNull();
          assertThat(globalReport.getStatus()).isEqualTo(Status.SUCCESS);
          Assertions.assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          Assertions.assertThat(globalReport.getAllErrors()).isEmpty();
          Assertions.assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  void givenSuccessTryFlowWithCustomContext_recoverableFlow_shouldSuccess() {
    RecoverableFlow<CustomContext> recoverableFlow = RecoverableFlowBuilder
        .builderForContextOfType(CustomContext.class)
        .named("Test")
        .tryFlow(SuccessStepFlow.flowNamed("Tried"))
        .recover(SuccessStepFlow.flowNamed("Recover"))
        .recoverOn(RecoverableFlowException.ALL)
        .build();

    StepVerifier
        .create(recoverableFlow.run(new CustomContext()))
        .assertNext(TestUtils.assertAndLog(globalReport -> {
          assertThat(globalReport.getContext().customField).isNotNull();
          Assertions.assertThat(globalReport.getContext().get("Tried")).isEqualTo("Tried");
          Assertions.assertThat(globalReport.getContext().get("Recover")).isNull();
          assertThat(globalReport.getStatus()).isEqualTo(Status.SUCCESS);
          Assertions.assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          Assertions.assertThat(globalReport.getAllErrors()).isEmpty();
          Assertions.assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  void givenFailedTryFlowAndRecoverMatching_recoverableFlow_shouldSuccess() {
    RecoverableFlow<FlowContext> recoverableFlow = RecoverableFlowBuilder
        .defaultBuilder()
        .named("Test")
        .tryFlow(ErrorStepFlow.flowNamed("Tried"))
        .recover(SuccessStepFlow.flowNamed("Recover"))
        .recoverOn(RecoverableFlowException.ALL)
        .build();

    StepVerifier
        .create(recoverableFlow.run(FlowContext.create()))
        .assertNext(TestUtils.assertAndLog(globalReport -> {
          Assertions.assertThat(globalReport.getContext().get("Tried")).isNull();
          Assertions.assertThat(globalReport.getContext().get("Recover")).isEqualTo("Recover");
          assertThat(globalReport.getStatus()).isEqualTo(Status.SUCCESS);
          Assertions.assertThat(globalReport.getAllRecoveredErrors()).hasSize(1);
          Assertions.assertThat(globalReport.getAllErrors()).isEmpty();
          Assertions.assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  void givenRawFailedTryFlowAndRecoverMatching_recoverableFlow_shouldSuccess() {
    RecoverableFlow<FlowContext> recoverableFlow = RecoverableFlowBuilder
        .defaultBuilder()
        .named("Test")
        .tryFlow(ErrorRawStepFlow.flowNamed("Tried"))
        .recover(SuccessStepFlow.flowNamed("Recover"))
        .recoverOn(RecoverableFlowException.ALL)
        .build();

    StepVerifier
        .create(recoverableFlow.run(FlowContext.create()))
        .assertNext(TestUtils.assertAndLog(globalReport -> {
          Assertions.assertThat(globalReport.getContext().get("Tried")).isNull();
          Assertions.assertThat(globalReport.getContext().get("Recover")).isEqualTo("Recover");
          assertThat(globalReport.getStatus()).isEqualTo(Status.SUCCESS);
          Assertions.assertThat(globalReport.getAllRecoveredErrors()).hasSize(1);
          Assertions.assertThat(globalReport.getAllErrors()).isEmpty();
          Assertions.assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  void givenMonoFailedTryFlowAndRecoverMatching_recoverableFlow_shouldSuccess() {
    RecoverableFlow<FlowContext> recoverableFlow = RecoverableFlowBuilder
        .defaultBuilder()
        .named("Test")
        .tryFlow(ErrorMonoStepFlow.flowNamed("Tried"))
        .recover(SuccessStepFlow.flowNamed("Recover"))
        .recoverOn(RecoverableFlowException.ALL)
        .build();

    StepVerifier
        .create(recoverableFlow.run(FlowContext.create()))
        .assertNext(TestUtils.assertAndLog(globalReport -> {
          Assertions.assertThat(globalReport.getContext().get("Tried")).isNull();
          Assertions.assertThat(globalReport.getContext().get("Recover")).isEqualTo("Recover");
          assertThat(globalReport.getStatus()).isEqualTo(Status.SUCCESS);
          Assertions.assertThat(globalReport.getAllRecoveredErrors()).hasSize(1);
          Assertions.assertThat(globalReport.getAllErrors()).isEmpty();
          Assertions.assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  void givenFailedTryFlowAndRecoverMatchingWithWarning_recoverableFlow_shouldWarning() {
    RecoverableFlow<FlowContext> recoverableFlow = RecoverableFlowBuilder
        .defaultBuilder()
        .named("Test")
        .tryFlow(ErrorStepFlow.flowNamed("Tried"))
        .recover(WarningStepFlow.flowNamed("Recover"))
        .recoverOn(RecoverableFlowException.ALL)
        .build();

    StepVerifier
        .create(recoverableFlow.run(FlowContext.create()))
        .assertNext(TestUtils.assertAndLog(globalReport -> {
          Assertions.assertThat(globalReport.getContext().get("Tried")).isNull();
          Assertions.assertThat(globalReport.getContext().get("Recover")).isEqualTo("Recover");
          assertThat(globalReport.getStatus()).isEqualTo(Status.WARNING);
          Assertions.assertThat(globalReport.getAllRecoveredErrors()).hasSize(1);
          Assertions.assertThat(globalReport.getAllErrors()).isEmpty();
          Assertions.assertThat(globalReport.getAllWarnings()).hasSize(1);
        }))
        .verifyComplete();
  }

  @Test
  void givenFailedTryFlowAndRecoverNotMatching_recoverableFlow_shouldError() {
    RecoverableFlow<FlowContext> recoverableFlow = RecoverableFlowBuilder
        .defaultBuilder()
        .named("Test")
        .tryFlow(ErrorStepFlow.flowNamed("Tried"))
        .recover(SuccessStepFlow.flowNamed("Recover"))
        .recoverOn(RecoverableFlowException.FUNCTIONAL)
        .build();

    StepVerifier
        .create(recoverableFlow.run(FlowContext.create()))
        .assertNext(TestUtils.assertAndLog(globalReport -> {
          Assertions.assertThat(globalReport.getContext().get("Tried")).isNull();
          Assertions.assertThat(globalReport.getContext().get("Recover")).isNull();
          assertThat(globalReport.getStatus()).isEqualTo(Status.ERROR);
          Assertions.assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          Assertions.assertThat(globalReport.getAllErrors()).hasSize(1);
          Assertions.assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  void givenNullName_recoverableFlow_shouldNotBuild() {
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
  void givenNullTryFlow_recoverableFlow_shouldNotBuild() {
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
  void givenNullRecover_recoverableFlow_shouldNotBuild() {
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
  void givenNullRecoverOn_recoverableFlow_shouldNotBuild() {
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
