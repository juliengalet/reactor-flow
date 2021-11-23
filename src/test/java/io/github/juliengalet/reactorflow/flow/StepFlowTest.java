package io.github.juliengalet.reactorflow.flow;

import io.github.juliengalet.reactorflow.builder.ParallelFlowBuilder;
import io.github.juliengalet.reactorflow.builder.StepFlowBuilder;
import io.github.juliengalet.reactorflow.exception.FlowBuilderException;
import io.github.juliengalet.reactorflow.exception.FlowExceptionType;
import io.github.juliengalet.reactorflow.exception.FlowFunctionalException;
import io.github.juliengalet.reactorflow.exception.FlowTechnicalException;
import io.github.juliengalet.reactorflow.report.FlowContext;
import io.github.juliengalet.reactorflow.report.Metadata;
import io.github.juliengalet.reactorflow.report.Report;
import io.github.juliengalet.reactorflow.report.Status;
import io.github.juliengalet.reactorflow.testutils.CustomContext;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.StreamTokenizer;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static io.github.juliengalet.reactorflow.testutils.TestUtils.assertAndLog;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

final class StepFlowTest {
  @Test
  void givenNullName_stepFlow_shouldNotBuild() {
    assertThatExceptionOfType(FlowBuilderException.class).isThrownBy(() -> StepFlowBuilder
        .defaultBuilder()
        .named(null)
        .execution((context, metadata) -> Mono.just(Report.success(context)))
        .build()
    );
  }

  @Test
  void givenNullExecution_stepFlow_shouldNotBuild() {
    assertThatExceptionOfType(FlowBuilderException.class).isThrownBy(() -> StepFlowBuilder
        .defaultBuilder()
        .named("Test")
        .execution(null)
        .build()
    );
  }

  @Test
  void givenSuccessLambdaExecution_stepFlow_shouldSuccess() {
    StepFlow<FlowContext, Object> stepFlow = StepFlowBuilder
        .defaultBuilder()
        .named("Test")
        .execution((context, metadata) -> {
          context.put("Test", "Test");
          return Mono.just(Report.success(context));
        })
        .build();

    StepVerifier
        .create(stepFlow.run(FlowContext.create()))
        .assertNext(globalReport -> {
          assertThat(globalReport.getStatus()).isEqualTo(Status.SUCCESS);
          assertThat(globalReport.getContext().get("Test")).isEqualTo("Test");
          assertThat(globalReport.getName()).isEqualTo("Test");
          assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          assertThat(globalReport.getAllErrors()).isEmpty();
          assertThat(globalReport.getAllWarnings()).isEmpty();
        })
        .verifyComplete();
  }

  @Test
  void givenClonedWithNewNameSuccessLambdaExecution_stepFlow_shouldSuccess() {
    StepFlow<FlowContext, Object> stepFlow = StepFlowBuilder
        .defaultBuilder()
        .named("Test")
        .execution((context, metadata) -> {
          context.put("Test", "Test");
          return Mono.just(Report.success(context));
        })
        .build();

    StepVerifier
        .create(stepFlow.cloneFlow("Test cloned").run(FlowContext.create()))
        .assertNext(globalReport -> {
          assertThat(globalReport.getStatus()).isEqualTo(Status.SUCCESS);
          assertThat(globalReport.getContext().get("Test")).isEqualTo("Test");
          assertThat(globalReport.getName()).isEqualTo("Test cloned");
          assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          assertThat(globalReport.getAllErrors()).isEmpty();
          assertThat(globalReport.getAllWarnings()).isEmpty();
        })
        .verifyComplete();
  }

  @Test
  void givenClonedSuccessLambdaExecution_stepFlow_shouldSuccess() {
    StepFlow<FlowContext, Object> stepFlow = StepFlowBuilder
        .defaultBuilder()
        .named("Test")
        .execution((context, metadata) -> {
          context.put("Test", "Test");
          return Mono.just(Report.success(context));
        })
        .build();

    StepVerifier
        .create(stepFlow.cloneFlow().run(FlowContext.create()))
        .assertNext(globalReport -> {
          assertThat(globalReport.getStatus()).isEqualTo(Status.SUCCESS);
          assertThat(globalReport.getContext().get("Test")).isEqualTo("Test");
          assertThat(globalReport.getName()).isEqualTo("Test");
          assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          assertThat(globalReport.getAllErrors()).isEmpty();
          assertThat(globalReport.getAllWarnings()).isEmpty();
        })
        .verifyComplete();
  }

