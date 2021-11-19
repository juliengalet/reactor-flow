package io.github.juliengalet.reactorflow.flow;

import io.github.juliengalet.reactorflow.builder.SequentialFlowBuilder;
import io.github.juliengalet.reactorflow.exception.FlowBuilderException;
import io.github.juliengalet.reactorflow.exception.FlowException;
import io.github.juliengalet.reactorflow.report.FlowContext;
import io.github.juliengalet.reactorflow.report.Report;
import io.github.juliengalet.reactorflow.report.Status;
import io.github.juliengalet.reactorflow.testutils.CustomContext;
import io.github.juliengalet.reactorflow.testutils.ErrorRawStepFlow;
import io.github.juliengalet.reactorflow.testutils.SuccessStepFlow;
import io.github.juliengalet.reactorflow.testutils.WarningStepFlow;
import io.github.juliengalet.reactorflow.testutils.TestUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

final class SequentialFlowTest {
  @Test
  void checkRootFlowToString() {
    SequentialFlow<FlowContext> sequentialFlow = SequentialFlowBuilder
        .defaultBuilder()
        .named("Test")
        .then(SuccessStepFlow.flowNamed("Step 1"))
        .then(SuccessStepFlow.flowNamed("Step 2"))
        .then(SuccessStepFlow.flowNamed("Step 3"))
        .build();

    StepVerifier.create(sequentialFlow.run(FlowContext.create()))
        .assertNext(TestUtils.assertAndLog(globalReport -> assertThat(sequentialFlow.toString()).isNotNull()))
        .verifyComplete();
  }

  @Test
  void checkRootFlowToPrettyString() {
    SequentialFlow<FlowContext> sequentialFlow = SequentialFlowBuilder
        .defaultBuilder()
        .named("Test")
        .then(SuccessStepFlow.flowNamed("Step 1"))
        .then(SuccessStepFlow.flowNamed("Step 2"))
        .then(SuccessStepFlow.flowNamed("Step 3"))
        .build();

    StepVerifier.create(sequentialFlow.run(FlowContext.create()))
        .assertNext(TestUtils.assertAndLog(globalReport -> assertThat(sequentialFlow.toPrettyString()).isNotNull()))
        .verifyComplete();
  }

  @Test
  void checkToTreeString() {
    SequentialFlow<FlowContext> sequentialFlow = SequentialFlowBuilder
        .defaultBuilder()
        .named("Test")
        .then(SuccessStepFlow.flowNamed("Step 1"))
        .then(SuccessStepFlow.flowNamed("Step 2"))
        .then(SuccessStepFlow.flowNamed("Step 3"))
        .build();

    StepVerifier.create(sequentialFlow.run(FlowContext.create()))
        .assertNext(TestUtils.assertAndLog(globalReport -> assertThat(sequentialFlow.toTreeString()).isNotNull()))
        .verifyComplete();
  }

  @Test
  void checkToPrettyTreeString() {
    SequentialFlow<FlowContext> sequentialFlow = SequentialFlowBuilder
        .defaultBuilder()
        .named("Test")
        .then(SuccessStepFlow.flowNamed("Step 1"))
        .then(SuccessStepFlow.flowNamed("Step 2"))
        .then(SuccessStepFlow.flowNamed("Step 3"))
        .build();

    StepVerifier.create(sequentialFlow.run(FlowContext.create()))
        .assertNext(TestUtils.assertAndLog(globalReport -> assertThat(sequentialFlow.toPrettyTreeString()).isNotNull()))
        .verifyComplete();
  }

