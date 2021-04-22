package fr.jtools.reactorflow.flow;

import fr.jtools.reactorflow.builder.ParallelFlowBuilder;
import fr.jtools.reactorflow.exception.FlowBuilderException;
import fr.jtools.reactorflow.report.FlowContext;
import fr.jtools.reactorflow.report.Status;
import fr.jtools.reactorflow.testutils.CustomContext;
import fr.jtools.reactorflow.testutils.ErrorRawStepFlow;
import fr.jtools.reactorflow.testutils.SuccessStepFlow;
import fr.jtools.reactorflow.testutils.WarningStepFlow;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static fr.jtools.reactorflow.testutils.TestUtils.assertAndLog;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

final class ParallelFlowTest {
  @Test
  final void givenOneSuccessFlow_parallelFlow_shouldSuccess() {
    ParallelFlow<FlowContext, Object> parallelFlow = ParallelFlowBuilder
        .defaultBuilder()
        .named("Test")
        .parallelize(Collections.singletonList(SuccessStepFlow.flowNamed("Parallel")))
        .mergeStrategy(ParallelFlowBuilder.defaultMergeStrategy())
        .build();

    StepVerifier
        .create(parallelFlow.run(FlowContext.create()))
        .assertNext(assertAndLog(globalReport -> {
          assertThat(globalReport.getName()).isEqualTo("Test");
          assertThat(globalReport.getContext().get("Parallel")).isEqualTo("Parallel");
          assertThat(globalReport.getStatus()).isEqualTo(Status.SUCCESS);
          assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          assertThat(globalReport.getAllErrors()).isEmpty();
          assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  final void givenOneWarningFlow_parallelFlow_shouldWarning() {
    ParallelFlow<FlowContext, Object> parallelFlow = ParallelFlowBuilder
        .defaultBuilder()
        .named("Test")
        .parallelize(Collections.singletonList(WarningStepFlow.flowNamed("Parallel")))
        .mergeStrategy(ParallelFlowBuilder.defaultMergeStrategy())
        .build();

    StepVerifier
        .create(parallelFlow.run(FlowContext.create()))
        .assertNext(assertAndLog(globalReport -> {
          assertThat(globalReport.getName()).isEqualTo("Test");
          assertThat(globalReport.getContext().get("Parallel")).isEqualTo("Parallel");
          assertThat(globalReport.getStatus()).isEqualTo(Status.WARNING);
          assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          assertThat(globalReport.getAllErrors()).isEmpty();
          assertThat(globalReport.getAllWarnings()).hasSize(1);
        }))
        .verifyComplete();
  }

  @Test
  final void givenOneErrorFlow_parallelFlow_shouldError() {
    ParallelFlow<FlowContext, Object> parallelFlow = ParallelFlowBuilder
        .defaultBuilder()
        .named("Test")
        .parallelize(Collections.singletonList(ErrorRawStepFlow.flowNamed("Parallel")))
        .mergeStrategy(ParallelFlowBuilder.defaultMergeStrategy())
        .build();

    StepVerifier
        .create(parallelFlow.run(FlowContext.create()))
        .assertNext(assertAndLog(globalReport -> {
          assertThat(globalReport.getName()).isEqualTo("Test");
          assertThat(globalReport.getContext().get("Parallel")).isNull();
          assertThat(globalReport.getStatus()).isEqualTo(Status.ERROR);
          assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          assertThat(globalReport.getAllErrors()).hasSize(1);
          assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  final void givenManyFlows_parallelFlow_shouldCheckResult() {
    ParallelFlow<FlowContext, Object> parallelFlow = ParallelFlowBuilder
        .defaultBuilder()
        .named("Test")
        .parallelize(List.of(ErrorRawStepFlow.flowNamed("Parallel"), WarningStepFlow.flowNamed("Parallel 2"), SuccessStepFlow.flowNamed("Parallel 3")))
        .mergeStrategy(ParallelFlowBuilder.defaultMergeStrategy())
        .build();

    StepVerifier
        .create(parallelFlow.run(FlowContext.create()))
        .assertNext(assertAndLog(globalReport -> {
          assertThat(globalReport.getName()).isEqualTo("Test");
          assertThat(globalReport.getContext().get("Parallel")).isNull();
          assertThat(globalReport.getContext().get("Parallel 2")).isEqualTo("Parallel 2");
          assertThat(globalReport.getContext().get("Parallel 3")).isEqualTo("Parallel 3");
          assertThat(globalReport.getStatus()).isEqualTo(Status.ERROR);
          assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          assertThat(globalReport.getAllErrors()).hasSize(1);
          assertThat(globalReport.getAllWarnings()).hasSize(1);
        }))
        .verifyComplete();
  }

  @Test
  final void givenAllSuccessFlows_parallelFlow_shouldSuccess() {
    ParallelFlow<FlowContext, Object> parallelFlow = ParallelFlowBuilder
        .defaultBuilder()
        .named("Test")
        .parallelize(List.of(SuccessStepFlow.flowNamed("Parallel"), SuccessStepFlow.flowNamed("Parallel 2"), SuccessStepFlow.flowNamed("Parallel 3")))
        .mergeStrategy(ParallelFlowBuilder.defaultMergeStrategy())
        .build();

    StepVerifier
        .create(parallelFlow.run(FlowContext.create()))
        .assertNext(assertAndLog(globalReport -> {
          assertThat(globalReport.getName()).isEqualTo("Test");
          assertThat(globalReport.getContext().get("Parallel")).isEqualTo("Parallel");
          assertThat(globalReport.getContext().get("Parallel 2")).isEqualTo("Parallel 2");
          assertThat(globalReport.getContext().get("Parallel 3")).isEqualTo("Parallel 3");
          assertThat(globalReport.getStatus()).isEqualTo(Status.SUCCESS);
          assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          assertThat(globalReport.getAllErrors()).isEmpty();
          assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  final void givenAllSuccessFlowsClonedWithNewName_parallelFlow_shouldSuccess() {
    ParallelFlow<FlowContext, Object> parallelFlow = ParallelFlowBuilder
        .defaultBuilder()
        .named("Test")
        .parallelize(List.of(SuccessStepFlow.flowNamed("Parallel"), SuccessStepFlow.flowNamed("Parallel 2"), SuccessStepFlow.flowNamed("Parallel 3")))
        .mergeStrategy(ParallelFlowBuilder.defaultMergeStrategy())
        .build();

    StepVerifier
        .create(parallelFlow.cloneFlow("Test copy").run(FlowContext.create()))
        .assertNext(assertAndLog(globalReport -> {
          assertThat(globalReport.getName()).isEqualTo("Test copy");
          assertThat(globalReport.getContext().get("Parallel")).isEqualTo("Parallel");
          assertThat(globalReport.getContext().get("Parallel 2")).isEqualTo("Parallel 2");
          assertThat(globalReport.getContext().get("Parallel 3")).isEqualTo("Parallel 3");
          assertThat(globalReport.getStatus()).isEqualTo(Status.SUCCESS);
          assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          assertThat(globalReport.getAllErrors()).isEmpty();
          assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  final void givenAllSuccessFlowsCloned_parallelFlow_shouldSuccess() {
    ParallelFlow<FlowContext, Object> parallelFlow = ParallelFlowBuilder
        .defaultBuilder()
        .named("Test")
        .parallelize(List.of(SuccessStepFlow.flowNamed("Parallel"), SuccessStepFlow.flowNamed("Parallel 2"), SuccessStepFlow.flowNamed("Parallel 3")))
        .mergeStrategy(ParallelFlowBuilder.defaultMergeStrategy())
        .build();

    StepVerifier
        .create(parallelFlow.cloneFlow().run(FlowContext.create()))
        .assertNext(assertAndLog(globalReport -> {
          assertThat(globalReport.getName()).isEqualTo("Test");
          assertThat(globalReport.getContext().get("Parallel")).isEqualTo("Parallel");
          assertThat(globalReport.getContext().get("Parallel 2")).isEqualTo("Parallel 2");
          assertThat(globalReport.getContext().get("Parallel 3")).isEqualTo("Parallel 3");
          assertThat(globalReport.getStatus()).isEqualTo(Status.SUCCESS);
          assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          assertThat(globalReport.getAllErrors()).isEmpty();
          assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  final void givenAllSuccessFlowsAndMergeStrategyError_parallelFlow_shouldSuccess() {
    ParallelFlow<FlowContext, Object> parallelFlow = ParallelFlowBuilder
        .defaultBuilder()
        .named("Test")
        .parallelize(List.of(SuccessStepFlow.flowNamed("Parallel"), SuccessStepFlow.flowNamed("Parallel 2"), SuccessStepFlow.flowNamed("Parallel 3")))
        .mergeStrategy((context, context2) -> {
          throw new RuntimeException("Merge strategy error");
        })
        .build();

    StepVerifier
        .create(parallelFlow.run(FlowContext.create()))
        .assertNext(assertAndLog(globalReport -> {
          assertThat(globalReport.getName()).isEqualTo("Test");
          assertThat(globalReport.getContext().get("Parallel")).isEqualTo("Parallel");
          assertThat(globalReport.getContext().get("Parallel 2")).isEqualTo("Parallel 2");
          assertThat(globalReport.getContext().get("Parallel 3")).isEqualTo("Parallel 3");
          assertThat(globalReport.getStatus()).isEqualTo(Status.ERROR);
          assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          assertThat(globalReport.getAllErrors()).hasSize(1);
          assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  @SuppressWarnings("unchecked")
  final void givenAllSuccessFlowsOnArray_parallelFlow_shouldSuccess() {
    ParallelFlow<FlowContext, Object> parallelFlow = ParallelFlowBuilder
        .defaultBuilder()
        .named("Test")
        .parallelizeFromArray(context -> (List<Object>) context.get("Iterable"))
        .parallelizedFlow(SuccessStepFlow.flowNamed("Parallel"))
        .mergeStrategy(ParallelFlowBuilder.defaultMergeStrategy())
        .build();

    StepVerifier
        .create(parallelFlow.run(FlowContext.createFrom(Map.of("Iterable", List.of("1", "2", "3")))))
        .assertNext(assertAndLog(globalReport -> {
          assertThat(globalReport.getName()).isEqualTo("Test");
          assertThat(globalReport.getContext().get("Parallel")).isEqualTo("Parallel");
          assertThat(globalReport.getStatus()).isEqualTo(Status.SUCCESS);
          assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          assertThat(globalReport.getAllErrors()).isEmpty();
          assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  @SuppressWarnings("unchecked")
  final void givenAllSuccessFlowsOnArrayCustomMetadata_parallelFlow_shouldSuccess() {
    ParallelFlow<FlowContext, Date> parallelFlow = ParallelFlowBuilder
        .builderForMetadataType(Date.class)
        .named("Test")
        .parallelizeFromArray(context -> (List<Date>) context.get("Iterable"))
        .parallelizedFlow(SuccessStepFlow.flowNamed("Parallel"))
        .mergeStrategy(ParallelFlowBuilder.defaultMergeStrategy())
        .build();

    StepVerifier
        .create(parallelFlow.run(FlowContext.createFrom(Map.of("Iterable", List.of(new Date(), new Date(), new Date())))))
        .assertNext(assertAndLog(globalReport -> {
          assertThat(globalReport.getName()).isEqualTo("Test");
          assertThat(globalReport.getContext().get("Parallel")).isEqualTo("Parallel");
          assertThat(globalReport.getStatus()).isEqualTo(Status.SUCCESS);
          assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          assertThat(globalReport.getAllErrors()).isEmpty();
          assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  @SuppressWarnings("unchecked")
  final void givenAllSuccessFlowsOnArrayCustomMetadataAndCustomContext_parallelFlow_shouldSuccess() {
    ParallelFlow<CustomContext, Date> parallelFlow = ParallelFlowBuilder
        .builderForTypes(CustomContext.class, Date.class)
        .named("Test")
        .parallelizeFromArray(context -> (List<Date>) context.get("Iterable"))
        .parallelizedFlow(SuccessStepFlow.flowNamed("Parallel"))
        .mergeStrategy(ParallelFlowBuilder.defaultMergeStrategy())
        .build();

    StepVerifier
        .create(parallelFlow.run(CustomContext.createFrom(Map.of("Iterable", List.of(new Date(), new Date(), new Date())))))
        .assertNext(assertAndLog(globalReport -> {
          assertThat(globalReport.getName()).isEqualTo("Test");
          assertThat(globalReport.getContext().get("Parallel")).isEqualTo("Parallel");
          assertThat(globalReport.getStatus()).isEqualTo(Status.SUCCESS);
          assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          assertThat(globalReport.getAllErrors()).isEmpty();
          assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  @SuppressWarnings("unchecked")
  final void givenAllSuccessFlowsOnArrayCustomContext_parallelFlow_shouldSuccess() {
    ParallelFlow<CustomContext, Object> parallelFlow = ParallelFlowBuilder
        .builderForContextOfType(CustomContext.class)
        .named("Test")
        .parallelizeFromArray(context -> (List<Object>) context.get("Iterable"))
        .parallelizedFlow(SuccessStepFlow.flowNamed("Parallel"))
        .mergeStrategy(ParallelFlowBuilder.defaultMergeStrategy())
        .build();

    StepVerifier
        .create(parallelFlow.run(CustomContext.createFrom(Map.of("Iterable", List.of("1", "2", "3")))))
        .assertNext(assertAndLog(globalReport -> {
          assertThat(globalReport.getName()).isEqualTo("Test");
          assertThat(globalReport.getContext().get("Parallel")).isEqualTo("Parallel");
          assertThat(globalReport.getStatus()).isEqualTo(Status.SUCCESS);
          assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          assertThat(globalReport.getAllErrors()).isEmpty();
          assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  @SuppressWarnings("unchecked")
  final void givenAllSuccessFlowsOnArrayAndCastErrorOnArray_parallelFlow_shouldError() {
    ParallelFlow<FlowContext, Object> parallelFlow = ParallelFlowBuilder
        .defaultBuilder()
        .named("Test")
        .parallelizeFromArray(context -> (List<Object>) context.get("Iterable"))
        .parallelizedFlow(SuccessStepFlow.flowNamed("Parallel"))
        .mergeStrategy(ParallelFlowBuilder.defaultMergeStrategy())
        .build();

    StepVerifier
        .create(parallelFlow.run(FlowContext.createFrom(Map.of("Iterable", "Non iterable"))))
        .assertNext(assertAndLog(globalReport -> {
          assertThat(globalReport.getName()).isEqualTo("Test");
          assertThat(globalReport.getContext().get("Parallel")).isNull();
          assertThat(globalReport.getStatus()).isEqualTo(Status.ERROR);
          assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          assertThat(globalReport.getAllErrors()).hasSize(1);
          assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  final void givenAllSuccessFlowsOnArrayAndErrorOnArray_parallelFlow_shouldError() {
    ParallelFlow<FlowContext, Object> parallelFlow = ParallelFlowBuilder
        .defaultBuilder()
        .named("Test")
        .parallelizeFromArray(context -> {
          throw new RuntimeException("Array error");
        })
        .parallelizedFlow(SuccessStepFlow.flowNamed("Parallel"))
        .mergeStrategy(ParallelFlowBuilder.defaultMergeStrategy())
        .build();

    StepVerifier
        .create(parallelFlow.run(FlowContext.create()))
        .assertNext(assertAndLog(globalReport -> {
          assertThat(globalReport.getName()).isEqualTo("Test");
          assertThat(globalReport.getContext().get("Parallel")).isNull();
          assertThat(globalReport.getStatus()).isEqualTo(Status.ERROR);
          assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          assertThat(globalReport.getAllErrors()).hasSize(1);
          assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  @SuppressWarnings("unchecked")
  final void givenAllSuccessFlowsOnArrayClonedWithNewName_parallelFlow_shouldSuccess() {
    ParallelFlow<FlowContext, Object> parallelFlow = ParallelFlowBuilder
        .defaultBuilder()
        .named("Test")
        .parallelizeFromArray(context -> (List<Object>) context.get("Iterable"))
        .parallelizedFlow(SuccessStepFlow.flowNamed("Parallel"))
        .mergeStrategy(ParallelFlowBuilder.defaultMergeStrategy())
        .build();

    StepVerifier
        .create(parallelFlow.cloneFlow("Test copy").run(FlowContext.createFrom(Map.of("Iterable", List.of("1", "2", "3")))))
        .assertNext(assertAndLog(globalReport -> {
          assertThat(globalReport.getName()).isEqualTo("Test copy");
          assertThat(globalReport.getContext().get("Parallel")).isEqualTo("Parallel");
          assertThat(globalReport.getStatus()).isEqualTo(Status.SUCCESS);
          assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          assertThat(globalReport.getAllErrors()).isEmpty();
          assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  @SuppressWarnings("unchecked")
  final void givenAllSuccessFlowsOnArrayCloned_parallelFlow_shouldSuccess() {
    ParallelFlow<FlowContext, Object> parallelFlow = ParallelFlowBuilder
        .defaultBuilder()
        .named("Test")
        .parallelizeFromArray(context -> (List<Object>) context.get("Iterable"))
        .parallelizedFlow(SuccessStepFlow.flowNamed("Parallel"))
        .mergeStrategy(ParallelFlowBuilder.defaultMergeStrategy())
        .build();

    StepVerifier
        .create(parallelFlow.cloneFlow().run(FlowContext.createFrom(Map.of("Iterable", List.of("1", "2", "3")))))
        .assertNext(assertAndLog(globalReport -> {
          assertThat(globalReport.getName()).isEqualTo("Test");
          assertThat(globalReport.getContext().get("Parallel")).isEqualTo("Parallel");
          assertThat(globalReport.getStatus()).isEqualTo(Status.SUCCESS);
          assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          assertThat(globalReport.getAllErrors()).isEmpty();
          assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  final void givenNullName_parallelFlow_shouldNotBuild() {
    assertThatExceptionOfType(FlowBuilderException.class).isThrownBy(() -> ParallelFlowBuilder
        .defaultBuilder()
        .named(null)
        .parallelize(Collections.singletonList(SuccessStepFlow.flowNamed("Test")))
        .mergeStrategy(ParallelFlowBuilder.defaultMergeStrategy())
        .build()
    );
  }

  @Test
  final void givenNullMergeStrategy_parallelFlow_shouldNotBuild() {
    assertThatExceptionOfType(FlowBuilderException.class).isThrownBy(() -> ParallelFlowBuilder
        .defaultBuilder()
        .named("Parallel")
        .parallelize(Collections.singletonList(SuccessStepFlow.flowNamed("Test")))
        .mergeStrategy(null)
        .build()
    );
  }

  @Test
  final void givenNullParallelizeFromArray_parallelFlow_shouldNotBuild() {
    assertThatExceptionOfType(FlowBuilderException.class).isThrownBy(() -> ParallelFlowBuilder
        .defaultBuilder()
        .named("Parallel")
        .parallelizeFromArray(null)
        .parallelizedFlow(SuccessStepFlow.flowNamed("Test"))
        .mergeStrategy(ParallelFlowBuilder.defaultMergeStrategy())
        .build()
    );
  }

  @Test
  final void givenNullParallelizedFlow_parallelFlow_shouldNotBuild() {
    assertThatExceptionOfType(FlowBuilderException.class).isThrownBy(() -> ParallelFlowBuilder
        .defaultBuilder()
        .named("Parallel")
        .parallelizeFromArray(context -> Collections.emptyList())
        .parallelizedFlow(null)
        .mergeStrategy(ParallelFlowBuilder.defaultMergeStrategy())
        .build()
    );
  }
}
