package fr.jtools.reactorflow.flow;

import fr.jtools.reactorflow.builder.ParallelFlowBuilder;
import fr.jtools.reactorflow.builder.StepFlowBuilder;
import fr.jtools.reactorflow.exception.FlowBuilderException;
import fr.jtools.reactorflow.exception.FlowExceptionType;
import fr.jtools.reactorflow.exception.FlowFunctionalException;
import fr.jtools.reactorflow.exception.FlowTechnicalException;
import fr.jtools.reactorflow.report.FlowContext;
import fr.jtools.reactorflow.report.Metadata;
import fr.jtools.reactorflow.report.Report;
import fr.jtools.reactorflow.report.Status;
import fr.jtools.reactorflow.testutils.CustomContext;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.StreamTokenizer;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static fr.jtools.reactorflow.testutils.TestUtils.assertAndLog;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

final class StepFlowTest {
  @Test
  final void givenNullName_stepFlow_shouldNotBuild() {
    assertThatExceptionOfType(FlowBuilderException.class).isThrownBy(() -> StepFlowBuilder
        .defaultBuilder()
        .named(null)
        .execution((context, metadata) -> Mono.just(Report.success(context)))
        .build()
    );
  }

  @Test
  final void givenNullExecution_stepFlow_shouldNotBuild() {
    assertThatExceptionOfType(FlowBuilderException.class).isThrownBy(() -> StepFlowBuilder
        .defaultBuilder()
        .named("Test")
        .execution(null)
        .build()
    );
  }

  @Test
  final void givenSuccessLambdaExecution_stepFlow_shouldSuccess() {
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
  final void givenClonedWithNewNameSuccessLambdaExecution_stepFlow_shouldSuccess() {
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
  final void givenClonedSuccessLambdaExecution_stepFlow_shouldSuccess() {
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
  final void givenSuccessClassExecution_stepFlow_shouldSuccess() {
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
  final void givenSuccessLambdaExecutionAndCustomMeta_stepFlow_shouldSuccess() {
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
  final void givenSuccessLambdaExecutionAndCustomContext_stepFlow_shouldSuccess() {
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
  final void givenSuccessLambdaExecutionAndCustomContextAndCustomMeta_stepFlow_shouldSuccess() {
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
  final void givenTypeErrorInMetadataStep_stepFlow_shouldError() {
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
          assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          assertThat(globalReport.getAllErrors()).hasSize(1);
          assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  final void givenErrorLambdaExecution_stepFlow_shouldError() {
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
  final void givenWarningLambdaExecution_stepFlow_shouldWarning() {
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
  final void givenWarningAndErrorLambdaExecution_stepFlow_shouldError() {
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
  final void givenRawThrowLambdaExecution_stepFlow_shouldError() {
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
  final void givenRawErrorLambdaExecution_stepFlow_shouldError() {
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
  final void givenRawFlowErrorLambdaExecution_stepFlow_shouldError() {
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

  static final class TestWork implements Step<FlowContext, Object> {
    @Override
    public Mono<Report<FlowContext>> apply(FlowContext context, Metadata<Object> metadata) {
      context.put("Test", "Test");
      return Mono.just(Report.success(context));
    }
  }

  static final class TestWorkWithMetadata implements Step<FlowContext, StreamTokenizer> {
    @Override
    public Mono<Report<FlowContext>> apply(FlowContext context, Metadata<StreamTokenizer> metadata) {
      System.out.println(metadata.getData().getClass().getSimpleName());
      context.put("Test", "Test");
      return Mono.just(Report.success(context));
    }
  }
}