  @Test
  void givenSuccessClassicExecution_stepFlow_shouldSuccess() {
    StepFlow<FlowContext, Object> stepFlow = StepFlowBuilder
        .defaultBuilder()
        .named("Test")
        .execution(new TestWork())
        .build();

    StepVerifier
        .create(stepFlow.run(FlowContext.create()))
        .assertNext(globalReport -> {
          assertThat(globalReport.getStatus()).isEqualTo(Status.SUCCESS);
          assertThat(globalReport.getContext().get("Test")).isEqualTo("Test");
          assertThat(globalReport.getName()).isEqualTo("Test");
          assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          assertThat(globalReport.getAllErrors()).isEmpty();
          assertThat(globalReport.getAllWarnings()).isEmpty();
        })
        .verifyComplete();
  }

  @Test
  void givenSuccessClassicExecutionExtendingDefaultMetadataStep_stepFlow_shouldSuccess() {
    StepFlow<FlowContext, Object> step = new DefaultStep<>().getStep();

    StepVerifier
        .create(step.run(FlowContext.create()))
        .assertNext(assertAndLog(globalReport -> assertThat(globalReport.getStatus()).isEqualTo(Status.SUCCESS)))
        .verifyComplete();
  }

  @Test
  void givenSuccessClassicExecutionExtendingDefaultStep_stepFlow_shouldSuccess() {
    StepFlow<FlowContext, Object> step = new DefaultMetadataStep<>().getStep();

    StepVerifier
        .create(step.run(FlowContext.create()))
        .assertNext(assertAndLog(globalReport -> assertThat(globalReport.getStatus()).isEqualTo(Status.SUCCESS)))
        .verifyComplete();
  }

  @Test
  void givenSuccessWithMonoContext_stepFlow_shouldSuccess() {
    StepFlow<FlowContext, Object> stepFlow = StepFlowBuilder
        .defaultBuilder()
        .named("Test")
        .execution(new TestWork())
        .build();

    StepVerifier
        .create(stepFlow.run(Mono.just(FlowContext.create())))
        .assertNext(globalReport -> {
          assertThat(globalReport.getStatus()).isEqualTo(Status.SUCCESS);
          assertThat(globalReport.getContext().get("Test")).isEqualTo("Test");
          assertThat(globalReport.getName()).isEqualTo("Test");
          assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          assertThat(globalReport.getAllErrors()).isEmpty();
          assertThat(globalReport.getAllWarnings()).isEmpty();
        })
        .verifyComplete();
  }

  @Test
  void givenSuccessWithSequentialFluxContext_stepFlow_shouldSuccess() {
    StepFlow<FlowContext, Object> stepFlow = StepFlowBuilder
        .defaultBuilder()
        .named("Test")
        .execution(new TestWork())
        .build();

    StepVerifier
        .create(stepFlow
            .runSequential(Flux.just(
                FlowContext.createFrom(Map.of("Init1", "Init1")),
                FlowContext.createFrom(Map.of("Init2", "Init2"))
            ))
        )
        .assertNext(globalReport -> {
          assertThat(globalReport.getStatus()).isEqualTo(Status.SUCCESS);
          assertThat(globalReport.getContext().get("Test")).isEqualTo("Test");
          assertThat(globalReport.getContext().get("Init2")).isNull();
          assertThat(globalReport.getContext().get("Init1")).isEqualTo("Init1");
          assertThat(globalReport.getName()).startsWith("Test");
          assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          assertThat(globalReport.getAllErrors()).isEmpty();
          assertThat(globalReport.getAllWarnings()).isEmpty();
        })
        .assertNext(globalReport -> {
          assertThat(globalReport.getStatus()).isEqualTo(Status.SUCCESS);
          assertThat(globalReport.getContext().get("Test")).isEqualTo("Test");
          assertThat(globalReport.getContext().get("Init1")).isNull();
          assertThat(globalReport.getContext().get("Init2")).isEqualTo("Init2");
          assertThat(globalReport.getName()).startsWith("Test");
          assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          assertThat(globalReport.getAllErrors()).isEmpty();
          assertThat(globalReport.getAllWarnings()).isEmpty();
        })
        .verifyComplete();
  }

