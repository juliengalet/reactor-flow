package fr.jtools.reactorflow.flow;

import fr.jtools.reactorflow.builder.SequentialFlowBuilder;
import fr.jtools.reactorflow.exception.FlowBuilderException;
import fr.jtools.reactorflow.exception.FlowException;
import fr.jtools.reactorflow.report.FlowContext;
import fr.jtools.reactorflow.report.Report;
import fr.jtools.reactorflow.report.Status;
import fr.jtools.reactorflow.testutils.CustomContext;
import fr.jtools.reactorflow.testutils.ErrorRawStepFlow;
import fr.jtools.reactorflow.testutils.SuccessStepFlow;
import fr.jtools.reactorflow.testutils.WarningStepFlow;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static fr.jtools.reactorflow.testutils.TestUtils.assertAndLog;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

final class SequentialFlowTest {
  @Test
  final void checkRootFlowToString() {
    SequentialFlow<FlowContext> sequentialFlow = SequentialFlowBuilder
        .defaultBuilder()
        .named("Test")
        .then(SuccessStepFlow.flowNamed("Step 1"))
        .then(SuccessStepFlow.flowNamed("Step 2"))
        .then(SuccessStepFlow.flowNamed("Step 3"))
        .build();

    StepVerifier.create(sequentialFlow.run(FlowContext.create()))
        .assertNext(assertAndLog(globalReport -> assertThat(sequentialFlow.toString()).isNotNull()))
        .verifyComplete();
  }

  @Test
  final void checkRootFlowToPrettyString() {
    SequentialFlow<FlowContext> sequentialFlow = SequentialFlowBuilder
        .defaultBuilder()
        .named("Test")
        .then(SuccessStepFlow.flowNamed("Step 1"))
        .then(SuccessStepFlow.flowNamed("Step 2"))
        .then(SuccessStepFlow.flowNamed("Step 3"))
        .build();

    StepVerifier.create(sequentialFlow.run(FlowContext.create()))
        .assertNext(assertAndLog(globalReport -> assertThat(sequentialFlow.toPrettyString()).isNotNull()))
        .verifyComplete();
  }

  @Test
  final void checkToTreeString() {
    SequentialFlow<FlowContext> sequentialFlow = SequentialFlowBuilder
        .defaultBuilder()
        .named("Test")
        .then(SuccessStepFlow.flowNamed("Step 1"))
        .then(SuccessStepFlow.flowNamed("Step 2"))
        .then(SuccessStepFlow.flowNamed("Step 3"))
        .build();

    StepVerifier.create(sequentialFlow.run(FlowContext.create()))
        .assertNext(assertAndLog(globalReport -> assertThat(sequentialFlow.toTreeString()).isNotNull()))
        .verifyComplete();
  }

  @Test
  final void checkToPrettyTreeString() {
    SequentialFlow<FlowContext> sequentialFlow = SequentialFlowBuilder
        .defaultBuilder()
        .named("Test")
        .then(SuccessStepFlow.flowNamed("Step 1"))
        .then(SuccessStepFlow.flowNamed("Step 2"))
        .then(SuccessStepFlow.flowNamed("Step 3"))
        .build();

    StepVerifier.create(sequentialFlow.run(FlowContext.create()))
        .assertNext(assertAndLog(globalReport -> assertThat(sequentialFlow.toPrettyTreeString()).isNotNull()))
        .verifyComplete();
  }