  @Test
  void givenSuccessSequenceWithoutFinally_sequentialFlow_shouldSuccess() {
    SequentialFlow<FlowContext> sequentialFlow = SequentialFlowBuilder
        .defaultBuilder()
        .named("Test")
        .then(SuccessStepFlow.flowNamed("Step 1"))
        .then(SuccessStepFlow.flowNamed("Step 2"))
        .then(SuccessStepFlow.flowNamed("Step 3"))
        .build();

    StepVerifier.create(sequentialFlow.run(FlowContext.create()))
        .assertNext(TestUtils.assertAndLog(globalReport -> {
          Assertions.assertThat(globalReport.getName()).isEqualTo("Test");
          assertThat(globalReport.getContext().get("Step 1")).isEqualTo("Step 1");
          assertThat(globalReport.getContext().get("Step 2")).isEqualTo("Step 2");
          assertThat(globalReport.getContext().get("Step 3")).isEqualTo("Step 3");
          assertThat(globalReport.getStatus()).isEqualTo(Status.SUCCESS);
          Assertions.assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          Assertions.assertThat(globalReport.getAllErrors()).isEmpty();
          Assertions.assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  void givenSuccessSequenceWithoutFinallyAndCustomContext_sequentialFlow_shouldSuccess() {
    SequentialFlow<CustomContext> sequentialFlow = SequentialFlowBuilder
        .builderForContextOfType(CustomContext.class)
        .named("Test")
        .then(SuccessStepFlow.flowNamed("Step 1"))
        .then(SuccessStepFlow.flowNamed("Step 2"))
        .then(SuccessStepFlow.flowNamed("Step 3"))
        .build();

    StepVerifier.create(sequentialFlow.run(new CustomContext()))
        .assertNext(TestUtils.assertAndLog(globalReport -> {
          Assertions.assertThat(globalReport.getName()).isEqualTo("Test");
          assertThat(globalReport.getContext().customField).isNotNull();
          assertThat(globalReport.getContext().get("Step 1")).isEqualTo("Step 1");
          assertThat(globalReport.getContext().get("Step 2")).isEqualTo("Step 2");
          assertThat(globalReport.getContext().get("Step 3")).isEqualTo("Step 3");
          assertThat(globalReport.getStatus()).isEqualTo(Status.SUCCESS);
          Assertions.assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          Assertions.assertThat(globalReport.getAllErrors()).isEmpty();
          Assertions.assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  void givenSuccessSequenceWithoutFinallyClonedWithNewName_sequentialFlow_shouldSuccess() {
    SequentialFlow<FlowContext> sequentialFlow = SequentialFlowBuilder
        .defaultBuilder()
        .named("Test")
        .then(SuccessStepFlow.flowNamed("Step 1"))
        .then(SuccessStepFlow.flowNamed("Step 2"))
        .then(SuccessStepFlow.flowNamed("Step 3"))
        .build();

    StepVerifier.create(sequentialFlow.cloneFlow("Test copy").run(FlowContext.create()))
        .assertNext(TestUtils.assertAndLog(globalReport -> {
          Assertions.assertThat(globalReport.getName()).isEqualTo("Test copy");
          assertThat(globalReport.getContext().get("Step 1")).isEqualTo("Step 1");
          assertThat(globalReport.getContext().get("Step 2")).isEqualTo("Step 2");
          assertThat(globalReport.getContext().get("Step 3")).isEqualTo("Step 3");
          assertThat(globalReport.getStatus()).isEqualTo(Status.SUCCESS);
          Assertions.assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          Assertions.assertThat(globalReport.getAllErrors()).isEmpty();
          Assertions.assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  void givenSuccessSequenceWithoutFinallyCloned_sequentialFlow_shouldSuccess() {
    SequentialFlow<FlowContext> sequentialFlow = SequentialFlowBuilder
        .defaultBuilder()
        .named("Test")
        .then(SuccessStepFlow.flowNamed("Step 1"))
        .then(SuccessStepFlow.flowNamed("Step 2"))
        .then(SuccessStepFlow.flowNamed("Step 3"))
        .build();

    StepVerifier.create(sequentialFlow.cloneFlow().run(FlowContext.create()))
        .assertNext(TestUtils.assertAndLog(globalReport -> {
          Assertions.assertThat(globalReport.getName()).isEqualTo("Test");
          assertThat(globalReport.getContext().get("Step 1")).isEqualTo("Step 1");
          assertThat(globalReport.getContext().get("Step 2")).isEqualTo("Step 2");
          assertThat(globalReport.getContext().get("Step 3")).isEqualTo("Step 3");
          assertThat(globalReport.getStatus()).isEqualTo(Status.SUCCESS);
          Assertions.assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          Assertions.assertThat(globalReport.getAllErrors()).isEmpty();
          Assertions.assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  void givenSequenceWithWarningWithoutFinally_sequentialFlow_shouldWarning() {
    SequentialFlow<FlowContext> sequentialFlow = SequentialFlowBuilder
        .defaultBuilder()
        .named("Test")
        .then(SuccessStepFlow.flowNamed("Step 1"))
        .then(WarningStepFlow.flowNamed("Step 2"))
        .then(SuccessStepFlow.flowNamed("Step 3"))
        .build();

    StepVerifier.create(sequentialFlow.run(FlowContext.create()))
        .assertNext(TestUtils.assertAndLog(globalReport -> {
          Assertions.assertThat(globalReport.getName()).isEqualTo("Test");
          assertThat(globalReport.getContext().get("Step 1")).isEqualTo("Step 1");
          assertThat(globalReport.getContext().get("Step 2")).isEqualTo("Step 2");
          assertThat(globalReport.getContext().get("Step 3")).isEqualTo("Step 3");
          assertThat(globalReport.getStatus()).isEqualTo(Status.WARNING);
          Assertions.assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          Assertions.assertThat(globalReport.getAllErrors()).isEmpty();
          Assertions.assertThat(globalReport.getAllWarnings()).hasSize(1);
        }))
        .verifyComplete();
  }

  @Test
  void givenSequenceWithWarningAndErrorWithoutFinally_sequentialFlow_shouldError() {
    SequentialFlow<FlowContext> sequentialFlow = SequentialFlowBuilder
        .defaultBuilder()
        .named("Test")
        .then(WarningStepFlow.flowNamed("Step 1"))
        .then(ErrorRawStepFlow.flowNamed("Step 2"))
        .then(SuccessStepFlow.flowNamed("Step 3"))
        .build();

    StepVerifier.create(sequentialFlow.run(FlowContext.create()))
        .assertNext(TestUtils.assertAndLog(globalReport -> {
          Assertions.assertThat(globalReport.getName()).isEqualTo("Test");
          assertThat(globalReport.getContext().get("Step 1")).isEqualTo("Step 1");
          assertThat(globalReport.getStatus()).isEqualTo(Status.ERROR);
          Assertions.assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          Assertions.assertThat(globalReport.getAllErrors()).hasSize(1);
          Assertions.assertThat(globalReport.getAllWarnings()).hasSize(1);
        }))
        .verifyComplete();
  }

  @Test
  void givenSuccessSequenceWithFinally_sequentialFlow_shouldSuccess() {
    SequentialFlow<FlowContext> sequentialFlow = SequentialFlowBuilder
        .defaultBuilder()
        .named("Test")
        .then(SuccessStepFlow.flowNamed("Step 1"))
        .then(SuccessStepFlow.flowNamed("Step 2"))
        .then(SuccessStepFlow.flowNamed("Step 3"))
        .doFinally(SuccessStepFlow.flowNamed("Final step"))
        .build();

    StepVerifier.create(sequentialFlow.run(FlowContext.create()))
        .assertNext(TestUtils.assertAndLog(globalReport -> {
          Assertions.assertThat(globalReport.getName()).isEqualTo("Test");
          assertThat(globalReport.getContext().get("Step 1")).isEqualTo("Step 1");
          assertThat(globalReport.getContext().get("Step 2")).isEqualTo("Step 2");
          assertThat(globalReport.getContext().get("Step 3")).isEqualTo("Step 3");
          assertThat(globalReport.getContext().get("Final step")).isEqualTo("Final step");
          assertThat(globalReport.getStatus()).isEqualTo(Status.SUCCESS);
          Assertions.assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          Assertions.assertThat(globalReport.getAllErrors()).isEmpty();
          Assertions.assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  void givenSuccessSequenceWithFinallyWarning_sequentialFlow_shouldWarning() {
    SequentialFlow<FlowContext> sequentialFlow = SequentialFlowBuilder
        .defaultBuilder()
        .named("Test")
        .then(SuccessStepFlow.flowNamed("Step 1"))
        .then(SuccessStepFlow.flowNamed("Step 2"))
        .then(SuccessStepFlow.flowNamed("Step 3"))
        .doFinally(WarningStepFlow.flowNamed("Final step"))
        .build();

    StepVerifier.create(sequentialFlow.run(FlowContext.create()))
        .assertNext(TestUtils.assertAndLog(globalReport -> {
          Assertions.assertThat(globalReport.getName()).isEqualTo("Test");
          assertThat(globalReport.getContext().get("Step 1")).isEqualTo("Step 1");
          assertThat(globalReport.getContext().get("Step 2")).isEqualTo("Step 2");
          assertThat(globalReport.getContext().get("Step 3")).isEqualTo("Step 3");
          assertThat(globalReport.getContext().get("Final step")).isEqualTo("Final step");
          assertThat(globalReport.getStatus()).isEqualTo(Status.WARNING);
          Assertions.assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          Assertions.assertThat(globalReport.getAllErrors()).isEmpty();
          Assertions.assertThat(globalReport.getAllWarnings()).hasSize(1);
        }))
        .verifyComplete();
  }

  @Test
  void givenWarningSequenceWithFinallyError_sequentialFlow_shouldWarning() {
    SequentialFlow<FlowContext> sequentialFlow = SequentialFlowBuilder
        .defaultBuilder()
        .named("Test")
        .then(SuccessStepFlow.flowNamed("Step 1"))
        .then(SuccessStepFlow.flowNamed("Step 2"))
        .then(WarningStepFlow.flowNamed("Step 3"))
        .doFinally(ErrorRawStepFlow.flowNamed("Final step"))
        .build();

    StepVerifier.create(sequentialFlow.run(FlowContext.create()))
        .assertNext(TestUtils.assertAndLog(globalReport -> {
          Assertions.assertThat(globalReport.getName()).isEqualTo("Test");
          assertThat(globalReport.getContext().get("Step 1")).isEqualTo("Step 1");
          assertThat(globalReport.getContext().get("Step 2")).isEqualTo("Step 2");
          assertThat(globalReport.getContext().get("Step 3")).isEqualTo("Step 3");
          assertThat(globalReport.getStatus()).isEqualTo(Status.ERROR);
          Assertions.assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          Assertions.assertThat(globalReport.getAllErrors()).hasSize(1);
          Assertions.assertThat(globalReport.getAllWarnings()).hasSize(1);
        }))
        .verifyComplete();
  }

  @Test
  void givenSuccessSequenceWithFinallyClonedWithNewName_sequentialFlow_shouldSuccess() {
    SequentialFlow<FlowContext> sequentialFlow = SequentialFlowBuilder
        .defaultBuilder()
        .named("Test")
        .then(SuccessStepFlow.flowNamed("Step 1"))
        .then(SuccessStepFlow.flowNamed("Step 2"))
        .then(SuccessStepFlow.flowNamed("Step 3"))
        .doFinally(SuccessStepFlow.flowNamed("Final step"))
        .build();

    StepVerifier.create(sequentialFlow.cloneFlow("Test copy").run(FlowContext.create()))
        .assertNext(TestUtils.assertAndLog(globalReport -> {
          Assertions.assertThat(globalReport.getName()).isEqualTo("Test copy");
          assertThat(globalReport.getContext().get("Step 1")).isEqualTo("Step 1");
          assertThat(globalReport.getContext().get("Step 2")).isEqualTo("Step 2");
          assertThat(globalReport.getContext().get("Step 3")).isEqualTo("Step 3");
          assertThat(globalReport.getContext().get("Final step")).isEqualTo("Final step");
          assertThat(globalReport.getStatus()).isEqualTo(Status.SUCCESS);
          Assertions.assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          Assertions.assertThat(globalReport.getAllErrors()).isEmpty();
          Assertions.assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  void givenSuccessSequenceWithFinallyCloned_sequentialFlow_shouldSuccess() {
    SequentialFlow<FlowContext> sequentialFlow = SequentialFlowBuilder
        .defaultBuilder()
        .named("Test")
        .then(SuccessStepFlow.flowNamed("Step 1"))
        .then(SuccessStepFlow.flowNamed("Step 2"))
        .then(SuccessStepFlow.flowNamed("Step 3"))
        .doFinally(SuccessStepFlow.flowNamed("Final step"))
        .build();

    StepVerifier.create(sequentialFlow.cloneFlow().run(FlowContext.create()))
        .assertNext(TestUtils.assertAndLog(globalReport -> {
          Assertions.assertThat(globalReport.getName()).isEqualTo("Test");
          assertThat(globalReport.getContext().get("Step 1")).isEqualTo("Step 1");
          assertThat(globalReport.getContext().get("Step 2")).isEqualTo("Step 2");
          assertThat(globalReport.getContext().get("Step 3")).isEqualTo("Step 3");
          assertThat(globalReport.getContext().get("Final step")).isEqualTo("Final step");
          assertThat(globalReport.getStatus()).isEqualTo(Status.SUCCESS);
          Assertions.assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          Assertions.assertThat(globalReport.getAllErrors()).isEmpty();
          Assertions.assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  void givenSequenceWithWarningWithFinally_sequentialFlow_shouldWarning() {
    SequentialFlow<FlowContext> sequentialFlow = SequentialFlowBuilder
        .defaultBuilder()
        .named("Test")
        .then(SuccessStepFlow.flowNamed("Step 1"))
        .then(WarningStepFlow.flowNamed("Step 2"))
        .then(SuccessStepFlow.flowNamed("Step 3"))
        .doFinally(SuccessStepFlow.flowNamed("Final step"))
        .build();

    StepVerifier.create(sequentialFlow.run(FlowContext.create()))
        .assertNext(TestUtils.assertAndLog(globalReport -> {
          Assertions.assertThat(globalReport.getName()).isEqualTo("Test");
          assertThat(globalReport.getContext().get("Step 1")).isEqualTo("Step 1");
          assertThat(globalReport.getContext().get("Step 2")).isEqualTo("Step 2");
          assertThat(globalReport.getContext().get("Step 3")).isEqualTo("Step 3");
          assertThat(globalReport.getContext().get("Final step")).isEqualTo("Final step");
          assertThat(globalReport.getStatus()).isEqualTo(Status.WARNING);
          Assertions.assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          Assertions.assertThat(globalReport.getAllErrors()).isEmpty();
          Assertions.assertThat(globalReport.getAllWarnings()).hasSize(1);
        }))
        .verifyComplete();
  }

  @Test
  void givenSequenceWithWarningAndErrorWithFinally_sequentialFlow_shouldErrorAndShouldGetItInFinally() {
    SequentialFlow<FlowContext> sequentialFlow = SequentialFlowBuilder
        .defaultBuilder()
        .named("Test")
        .then(WarningStepFlow.flowNamed("Step 1"))
        .then(ErrorRawStepFlow.flowNamed("Step 2"))
        .then(SuccessStepFlow.flowNamed("Step 3"))
        .doFinally(StepWithMetadata.build((context, metadata) -> {
          List<FlowException> errors = metadata.getErrors();
          List<FlowException> warnings = metadata.getWarnings();

          context.put("error", errors.get(0).getMessage());
          context.put("warning", warnings.get(0).getMessage());
          return Mono.just(Report.success(context));
        }, "Final step"))
        .build();

    StepVerifier.create(sequentialFlow.run(FlowContext.create()))
        .assertNext(TestUtils.assertAndLog(globalReport -> {
          Assertions.assertThat(globalReport.getName()).isEqualTo("Test");
          assertThat(globalReport.getContext().get("Step 1")).isEqualTo("Step 1");
          assertThat(globalReport.getContext().get("error")).isEqualTo("Step 2 raw error");
          assertThat(globalReport.getContext().get("warning")).isEqualTo("Step 1");
          assertThat(globalReport.getStatus()).isEqualTo(Status.ERROR);
          Assertions.assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          Assertions.assertThat(globalReport.getAllErrors()).hasSize(1);
          Assertions.assertThat(globalReport.getAllWarnings()).hasSize(1);
        }))
        .verifyComplete();
  }

  @Test
  void givenSuccessSequenceWithFinallyEmpty_sequentialFlow_shouldError() {
    SequentialFlow<FlowContext> sequentialFlow = SequentialFlowBuilder
        .defaultBuilder()
        .named("Test")
        .then(SuccessStepFlow.flowNamed("Step 1"))
        .then(SuccessStepFlow.flowNamed("Step 2"))
        .then(SuccessStepFlow.flowNamed("Step 3"))
        .doFinally(StepWithMetadata.build((context, metadata) -> Mono.empty(), "Final step"))
        .build();

    StepVerifier.create(sequentialFlow.run(FlowContext.create()))
        .assertNext(TestUtils.assertAndLog(globalReport -> {
          Assertions.assertThat(globalReport.getName()).isEqualTo("Test");
          assertThat(globalReport.getContext().get("Step 1")).isEqualTo("Step 1");
          assertThat(globalReport.getContext().get("Step 2")).isEqualTo("Step 2");
          assertThat(globalReport.getContext().get("Step 3")).isEqualTo("Step 3");
          assertThat(globalReport.getStatus()).isEqualTo(Status.ERROR);
          Assertions.assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          Assertions.assertThat(globalReport.getAllErrors()).hasSize(1);
          Assertions.assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  void givenSuccessSequenceWithFinallyNull_sequentialFlow_shouldError() {
    SequentialFlow<FlowContext> sequentialFlow = SequentialFlowBuilder
        .defaultBuilder()
        .named("Test")
        .then(SuccessStepFlow.flowNamed("Step 1"))
        .then(SuccessStepFlow.flowNamed("Step 2"))
        .then(SuccessStepFlow.flowNamed("Step 3"))
        .doFinally(StepWithMetadata.build((context, metadata) -> null, "Final step"))
        .build();

    StepVerifier.create(sequentialFlow.run(FlowContext.create()))
        .assertNext(TestUtils.assertAndLog(globalReport -> {
          Assertions.assertThat(globalReport.getName()).isEqualTo("Test");
          assertThat(globalReport.getContext().get("Step 1")).isEqualTo("Step 1");
          assertThat(globalReport.getContext().get("Step 2")).isEqualTo("Step 2");
          assertThat(globalReport.getContext().get("Step 3")).isEqualTo("Step 3");
          assertThat(globalReport.getStatus()).isEqualTo(Status.ERROR);
          Assertions.assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          Assertions.assertThat(globalReport.getAllErrors()).hasSize(1);
          Assertions.assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  void givenSequenceWithWarningAndErrorWithFinally_sequentialFlow_shouldError() {
    SequentialFlow<FlowContext> sequentialFlow = SequentialFlowBuilder
        .defaultBuilder()
        .named("Test")
        .then(WarningStepFlow.flowNamed("Step 1"))
        .then(ErrorRawStepFlow.flowNamed("Step 2"))
        .then(SuccessStepFlow.flowNamed("Step 3"))
        .doFinally(SuccessStepFlow.flowNamed("Final step"))
        .build();

    StepVerifier.create(sequentialFlow.run(FlowContext.create()))
        .assertNext(TestUtils.assertAndLog(globalReport -> {
          Assertions.assertThat(globalReport.getName()).isEqualTo("Test");
          assertThat(globalReport.getContext().get("Step 1")).isEqualTo("Step 1");
          assertThat(globalReport.getContext().get("Final step")).isEqualTo("Final step");
          assertThat(globalReport.getStatus()).isEqualTo(Status.ERROR);
          Assertions.assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          Assertions.assertThat(globalReport.getAllErrors()).hasSize(1);
          Assertions.assertThat(globalReport.getAllWarnings()).hasSize(1);
        }))
        .verifyComplete();
  }

  @Test
  void givenNullName_sequentialFlow_shouldNotBuild() {
    assertThatExceptionOfType(FlowBuilderException.class).isThrownBy(() -> SequentialFlowBuilder
        .defaultBuilder()
        .named(null)
        .then(SuccessStepFlow.flowNamed("Step"))
        .doFinally(SuccessStepFlow.flowNamed("Finally"))
        .build()
    );
  }

  @Test
  void givenNullThen_sequentialFlow_shouldNotBuild() {
    assertThatExceptionOfType(FlowBuilderException.class).isThrownBy(() -> SequentialFlowBuilder
        .defaultBuilder()
        .named("Test")
        .then(null)
        .doFinally(SuccessStepFlow.flowNamed("Finally"))
        .build()
    );
  }

  @Test
  void givenNullFinally_sequentialFlow_shouldNotBuild() {
    assertThatExceptionOfType(FlowBuilderException.class).isThrownBy(() -> SequentialFlowBuilder
        .defaultBuilder()
        .named("Test")
        .then(SuccessStepFlow.flowNamed("Step"))
        .doFinally(null)
        .build()
    );
  }
}