  @Test
  void givenSuccessWithFluxContext_stepFlow_shouldSuccess() {
    StepFlow<FlowContext, Object> stepFlow = StepFlowBuilder
        .defaultBuilder()
        .named("Test")
        .execution(new TestWork())
        .build();

    StepVerifier
        .create(stepFlow
            .run(Flux.just(
                FlowContext.createFrom(Map.of("Init1", "Init1")),
                FlowContext.createFrom(Map.of("Init2", "Init2"))
            ))
        )
        .assertNext(globalReport -> {
          assertThat(globalReport.getStatus()).isEqualTo(Status.SUCCESS);
          assertThat(globalReport.getContext().get("Test")).isEqualTo("Test");
          assertThat(globalReport.getContext().get("Init2")).isNull();
          assertThat(globalReport.getContext().get("Init1")).isEqualTo("Init1");
          assertThat(globalReport.getName()).startsWith("Test");
          assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          assertThat(globalReport.getAllErrors()).isEmpty();
          assertThat(globalReport.getAllWarnings()).isEmpty();
        })
        .assertNext(globalReport -> {
          assertThat(globalReport.getStatus()).isEqualTo(Status.SUCCESS);
          assertThat(globalReport.getContext().get("Test")).isEqualTo("Test");
          assertThat(globalReport.getContext().get("Init1")).isNull();
          assertThat(globalReport.getContext().get("Init2")).isEqualTo("Init2");
          assertThat(globalReport.getName()).startsWith("Test");
          assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          assertThat(globalReport.getAllErrors()).isEmpty();
          assertThat(globalReport.getAllWarnings()).isEmpty();
        })
        .verifyComplete();
  }

  @Test
  void givenSuccessLambdaExecutionAndCustomMeta_stepFlow_shouldSuccess() {
    StepFlow<FlowContext, String> stepFlow = StepFlowBuilder
        .builderForMetadataType(String.class)
        .named("Test")
        .execution((context, metadata) -> {
          context.put("Test", "Test");
          return Mono.just(Report.success(context));
        })
        .build();

    StepVerifier
        .create(stepFlow.run(FlowContext.create()))
        .assertNext(globalReport -> {
          assertThat(globalReport.getStatus()).isEqualTo(Status.SUCCESS);
          assertThat(globalReport.getContext().get("Test")).isEqualTo("Test");
          assertThat(globalReport.getName()).isEqualTo("Test");
          assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          assertThat(globalReport.getAllErrors()).isEmpty();
          assertThat(globalReport.getAllWarnings()).isEmpty();
        })
        .verifyComplete();
  }

  @Test
  void givenSuccessLambdaExecutionAndCustomContext_stepFlow_shouldSuccess() {
    StepFlow<CustomContext, Object> stepFlow = StepFlowBuilder
        .builderForContextOfType(CustomContext.class)
        .named("Test")
        .execution((context, metadata) -> {
          context.put("Test", "Test");
          return Mono.just(Report.success(context));
        })
        .build();

    StepVerifier
        .create(stepFlow.run(new CustomContext()))
        .assertNext(globalReport -> {
          assertThat(globalReport.getStatus()).isEqualTo(Status.SUCCESS);
          assertThat(globalReport.getContext().customField).isNotNull();
          assertThat(globalReport.getContext().get("Test")).isEqualTo("Test");
          assertThat(globalReport.getName()).isEqualTo("Test");
          assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          assertThat(globalReport.getAllErrors()).isEmpty();
          assertThat(globalReport.getAllWarnings()).isEmpty();
        })
        .verifyComplete();
  }