  @Test
  final void givenSuccessSequenceWithoutFinally_sequentialFlow_shouldSuccess() {
    SequentialFlow<FlowContext> sequentialFlow = SequentialFlowBuilder
        .defaultBuilder()
        .named("Test")
        .then(SuccessStepFlow.flowNamed("Step 1"))
        .then(SuccessStepFlow.flowNamed("Step 2"))
        .then(SuccessStepFlow.flowNamed("Step 3"))
        .build();

    StepVerifier.create(sequentialFlow.run(FlowContext.create()))
        .assertNext(assertAndLog(globalReport -> {
          assertThat(globalReport.getName()).isEqualTo("Test");
          assertThat(globalReport.getContext().get("Step 1")).isEqualTo("Step 1");
          assertThat(globalReport.getContext().get("Step 2")).isEqualTo("Step 2");
          assertThat(globalReport.getContext().get("Step 3")).isEqualTo("Step 3");
          assertThat(globalReport.getStatus()).isEqualTo(Status.SUCCESS);
          assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          assertThat(globalReport.getAllErrors()).isEmpty();
          assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  final void givenSuccessSequenceWithoutFinallyAndCustomContext_sequentialFlow_shouldSuccess() {
    SequentialFlow<CustomContext> sequentialFlow = SequentialFlowBuilder
        .builderForContextOfType(CustomContext.class)
        .named("Test")
        .then(SuccessStepFlow.flowNamed("Step 1"))
        .then(SuccessStepFlow.flowNamed("Step 2"))
        .then(SuccessStepFlow.flowNamed("Step 3"))
        .build();

    StepVerifier.create(sequentialFlow.run(new CustomContext()))
        .assertNext(assertAndLog(globalReport -> {
          assertThat(globalReport.getName()).isEqualTo("Test");
          assertThat(globalReport.getContext().customField).isNotNull();
          assertThat(globalReport.getContext().get("Step 1")).isEqualTo("Step 1");
          assertThat(globalReport.getContext().get("Step 2")).isEqualTo("Step 2");
          assertThat(globalReport.getContext().get("Step 3")).isEqualTo("Step 3");
          assertThat(globalReport.getStatus()).isEqualTo(Status.SUCCESS);
          assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          assertThat(globalReport.getAllErrors()).isEmpty();
          assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  final void givenSuccessSequenceWithoutFinallyClonedWithNewName_sequentialFlow_shouldSuccess() {
    SequentialFlow<FlowContext> sequentialFlow = SequentialFlowBuilder
        .defaultBuilder()
        .named("Test")
        .then(SuccessStepFlow.flowNamed("Step 1"))
        .then(SuccessStepFlow.flowNamed("Step 2"))
        .then(SuccessStepFlow.flowNamed("Step 3"))
        .build();

    StepVerifier.create(sequentialFlow.cloneFlow("Test copy").run(FlowContext.create()))
        .assertNext(assertAndLog(globalReport -> {
          assertThat(globalReport.getName()).isEqualTo("Test copy");
          assertThat(globalReport.getContext().get("Step 1")).isEqualTo("Step 1");
          assertThat(globalReport.getContext().get("Step 2")).isEqualTo("Step 2");
          assertThat(globalReport.getContext().get("Step 3")).isEqualTo("Step 3");
          assertThat(globalReport.getStatus()).isEqualTo(Status.SUCCESS);
          assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          assertThat(globalReport.getAllErrors()).isEmpty();
          assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  final void givenSuccessSequenceWithoutFinallyCloned_sequentialFlow_shouldSuccess() {
    SequentialFlow<FlowContext> sequentialFlow = SequentialFlowBuilder
        .defaultBuilder()
        .named("Test")
        .then(SuccessStepFlow.flowNamed("Step 1"))
        .then(SuccessStepFlow.flowNamed("Step 2"))
        .then(SuccessStepFlow.flowNamed("Step 3"))
        .build();

    StepVerifier.create(sequentialFlow.cloneFlow().run(FlowContext.create()))
        .assertNext(assertAndLog(globalReport -> {
          assertThat(globalReport.getName()).isEqualTo("Test");
          assertThat(globalReport.getContext().get("Step 1")).isEqualTo("Step 1");
          assertThat(globalReport.getContext().get("Step 2")).isEqualTo("Step 2");
          assertThat(globalReport.getContext().get("Step 3")).isEqualTo("Step 3");
          assertThat(globalReport.getStatus()).isEqualTo(Status.SUCCESS);
          assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          assertThat(globalReport.getAllErrors()).isEmpty();
          assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  final void givenSequenceWithWarningWithoutFinally_sequentialFlow_shouldWarning() {
    SequentialFlow<FlowContext> sequentialFlow = SequentialFlowBuilder
        .defaultBuilder()
        .named("Test")
        .then(SuccessStepFlow.flowNamed("Step 1"))
        .then(WarningStepFlow.flowNamed("Step 2"))
        .then(SuccessStepFlow.flowNamed("Step 3"))
        .build();

    StepVerifier.create(sequentialFlow.run(FlowContext.create()))
        .assertNext(assertAndLog(globalReport -> {
          assertThat(globalReport.getName()).isEqualTo("Test");
          assertThat(globalReport.getContext().get("Step 1")).isEqualTo("Step 1");
          assertThat(globalReport.getContext().get("Step 2")).isEqualTo("Step 2");
          assertThat(globalReport.getContext().get("Step 3")).isEqualTo("Step 3");
          assertThat(globalReport.getStatus()).isEqualTo(Status.WARNING);
          assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          assertThat(globalReport.getAllErrors()).isEmpty();
          assertThat(globalReport.getAllWarnings()).hasSize(1);
        }))
        .verifyComplete();
  }

  @Test
  final void givenSequenceWithWarningAndErrorWithoutFinally_sequentialFlow_shouldError() {
    SequentialFlow<FlowContext> sequentialFlow = SequentialFlowBuilder
        .defaultBuilder()
        .named("Test")
        .then(WarningStepFlow.flowNamed("Step 1"))
        .then(ErrorRawStepFlow.flowNamed("Step 2"))
        .then(SuccessStepFlow.flowNamed("Step 3"))
        .build();

    StepVerifier.create(sequentialFlow.run(FlowContext.create()))
        .assertNext(assertAndLog(globalReport -> {
          assertThat(globalReport.getName()).isEqualTo("Test");
          assertThat(globalReport.getContext().get("Step 1")).isEqualTo("Step 1");
          assertThat(globalReport.getStatus()).isEqualTo(Status.ERROR);
          assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          assertThat(globalReport.getAllErrors()).hasSize(1);
          assertThat(globalReport.getAllWarnings()).hasSize(1);
        }))
        .verifyComplete();
  }

  @Test
  final void givenSuccessSequenceWithFinally_sequentialFlow_shouldSuccess() {
    SequentialFlow<FlowContext> sequentialFlow = SequentialFlowBuilder
        .defaultBuilder()
        .named("Test")
        .then(SuccessStepFlow.flowNamed("Step 1"))
        .then(SuccessStepFlow.flowNamed("Step 2"))
        .then(SuccessStepFlow.flowNamed("Step 3"))
        .doFinally(SuccessStepFlow.flowNamed("Final step"))
        .build();

    StepVerifier.create(sequentialFlow.run(FlowContext.create()))
        .assertNext(assertAndLog(globalReport -> {
          assertThat(globalReport.getName()).isEqualTo("Test");
          assertThat(globalReport.getContext().get("Step 1")).isEqualTo("Step 1");
          assertThat(globalReport.getContext().get("Step 2")).isEqualTo("Step 2");
          assertThat(globalReport.getContext().get("Step 3")).isEqualTo("Step 3");
          assertThat(globalReport.getContext().get("Final step")).isEqualTo("Final step");
          assertThat(globalReport.getStatus()).isEqualTo(Status.SUCCESS);
          assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          assertThat(globalReport.getAllErrors()).isEmpty();
          assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  final void givenSuccessSequenceWithFinallyWarning_sequentialFlow_shouldWarning() {
    SequentialFlow<FlowContext> sequentialFlow = SequentialFlowBuilder
        .defaultBuilder()
        .named("Test")
        .then(SuccessStepFlow.flowNamed("Step 1"))
        .then(SuccessStepFlow.flowNamed("Step 2"))
        .then(SuccessStepFlow.flowNamed("Step 3"))
        .doFinally(WarningStepFlow.flowNamed("Final step"))
        .build();

    StepVerifier.create(sequentialFlow.run(FlowContext.create()))
        .assertNext(assertAndLog(globalReport -> {
          assertThat(globalReport.getName()).isEqualTo("Test");
          assertThat(globalReport.getContext().get("Step 1")).isEqualTo("Step 1");
          assertThat(globalReport.getContext().get("Step 2")).isEqualTo("Step 2");
          assertThat(globalReport.getContext().get("Step 3")).isEqualTo("Step 3");
          assertThat(globalReport.getContext().get("Final step")).isEqualTo("Final step");
          assertThat(globalReport.getStatus()).isEqualTo(Status.WARNING);
          assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          assertThat(globalReport.getAllErrors()).isEmpty();
          assertThat(globalReport.getAllWarnings()).hasSize(1);
        }))
        .verifyComplete();
  }

  @Test
  final void givenWarningSequenceWithFinallyError_sequentialFlow_shouldWarning() {
    SequentialFlow<FlowContext> sequentialFlow = SequentialFlowBuilder
        .defaultBuilder()
        .named("Test")
        .then(SuccessStepFlow.flowNamed("Step 1"))
        .then(SuccessStepFlow.flowNamed("Step 2"))
        .then(WarningStepFlow.flowNamed("Step 3"))
        .doFinally(ErrorRawStepFlow.flowNamed("Final step"))
        .build();

    StepVerifier.create(sequentialFlow.run(FlowContext.create()))
        .assertNext(assertAndLog(globalReport -> {
          assertThat(globalReport.getName()).isEqualTo("Test");
          assertThat(globalReport.getContext().get("Step 1")).isEqualTo("Step 1");
          assertThat(globalReport.getContext().get("Step 2")).isEqualTo("Step 2");
          assertThat(globalReport.getContext().get("Step 3")).isEqualTo("Step 3");
          assertThat(globalReport.getStatus()).isEqualTo(Status.ERROR);
          assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          assertThat(globalReport.getAllErrors()).hasSize(1);
          assertThat(globalReport.getAllWarnings()).hasSize(1);
        }))
        .verifyComplete();
  }

  @Test
  final void givenSuccessSequenceWithFinallyClonedWithNewName_sequentialFlow_shouldSuccess() {
    SequentialFlow<FlowContext> sequentialFlow = SequentialFlowBuilder
        .defaultBuilder()
        .named("Test")
        .then(SuccessStepFlow.flowNamed("Step 1"))
        .then(SuccessStepFlow.flowNamed("Step 2"))
        .then(SuccessStepFlow.flowNamed("Step 3"))
        .doFinally(SuccessStepFlow.flowNamed("Final step"))
        .build();

    StepVerifier.create(sequentialFlow.cloneFlow("Test copy").run(FlowContext.create()))
        .assertNext(assertAndLog(globalReport -> {
          assertThat(globalReport.getName()).isEqualTo("Test copy");
          assertThat(globalReport.getContext().get("Step 1")).isEqualTo("Step 1");
          assertThat(globalReport.getContext().get("Step 2")).isEqualTo("Step 2");
          assertThat(globalReport.getContext().get("Step 3")).isEqualTo("Step 3");
          assertThat(globalReport.getContext().get("Final step")).isEqualTo("Final step");
          assertThat(globalReport.getStatus()).isEqualTo(Status.SUCCESS);
          assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          assertThat(globalReport.getAllErrors()).isEmpty();
          assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  final void givenSuccessSequenceWithFinallyCloned_sequentialFlow_shouldSuccess() {
    SequentialFlow<FlowContext> sequentialFlow = SequentialFlowBuilder
        .defaultBuilder()
        .named("Test")
        .then(SuccessStepFlow.flowNamed("Step 1"))
        .then(SuccessStepFlow.flowNamed("Step 2"))
        .then(SuccessStepFlow.flowNamed("Step 3"))
        .doFinally(SuccessStepFlow.flowNamed("Final step"))
        .build();

    StepVerifier.create(sequentialFlow.cloneFlow().run(FlowContext.create()))
        .assertNext(assertAndLog(globalReport -> {
          assertThat(globalReport.getName()).isEqualTo("Test");
          assertThat(globalReport.getContext().get("Step 1")).isEqualTo("Step 1");
          assertThat(globalReport.getContext().get("Step 2")).isEqualTo("Step 2");
          assertThat(globalReport.getContext().get("Step 3")).isEqualTo("Step 3");
          assertThat(globalReport.getContext().get("Final step")).isEqualTo("Final step");
          assertThat(globalReport.getStatus()).isEqualTo(Status.SUCCESS);
          assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          assertThat(globalReport.getAllErrors()).isEmpty();
          assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  final void givenSequenceWithWarningWithFinally_sequentialFlow_shouldWarning() {
    SequentialFlow<FlowContext> sequentialFlow = SequentialFlowBuilder
        .defaultBuilder()
        .named("Test")
        .then(SuccessStepFlow.flowNamed("Step 1"))
        .then(WarningStepFlow.flowNamed("Step 2"))
        .then(SuccessStepFlow.flowNamed("Step 3"))
        .doFinally(SuccessStepFlow.flowNamed("Final step"))
        .build();

    StepVerifier.create(sequentialFlow.run(FlowContext.create()))
        .assertNext(assertAndLog(globalReport -> {
          assertThat(globalReport.getName()).isEqualTo("Test");
          assertThat(globalReport.getContext().get("Step 1")).isEqualTo("Step 1");
          assertThat(globalReport.getContext().get("Step 2")).isEqualTo("Step 2");
          assertThat(globalReport.getContext().get("Step 3")).isEqualTo("Step 3");
          assertThat(globalReport.getContext().get("Final step")).isEqualTo("Final step");
          assertThat(globalReport.getStatus()).isEqualTo(Status.WARNING);
          assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          assertThat(globalReport.getAllErrors()).isEmpty();
          assertThat(globalReport.getAllWarnings()).hasSize(1);
        }))
        .verifyComplete();
  }

  @Test
  final void givenSequenceWithWarningAndErrorWithFinally_sequentialFlow_shouldErrorAndShouldGetItInFinally() {
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
        .assertNext(assertAndLog(globalReport -> {
          assertThat(globalReport.getName()).isEqualTo("Test");
          assertThat(globalReport.getContext().get("Step 1")).isEqualTo("Step 1");
          assertThat(globalReport.getContext().get("error")).isEqualTo("Step 2 raw error");
          assertThat(globalReport.getContext().get("warning")).isEqualTo("Step 1");
          assertThat(globalReport.getStatus()).isEqualTo(Status.ERROR);
          assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          assertThat(globalReport.getAllErrors()).hasSize(1);
          assertThat(globalReport.getAllWarnings()).hasSize(1);
        }))
        .verifyComplete();
  }

  @Test
  final void givenSuccessSequenceWithFinallyEmpty_sequentialFlow_shouldError() {
    SequentialFlow<FlowContext> sequentialFlow = SequentialFlowBuilder
        .defaultBuilder()
        .named("Test")
        .then(SuccessStepFlow.flowNamed("Step 1"))
        .then(SuccessStepFlow.flowNamed("Step 2"))
        .then(SuccessStepFlow.flowNamed("Step 3"))
        .doFinally(StepWithMetadata.build((context, metadata) -> Mono.empty(), "Final step"))
        .build();

    StepVerifier.create(sequentialFlow.run(FlowContext.create()))
        .assertNext(assertAndLog(globalReport -> {
          assertThat(globalReport.getName()).isEqualTo("Test");
          assertThat(globalReport.getContext().get("Step 1")).isEqualTo("Step 1");
          assertThat(globalReport.getContext().get("Step 2")).isEqualTo("Step 2");
          assertThat(globalReport.getContext().get("Step 3")).isEqualTo("Step 3");
          assertThat(globalReport.getStatus()).isEqualTo(Status.ERROR);
          assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          assertThat(globalReport.getAllErrors()).hasSize(1);
          assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  final void givenSuccessSequenceWithFinallyNull_sequentialFlow_shouldError() {
    SequentialFlow<FlowContext> sequentialFlow = SequentialFlowBuilder
        .defaultBuilder()
        .named("Test")
        .then(SuccessStepFlow.flowNamed("Step 1"))
        .then(SuccessStepFlow.flowNamed("Step 2"))
        .then(SuccessStepFlow.flowNamed("Step 3"))
        .doFinally(StepWithMetadata.build((context, metadata) -> null, "Final step"))
        .build();

    StepVerifier.create(sequentialFlow.run(FlowContext.create()))
        .assertNext(assertAndLog(globalReport -> {
          assertThat(globalReport.getName()).isEqualTo("Test");
          assertThat(globalReport.getContext().get("Step 1")).isEqualTo("Step 1");
          assertThat(globalReport.getContext().get("Step 2")).isEqualTo("Step 2");
          assertThat(globalReport.getContext().get("Step 3")).isEqualTo("Step 3");
          assertThat(globalReport.getStatus()).isEqualTo(Status.ERROR);
          assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          assertThat(globalReport.getAllErrors()).hasSize(1);
          assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  final void givenSequenceWithWarningAndErrorWithFinally_sequentialFlow_shouldError() {
    SequentialFlow<FlowContext> sequentialFlow = SequentialFlowBuilder
        .defaultBuilder()
        .named("Test")
        .then(WarningStepFlow.flowNamed("Step 1"))
        .then(ErrorRawStepFlow.flowNamed("Step 2"))
        .then(SuccessStepFlow.flowNamed("Step 3"))
        .doFinally(SuccessStepFlow.flowNamed("Final step"))
        .build();

    StepVerifier.create(sequentialFlow.run(FlowContext.create()))
        .assertNext(assertAndLog(globalReport -> {
          assertThat(globalReport.getName()).isEqualTo("Test");
          assertThat(globalReport.getContext().get("Step 1")).isEqualTo("Step 1");
          assertThat(globalReport.getContext().get("Final step")).isEqualTo("Final step");
          assertThat(globalReport.getStatus()).isEqualTo(Status.ERROR);
          assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          assertThat(globalReport.getAllErrors()).hasSize(1);
          assertThat(globalReport.getAllWarnings()).hasSize(1);
        }))
        .verifyComplete();
  }

  @Test
  final void givenNullName_sequentialFlow_shouldNotBuild() {
    assertThatExceptionOfType(FlowBuilderException.class).isThrownBy(() -> SequentialFlowBuilder
        .defaultBuilder()
        .named(null)
        .then(SuccessStepFlow.flowNamed("Step"))
        .doFinally(SuccessStepFlow.flowNamed("Finally"))
        .build()
    );
  }

  @Test
  final void givenNullThen_sequentialFlow_shouldNotBuild() {
    assertThatExceptionOfType(FlowBuilderException.class).isThrownBy(() -> SequentialFlowBuilder
        .defaultBuilder()
        .named("Test")
        .then(null)
        .doFinally(SuccessStepFlow.flowNamed("Finally"))
        .build()
    );
  }

  @Test
  final void givenNullFinally_sequentialFlow_shouldNotBuild() {
    assertThatExceptionOfType(FlowBuilderException.class).isThrownBy(() -> SequentialFlowBuilder
        .defaultBuilder()
        .named("Test")
        .then(SuccessStepFlow.flowNamed("Step"))
        .doFinally(null)
        .build()
    );
  }
}
