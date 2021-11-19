package io.github.juliengalet.reactorflow.flow;

import io.github.juliengalet.reactorflow.builder.ParallelFlowBuilder;
import io.github.juliengalet.reactorflow.exception.FlowBuilderException;
import io.github.juliengalet.reactorflow.report.FlowContext;
import io.github.juliengalet.reactorflow.report.Status;
import io.github.juliengalet.reactorflow.testutils.CustomContext;
import io.github.juliengalet.reactorflow.testutils.ErrorRawStepFlow;
import io.github.juliengalet.reactorflow.testutils.SuccessStepFlow;
import io.github.juliengalet.reactorflow.testutils.WarningStepFlow;
import io.github.juliengalet.reactorflow.testutils.TestUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

final class ParallelFlowTest {
  @Test
  void givenOneSuccessFlow_parallelFlow_shouldSuccess() {
    ParallelFlow<FlowContext, Object> parallelFlow = ParallelFlowBuilder
        .defaultBuilder()
        .named("Test")
        .parallelize(Collections.singletonList(SuccessStepFlow.flowNamed("Parallel")))
        .mergeStrategy(ParallelFlowBuilder.defaultMergeStrategy())
        .build();

    StepVerifier
        .create(parallelFlow.run(FlowContext.create()))
        .assertNext(TestUtils.assertAndLog(globalReport -> {
          Assertions.assertThat(globalReport.getName()).isEqualTo("Test");
          Assertions.assertThat(globalReport.getContext().get("Parallel")).isEqualTo("Parallel");
          assertThat(globalReport.getStatus()).isEqualTo(Status.SUCCESS);
          Assertions.assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          Assertions.assertThat(globalReport.getAllErrors()).isEmpty();
          Assertions.assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  void givenOneWarningFlow_parallelFlow_shouldWarning() {
    ParallelFlow<FlowContext, Object> parallelFlow = ParallelFlowBuilder
        .defaultBuilder()
        .named("Test")
        .parallelize(Collections.singletonList(WarningStepFlow.flowNamed("Parallel")))
        .mergeStrategy(ParallelFlowBuilder.defaultMergeStrategy())
        .build();

    StepVerifier
        .create(parallelFlow.run(FlowContext.create()))
        .assertNext(TestUtils.assertAndLog(globalReport -> {
          Assertions.assertThat(globalReport.getName()).isEqualTo("Test");
          Assertions.assertThat(globalReport.getContext().get("Parallel")).isEqualTo("Parallel");
          assertThat(globalReport.getStatus()).isEqualTo(Status.WARNING);
          Assertions.assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          Assertions.assertThat(globalReport.getAllErrors()).isEmpty();
          Assertions.assertThat(globalReport.getAllWarnings()).hasSize(1);
        }))
        .verifyComplete();
  }

  @Test
  void givenOneErrorFlow_parallelFlow_shouldError() {
    ParallelFlow<FlowContext, Object> parallelFlow = ParallelFlowBuilder
        .defaultBuilder()
        .named("Test")
        .parallelize(Collections.singletonList(ErrorRawStepFlow.flowNamed("Parallel")))
        .mergeStrategy(ParallelFlowBuilder.defaultMergeStrategy())
        .build();

    StepVerifier
        .create(parallelFlow.run(FlowContext.create()))
        .assertNext(TestUtils.assertAndLog(globalReport -> {
          Assertions.assertThat(globalReport.getName()).isEqualTo("Test");
          Assertions.assertThat(globalReport.getContext().get("Parallel")).isNull();
          assertThat(globalReport.getStatus()).isEqualTo(Status.ERROR);
          Assertions.assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          Assertions.assertThat(globalReport.getAllErrors()).hasSize(1);
          Assertions.assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  void givenManyFlows_parallelFlow_shouldCheckResult() {
    ParallelFlow<FlowContext, Object> parallelFlow = ParallelFlowBuilder
        .defaultBuilder()
        .named("Test")
        .parallelize(List.of(ErrorRawStepFlow.flowNamed("Parallel"), WarningStepFlow.flowNamed("Parallel 2"), SuccessStepFlow.flowNamed("Parallel 3")))
        .mergeStrategy(ParallelFlowBuilder.defaultMergeStrategy())
        .build();

    StepVerifier
        .create(parallelFlow.run(FlowContext.create()))
        .assertNext(TestUtils.assertAndLog(globalReport -> {
          Assertions.assertThat(globalReport.getName()).isEqualTo("Test");
          Assertions.assertThat(globalReport.getContext().get("Parallel")).isNull();
          Assertions.assertThat(globalReport.getContext().get("Parallel 2")).isEqualTo("Parallel 2");
          Assertions.assertThat(globalReport.getContext().get("Parallel 3")).isEqualTo("Parallel 3");
          assertThat(globalReport.getStatus()).isEqualTo(Status.ERROR);
          Assertions.assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          Assertions.assertThat(globalReport.getAllErrors()).hasSize(1);
          Assertions.assertThat(globalReport.getAllWarnings()).hasSize(1);
        }))
        .verifyComplete();
  }

  @Test
  void givenAllSuccessFlows_parallelFlow_shouldSuccess() {
    ParallelFlow<FlowContext, Object> parallelFlow = ParallelFlowBuilder
        .defaultBuilder()
        .named("Test")
        .parallelize(List.of(SuccessStepFlow.flowNamed("Parallel"), SuccessStepFlow.flowNamed("Parallel 2"), SuccessStepFlow.flowNamed("Parallel 3")))
        .mergeStrategy(ParallelFlowBuilder.defaultMergeStrategy())
        .build();

    StepVerifier
        .create(parallelFlow.run(FlowContext.create()))
        .assertNext(TestUtils.assertAndLog(globalReport -> {
          Assertions.assertThat(globalReport.getName()).isEqualTo("Test");
          Assertions.assertThat(globalReport.getContext().get("Parallel")).isEqualTo("Parallel");
          Assertions.assertThat(globalReport.getContext().get("Parallel 2")).isEqualTo("Parallel 2");
          Assertions.assertThat(globalReport.getContext().get("Parallel 3")).isEqualTo("Parallel 3");
          assertThat(globalReport.getStatus()).isEqualTo(Status.SUCCESS);
          Assertions.assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          Assertions.assertThat(globalReport.getAllErrors()).isEmpty();
          Assertions.assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  void givenAllSuccessFlowsClonedWithNewName_parallelFlow_shouldSuccess() {
    ParallelFlow<FlowContext, Object> parallelFlow = ParallelFlowBuilder
        .defaultBuilder()
        .named("Test")
        .parallelize(List.of(SuccessStepFlow.flowNamed("Parallel"), SuccessStepFlow.flowNamed("Parallel 2"), SuccessStepFlow.flowNamed("Parallel 3")))
        .mergeStrategy(ParallelFlowBuilder.defaultMergeStrategy())
        .build();

    StepVerifier
        .create(parallelFlow.cloneFlow("Test copy").run(FlowContext.create()))
        .assertNext(TestUtils.assertAndLog(globalReport -> {
          Assertions.assertThat(globalReport.getName()).isEqualTo("Test copy");
          Assertions.assertThat(globalReport.getContext().get("Parallel")).isEqualTo("Parallel");
          Assertions.assertThat(globalReport.getContext().get("Parallel 2")).isEqualTo("Parallel 2");
          Assertions.assertThat(globalReport.getContext().get("Parallel 3")).isEqualTo("Parallel 3");
          assertThat(globalReport.getStatus()).isEqualTo(Status.SUCCESS);
          Assertions.assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          Assertions.assertThat(globalReport.getAllErrors()).isEmpty();
          Assertions.assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  void givenAllSuccessFlowsCloned_parallelFlow_shouldSuccess() {
    ParallelFlow<FlowContext, Object> parallelFlow = ParallelFlowBuilder
        .defaultBuilder()
        .named("Test")
        .parallelize(List.of(SuccessStepFlow.flowNamed("Parallel"), SuccessStepFlow.flowNamed("Parallel 2"), SuccessStepFlow.flowNamed("Parallel 3")))
        .mergeStrategy(ParallelFlowBuilder.defaultMergeStrategy())
        .build();

    StepVerifier
        .create(parallelFlow.cloneFlow().run(FlowContext.create()))
        .assertNext(TestUtils.assertAndLog(globalReport -> {
          Assertions.assertThat(globalReport.getName()).isEqualTo("Test");
          Assertions.assertThat(globalReport.getContext().get("Parallel")).isEqualTo("Parallel");
          Assertions.assertThat(globalReport.getContext().get("Parallel 2")).isEqualTo("Parallel 2");
          Assertions.assertThat(globalReport.getContext().get("Parallel 3")).isEqualTo("Parallel 3");
          assertThat(globalReport.getStatus()).isEqualTo(Status.SUCCESS);
          Assertions.assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          Assertions.assertThat(globalReport.getAllErrors()).isEmpty();
          Assertions.assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  void givenAllSuccessFlowsAndMergeStrategyError_parallelFlow_shouldSuccess() {
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
        .assertNext(TestUtils.assertAndLog(globalReport -> {
          Assertions.assertThat(globalReport.getName()).isEqualTo("Test");
          Assertions.assertThat(globalReport.getContext().get("Parallel")).isEqualTo("Parallel");
          Assertions.assertThat(globalReport.getContext().get("Parallel 2")).isEqualTo("Parallel 2");
          Assertions.assertThat(globalReport.getContext().get("Parallel 3")).isEqualTo("Parallel 3");
          assertThat(globalReport.getStatus()).isEqualTo(Status.ERROR);
          Assertions.assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          Assertions.assertThat(globalReport.getAllErrors()).hasSize(1);
          Assertions.assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  @SuppressWarnings("unchecked")
  void givenAllSuccessFlowsOnArray_parallelFlow_shouldSuccess() {
    ParallelFlow<FlowContext, Object> parallelFlow = ParallelFlowBuilder
        .defaultBuilder()
        .named("Test")
        .parallelizeFromArray(context -> (List<Object>) context.get("Iterable"))
        .parallelizedFlow(SuccessStepFlow.flowNamed("Parallel"))
        .mergeStrategy(ParallelFlowBuilder.defaultMergeStrategy())
        .build();

    StepVerifier
        .create(parallelFlow.run(FlowContext.createFrom(Map.of("Iterable", List.of("1", "2", "3")))))
        .assertNext(TestUtils.assertAndLog(globalReport -> {
          Assertions.assertThat(globalReport.getName()).isEqualTo("Test");
          Assertions.assertThat(globalReport.getContext().get("Parallel")).isEqualTo("Parallel");
          assertThat(globalReport.getStatus()).isEqualTo(Status.SUCCESS);
          Assertions.assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          Assertions.assertThat(globalReport.getAllErrors()).isEmpty();
          Assertions.assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  @SuppressWarnings("unchecked")
  void givenAllSuccessFlowsOnArrayCustomMetadata_parallelFlow_shouldSuccess() {
    ParallelFlow<FlowContext, Date> parallelFlow = ParallelFlowBuilder
        .builderForMetadataType(Date.class)
        .named("Test")
        .parallelizeFromArray(context -> (List<Date>) context.get("Iterable"))
        .parallelizedFlow(SuccessStepFlow.flowNamed("Parallel"))
        .mergeStrategy(ParallelFlowBuilder.defaultMergeStrategy())
        .build();

    StepVerifier
        .create(parallelFlow.run(FlowContext.createFrom(Map.of("Iterable", List.of(new Date(), new Date(), new Date())))))
        .assertNext(TestUtils.assertAndLog(globalReport -> {
          Assertions.assertThat(globalReport.getName()).isEqualTo("Test");
          Assertions.assertThat(globalReport.getContext().get("Parallel")).isEqualTo("Parallel");
          assertThat(globalReport.getStatus()).isEqualTo(Status.SUCCESS);
          Assertions.assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          Assertions.assertThat(globalReport.getAllErrors()).isEmpty();
          Assertions.assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  @SuppressWarnings("unchecked")
  void givenAllSuccessFlowsOnArrayCustomMetadataAndCustomContext_parallelFlow_shouldSuccess() {
    ParallelFlow<CustomContext, Date> parallelFlow = ParallelFlowBuilder
        .builderForTypes(CustomContext.class, Date.class)
        .named("Test")
        .parallelizeFromArray(context -> (List<Date>) context.get("Iterable"))
        .parallelizedFlow(SuccessStepFlow.flowNamed("Parallel"))
        .mergeStrategy(ParallelFlowBuilder.defaultMergeStrategy())
        .build();

    StepVerifier
        .create(parallelFlow.run(CustomContext.createFrom(Map.of("Iterable", List.of(new Date(), new Date(), new Date())))))
        .assertNext(TestUtils.assertAndLog(globalReport -> {
          Assertions.assertThat(globalReport.getName()).isEqualTo("Test");
          Assertions.assertThat(globalReport.getContext().get("Parallel")).isEqualTo("Parallel");
          assertThat(globalReport.getStatus()).isEqualTo(Status.SUCCESS);
          Assertions.assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          Assertions.assertThat(globalReport.getAllErrors()).isEmpty();
          Assertions.assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  @SuppressWarnings("unchecked")
  void givenAllSuccessFlowsOnArrayCustomContext_parallelFlow_shouldSuccess() {
    ParallelFlow<CustomContext, Object> parallelFlow = ParallelFlowBuilder
        .builderForContextOfType(CustomContext.class)
        .named("Test")
        .parallelizeFromArray(context -> (List<Object>) context.get("Iterable"))
        .parallelizedFlow(SuccessStepFlow.flowNamed("Parallel"))
        .mergeStrategy(ParallelFlowBuilder.defaultMergeStrategy())
        .build();

    StepVerifier
        .create(parallelFlow.run(CustomContext.createFrom(Map.of("Iterable", List.of("1", "2", "3")))))
        .assertNext(TestUtils.assertAndLog(globalReport -> {
          Assertions.assertThat(globalReport.getName()).isEqualTo("Test");
          Assertions.assertThat(globalReport.getContext().get("Parallel")).isEqualTo("Parallel");
          assertThat(globalReport.getStatus()).isEqualTo(Status.SUCCESS);
          Assertions.assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          Assertions.assertThat(globalReport.getAllErrors()).isEmpty();
          Assertions.assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  @SuppressWarnings("unchecked")
  void givenAllSuccessFlowsOnArrayAndCastErrorOnArray_parallelFlow_shouldError() {
    ParallelFlow<FlowContext, Object> parallelFlow = ParallelFlowBuilder
        .defaultBuilder()
        .named("Test")
        .parallelizeFromArray(context -> (List<Object>) context.get("Iterable"))
        .parallelizedFlow(SuccessStepFlow.flowNamed("Parallel"))
        .mergeStrategy(ParallelFlowBuilder.defaultMergeStrategy())
        .build();

    StepVerifier
        .create(parallelFlow.run(FlowContext.createFrom(Map.of("Iterable", "Non iterable"))))
        .assertNext(TestUtils.assertAndLog(globalReport -> {
          Assertions.assertThat(globalReport.getName()).isEqualTo("Test");
          Assertions.assertThat(globalReport.getContext().get("Parallel")).isNull();
          assertThat(globalReport.getStatus()).isEqualTo(Status.ERROR);
          Assertions.assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          Assertions.assertThat(globalReport.getAllErrors()).hasSize(1);
          Assertions.assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  void givenAllSuccessFlowsOnArrayAndErrorOnArray_parallelFlow_shouldError() {
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
        .assertNext(TestUtils.assertAndLog(globalReport -> {
          Assertions.assertThat(globalReport.getName()).isEqualTo("Test");
          Assertions.assertThat(globalReport.getContext().get("Parallel")).isNull();
          assertThat(globalReport.getStatus()).isEqualTo(Status.ERROR);
          Assertions.assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          Assertions.assertThat(globalReport.getAllErrors()).hasSize(1);
          Assertions.assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  @SuppressWarnings("unchecked")
  void givenAllSuccessFlowsOnArrayClonedWithNewName_parallelFlow_shouldSuccess() {
    ParallelFlow<FlowContext, Object> parallelFlow = ParallelFlowBuilder
        .defaultBuilder()
        .named("Test")
        .parallelizeFromArray(context -> (List<Object>) context.get("Iterable"))
        .parallelizedFlow(SuccessStepFlow.flowNamed("Parallel"))
        .mergeStrategy(ParallelFlowBuilder.defaultMergeStrategy())
        .build();

    StepVerifier
        .create(parallelFlow.cloneFlow("Test copy").run(FlowContext.createFrom(Map.of("Iterable", List.of("1", "2", "3")))))
        .assertNext(TestUtils.assertAndLog(globalReport -> {
          Assertions.assertThat(globalReport.getName()).isEqualTo("Test copy");
          Assertions.assertThat(globalReport.getContext().get("Parallel")).isEqualTo("Parallel");
          assertThat(globalReport.getStatus()).isEqualTo(Status.SUCCESS);
          Assertions.assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          Assertions.assertThat(globalReport.getAllErrors()).isEmpty();
          Assertions.assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  @SuppressWarnings("unchecked")
  void givenAllSuccessFlowsOnArrayCloned_parallelFlow_shouldSuccess() {
    ParallelFlow<FlowContext, Object> parallelFlow = ParallelFlowBuilder
        .defaultBuilder()
        .named("Test")
        .parallelizeFromArray(context -> (List<Object>) context.get("Iterable"))
        .parallelizedFlow(SuccessStepFlow.flowNamed("Parallel"))
        .mergeStrategy(ParallelFlowBuilder.defaultMergeStrategy())
        .build();

    StepVerifier
        .create(parallelFlow.cloneFlow().run(FlowContext.createFrom(Map.of("Iterable", List.of("1", "2", "3")))))
        .assertNext(TestUtils.assertAndLog(globalReport -> {
          Assertions.assertThat(globalReport.getName()).isEqualTo("Test");
          Assertions.assertThat(globalReport.getContext().get("Parallel")).isEqualTo("Parallel");
          assertThat(globalReport.getStatus()).isEqualTo(Status.SUCCESS);
          Assertions.assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          Assertions.assertThat(globalReport.getAllErrors()).isEmpty();
          Assertions.assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  void givenNullName_parallelFlow_shouldNotBuild() {
    assertThatExceptionOfType(FlowBuilderException.class).isThrownBy(() -> ParallelFlowBuilder
        .defaultBuilder()
        .named(null)
        .parallelize(Collections.singletonList(SuccessStepFlow.flowNamed("Test")))
        .mergeStrategy(ParallelFlowBuilder.defaultMergeStrategy())
        .build()
    );
  }

  @Test
  void givenNullParallelize_parallelFlow_shouldNotBuild() {
    assertThatExceptionOfType(FlowBuilderException.class).isThrownBy(() -> ParallelFlowBuilder
        .defaultBuilder()
        .named("Parallel")
        .parallelize(null)
        .mergeStrategy(ParallelFlowBuilder.defaultMergeStrategy())
        .build()
    );
  }

  @Test
  void givenNullMergeStrategy_parallelFlow_shouldNotBuild() {
    assertThatExceptionOfType(FlowBuilderException.class).isThrownBy(() -> ParallelFlowBuilder
        .defaultBuilder()
        .named("Parallel")
        .parallelize(Collections.singletonList(SuccessStepFlow.flowNamed("Test")))
        .mergeStrategy(null)
        .build()
    );
  }

  @Test
  void givenNullParallelizeFromArray_parallelFlow_shouldNotBuild() {
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
  void givenNullParallelizedFlow_parallelFlow_shouldNotBuild() {
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