  @Test
  void givenSuccessLambdaExecutionAndCustomContextAndCustomMeta_stepFlow_shouldSuccess() {
    StepFlow<CustomContext, String> stepFlow = StepFlowBuilder
        .builderForTypes(CustomContext.class, String.class)
        .named("Test")
        .execution((context, metadata) -> {
          context.put("Test", "Test");
          return Mono.just(Report.success(context));
        })
        .build();

    StepVerifier
        .create(stepFlow.run(new CustomContext()))
        .assertNext(globalReport -> {
          assertThat(globalReport.getStatus()).isEqualTo(Status.SUCCESS);
          assertThat(globalReport.getContext().customField).isNotNull();
          assertThat(globalReport.getContext().get("Test")).isEqualTo("Test");
          assertThat(globalReport.getName()).isEqualTo("Test");
          assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          assertThat(globalReport.getAllErrors()).isEmpty();
          assertThat(globalReport.getAllWarnings()).isEmpty();
        })
        .verifyComplete();
  }

  @Test
  @SuppressWarnings("unchecked")
  void givenTypeErrorInMetadataStep_stepFlow_shouldError() {
    ParallelFlow<FlowContext, Date> testWithMetadata = ParallelFlowBuilder
        .builderForMetadataType(Date.class)
        .named("Test")
        .parallelizeFromArray(flowContext -> (List<Date>) flowContext.get("data"))
        .parallelizedFlow(StepFlowBuilder
            .builderForMetadataType(StreamTokenizer.class)
            .named("Step")
            .execution(new TestWorkWithMetadata())
            .build()
        )
        .mergeStrategy(ParallelFlowBuilder.defaultMergeStrategy())
        .build();

    StepVerifier
        .create(testWithMetadata.run(FlowContext.createFrom(Map.of("data", List.of(new Date())))))
        .assertNext(assertAndLog(globalReport -> {
          assertThat(globalReport.getStatus()).isEqualTo(Status.ERROR);
          Assertions.assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          Assertions.assertThat(globalReport.getAllErrors()).hasSize(1);
          Assertions.assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  void givenErrorLambdaExecution_stepFlow_shouldError() {
    StepFlow<FlowContext, Object> stepFlow = StepFlowBuilder
        .defaultBuilder()
        .named("Test")
        .execution((context, metadata) ->
            Mono.just(Report.error(context, List.of(new FlowTechnicalException("Error 1"), new FlowFunctionalException("Error 2"))))
        )
        .build();

    StepVerifier
        .create(stepFlow.run(FlowContext.create()))
        .assertNext(globalReport -> {
          assertThat(globalReport.getStatus()).isEqualTo(Status.ERROR);
          assertThat(globalReport.getName()).isEqualTo("Test");
          assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          assertThat(globalReport.getAllErrors()).hasSize(2);
          assertThat(globalReport.getAllWarnings()).isEmpty();
        })
        .verifyComplete();
  }

  @Test
  void givenWarningLambdaExecution_stepFlow_shouldWarning() {
    StepFlow<FlowContext, Object> stepFlow = StepFlowBuilder
        .defaultBuilder()
        .named("Test")
        .execution((context, metadata) -> {
          context.put("Test", "Test");
          return Mono.just(Report.successWithWarning(
              context,
              List.of(new FlowTechnicalException("Warning 1"), new FlowFunctionalException("Warning 2")))
          );
        })
        .build();

    StepVerifier
        .create(stepFlow.run(FlowContext.create()))
        .assertNext(globalReport -> {
          assertThat(globalReport.getStatus()).isEqualTo(Status.WARNING);
          assertThat(globalReport.getContext().get("Test")).isEqualTo("Test");
          assertThat(globalReport.getName()).isEqualTo("Test");
          assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          assertThat(globalReport.getAllErrors()).isEmpty();
          assertThat(globalReport.getAllWarnings()).hasSize(2);
        })
        .verifyComplete();
  }

  @Test
  void givenWarningAndErrorLambdaExecution_stepFlow_shouldError() {
    StepFlow<FlowContext, Object> stepFlow = StepFlowBuilder
        .defaultBuilder()
        .named("Test")
        .execution((context, metadata) -> Mono.just(Report.errorWithWarning(
            context,
            List.of(new FlowTechnicalException("Error 1"), new FlowFunctionalException("Error 2")),
            List.of(new FlowTechnicalException("Warning 1"), new FlowFunctionalException("Warning 2"))
        )))
        .build();

    StepVerifier
        .create(stepFlow.run(FlowContext.create()))
        .assertNext(globalReport -> {
          assertThat(globalReport.getStatus()).isEqualTo(Status.ERROR);
          assertThat(globalReport.getName()).isEqualTo("Test");
          assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          assertThat(globalReport.getAllErrors()).hasSize(2);
          assertThat(globalReport.getAllWarnings()).hasSize(2);
        })
        .verifyComplete();
  }

  @Test
  void givenRawThrowLambdaExecution_stepFlow_shouldError() {
    StepFlow<FlowContext, Object> stepFlow = StepFlowBuilder
        .defaultBuilder()
        .named("Test")
        .execution((context, metadata) -> {
          throw new RuntimeException("Raw error");
        })
        .build();

    StepVerifier
        .create(stepFlow.run(FlowContext.create()))
        .assertNext(globalReport -> {
          assertThat(globalReport.getStatus()).isEqualTo(Status.ERROR);
          assertThat(globalReport.getName()).isEqualTo("Test");
          assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          assertThat(globalReport.getAllErrors()).hasSize(1);
          assertThat(globalReport.getAllWarnings()).isEmpty();
          assertThat(globalReport.getAllErrors().get(0).getType()).isEqualTo(FlowExceptionType.TECHNICAL);
        })
        .verifyComplete();
  }

  @Test
  void givenRawErrorLambdaExecution_stepFlow_shouldError() {
    StepFlow<FlowContext, Object> stepFlow = StepFlowBuilder
        .defaultBuilder()
        .named("Test")
        .execution((context, metadata) -> Mono.error(new RuntimeException("Raw error")))
        .build();

    StepVerifier
        .create(stepFlow.run(FlowContext.create()))
        .assertNext(globalReport -> {
          assertThat(globalReport.getStatus()).isEqualTo(Status.ERROR);
          assertThat(globalReport.getName()).isEqualTo("Test");
          assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          assertThat(globalReport.getAllErrors()).hasSize(1);
          assertThat(globalReport.getAllWarnings()).isEmpty();
          assertThat(globalReport.getAllErrors().get(0).getType()).isEqualTo(FlowExceptionType.TECHNICAL);
        })
        .verifyComplete();
  }

  @Test
  void givenRawFlowErrorLambdaExecution_stepFlow_shouldError() {
    StepFlow<FlowContext, Object> stepFlow = StepFlowBuilder
        .defaultBuilder()
        .named("Test")
        .execution((context, metadata) -> Mono.error(new FlowFunctionalException("Raw")))
        .build();

    StepVerifier
        .create(stepFlow.run(FlowContext.create()))
        .assertNext(globalReport -> {
          assertThat(globalReport.getStatus()).isEqualTo(Status.ERROR);
          assertThat(globalReport.getName()).isEqualTo("Test");
          assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          assertThat(globalReport.getAllErrors()).hasSize(1);
          assertThat(globalReport.getAllWarnings()).isEmpty();
          assertThat(globalReport.getAllErrors().get(0).getType()).isEqualTo(FlowExceptionType.FUNCTIONAL);
        })
        .verifyComplete();
  }

  static final class TestWork implements StepWithMetadata<FlowContext, Object> {
    @Override
    public Mono<Report<FlowContext>> apply(FlowContext context, Metadata<Object> metadata) {
      context.put("Test", "Test");
      return Mono.just(Report.success(context));
    }
  }

  static final class TestWorkWithMetadata implements StepWithMetadata<FlowContext, StreamTokenizer> {
    @Override
    public Mono<Report<FlowContext>> apply(FlowContext context, Metadata<StreamTokenizer> metadata) {
      System.out.println(metadata.getData().getClass().getSimpleName());
      context.put("Test", "Test");
      return Mono.just(Report.success(context));
    }
  }
}
