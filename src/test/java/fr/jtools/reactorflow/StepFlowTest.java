package fr.jtools.reactorflow;

import fr.jtools.reactorflow.builder.ParallelFlowBuilder;
import fr.jtools.reactorflow.builder.StepFlowBuilder;
import fr.jtools.reactorflow.exception.FlowBuilderException;
import fr.jtools.reactorflow.exception.FlowExceptionType;
import fr.jtools.reactorflow.exception.FlowFunctionalException;
import fr.jtools.reactorflow.exception.FlowTechnicalException;
import fr.jtools.reactorflow.flow.ParallelFlow;
import fr.jtools.reactorflow.flow.Step;
import fr.jtools.reactorflow.flow.StepFlow;
import fr.jtools.reactorflow.state.FlowContext;
import fr.jtools.reactorflow.state.Metadata;
import fr.jtools.reactorflow.state.State;
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
        .execution((flow, state, metadata) -> Mono.just(state))
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
        .execution((flow, state, metadata) -> {
          state.getContext().put("Test", "Test");
          return Mono.just(state);
        })
        .build();

    StepVerifier
        .create(stepFlow.run(FlowContext.create()))
        .assertNext(state -> {
          assertThat(state.getStatus()).isEqualTo(State.Status.SUCCESS);
          assertThat(state.getContext().get("Test")).isEqualTo("Test");
          assertThat(state.getName()).isEqualTo("Test");
          assertThat(state.getAllRecoveredErrors()).isEmpty();
          assertThat(state.getAllErrors()).isEmpty();
          assertThat(state.getAllWarnings()).isEmpty();
        })
        .verifyComplete();
  }

  @Test
  final void givenClonedWithNewNameSuccessLambdaExecution_stepFlow_shouldSuccess() {
    StepFlow<FlowContext, Object> stepFlow = StepFlowBuilder
        .defaultBuilder()
        .named("Test")
        .execution((flow, state, metadata) -> {
          state.getContext().put("Test", "Test");
          return Mono.just(state);
        })
        .build();

    StepVerifier
        .create(stepFlow.cloneFlow("Test cloned").run(FlowContext.create()))
        .assertNext(state -> {
          assertThat(state.getStatus()).isEqualTo(State.Status.SUCCESS);
          assertThat(state.getContext().get("Test")).isEqualTo("Test");
          assertThat(state.getName()).isEqualTo("Test cloned");
          assertThat(state.getAllRecoveredErrors()).isEmpty();
          assertThat(state.getAllErrors()).isEmpty();
          assertThat(state.getAllWarnings()).isEmpty();
        })
        .verifyComplete();
  }

  @Test
  final void givenClonedSuccessLambdaExecution_stepFlow_shouldSuccess() {
    StepFlow<FlowContext, Object> stepFlow = StepFlowBuilder
        .defaultBuilder()
        .named("Test")
        .execution((flow, state, metadata) -> {
          state.getContext().put("Test", "Test");
          return Mono.just(state);
        })
        .build();

    StepVerifier
        .create(stepFlow.cloneFlow().run(FlowContext.create()))
        .assertNext(state -> {
          assertThat(state.getStatus()).isEqualTo(State.Status.SUCCESS);
          assertThat(state.getContext().get("Test")).isEqualTo("Test");
          assertThat(state.getName()).isEqualTo("Test");
          assertThat(state.getAllRecoveredErrors()).isEmpty();
          assertThat(state.getAllErrors()).isEmpty();
          assertThat(state.getAllWarnings()).isEmpty();
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
        .assertNext(state -> {
          assertThat(state.getStatus()).isEqualTo(State.Status.SUCCESS);
          assertThat(state.getContext().get("Test")).isEqualTo("Test");
          assertThat(state.getName()).isEqualTo("Test");
          assertThat(state.getAllRecoveredErrors()).isEmpty();
          assertThat(state.getAllErrors()).isEmpty();
          assertThat(state.getAllWarnings()).isEmpty();
        })
        .verifyComplete();
  }

  @Test
  final void givenSuccessLambdaExecutionAndCustomMeta_stepFlow_shouldSuccess() {
    StepFlow<FlowContext, String> stepFlow = StepFlowBuilder
        .builderForMetadataType(String.class)
        .named("Test")
        .execution((flow, state, metadata) -> {
          state.getContext().put("Test", "Test");
          return Mono.just(state);
        })
        .build();

    StepVerifier
        .create(stepFlow.run(FlowContext.create()))
        .assertNext(state -> {
          assertThat(state.getStatus()).isEqualTo(State.Status.SUCCESS);
          assertThat(state.getContext().get("Test")).isEqualTo("Test");
          assertThat(state.getName()).isEqualTo("Test");
          assertThat(state.getAllRecoveredErrors()).isEmpty();
          assertThat(state.getAllErrors()).isEmpty();
          assertThat(state.getAllWarnings()).isEmpty();
        })
        .verifyComplete();
  }

  @Test
  final void givenSuccessLambdaExecutionAndCustomContext_stepFlow_shouldSuccess() {
    StepFlow<CustomContext, Object> stepFlow = StepFlowBuilder
        .builderForContextOfType(CustomContext.class)
        .named("Test")
        .execution((flow, state, metadata) -> {
          state.getContext().put("Test", "Test");
          return Mono.just(state);
        })
        .build();

    StepVerifier
        .create(stepFlow.run(new CustomContext()))
        .assertNext(state -> {
          assertThat(state.getStatus()).isEqualTo(State.Status.SUCCESS);
          assertThat(state.getContext().customField).isNotNull();
          assertThat(state.getContext().get("Test")).isEqualTo("Test");
          assertThat(state.getName()).isEqualTo("Test");
          assertThat(state.getAllRecoveredErrors()).isEmpty();
          assertThat(state.getAllErrors()).isEmpty();
          assertThat(state.getAllWarnings()).isEmpty();
        })
        .verifyComplete();
  }

  @Test
  final void givenSuccessLambdaExecutionAndCustomContextAndCustomMeta_stepFlow_shouldSuccess() {
    StepFlow<CustomContext, String> stepFlow = StepFlowBuilder
        .builderForTypes(CustomContext.class, String.class)
        .named("Test")
        .execution((flow, state, metadata) -> {
          state.getContext().put("Test", "Test");
          return Mono.just(state);
        })
        .build();

    StepVerifier
        .create(stepFlow.run(new CustomContext()))
        .assertNext(state -> {
          assertThat(state.getStatus()).isEqualTo(State.Status.SUCCESS);
          assertThat(state.getContext().customField).isNotNull();
          assertThat(state.getContext().get("Test")).isEqualTo("Test");
          assertThat(state.getName()).isEqualTo("Test");
          assertThat(state.getAllRecoveredErrors()).isEmpty();
          assertThat(state.getAllErrors()).isEmpty();
          assertThat(state.getAllWarnings()).isEmpty();
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
        .assertNext(assertAndLog(state -> {
          assertThat(state.getStatus()).isEqualTo(State.Status.ERROR);
          assertThat(state.getAllRecoveredErrors()).isEmpty();
          assertThat(state.getAllErrors()).hasSize(1);
          assertThat(state.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  final void givenErrorLambdaExecution_stepFlow_shouldError() {
    StepFlow<FlowContext, Object> stepFlow = StepFlowBuilder
        .defaultBuilder()
        .named("Test")
        .execution((flow, state, metadata) -> {
          flow.addErrors(List.of(new FlowTechnicalException(flow, "Error 1"), new FlowFunctionalException(flow, "Error 2")));
          return Mono.just(state);
        })
        .build();

    StepVerifier
        .create(stepFlow.run(FlowContext.create()))
        .assertNext(state -> {
          assertThat(state.getStatus()).isEqualTo(State.Status.ERROR);
          assertThat(state.getName()).isEqualTo("Test");
          assertThat(state.getAllRecoveredErrors()).isEmpty();
          assertThat(state.getAllErrors()).hasSize(2);
          assertThat(state.getAllWarnings()).isEmpty();
        })
        .verifyComplete();
  }

  @Test
  final void givenWarningLambdaExecution_stepFlow_shouldWarning() {
    StepFlow<FlowContext, Object> stepFlow = StepFlowBuilder
        .defaultBuilder()
        .named("Test")
        .execution((flow, state, metadata) -> {
          state.getContext().put("Test", "Test");
          flow.addWarnings(List.of(new FlowTechnicalException(flow, "Warning 1"), new FlowFunctionalException(flow, "Warning 2")));
          return Mono.just(state);
        })
        .build();

    StepVerifier
        .create(stepFlow.run(FlowContext.create()))
        .assertNext(state -> {
          assertThat(state.getStatus()).isEqualTo(State.Status.WARNING);
          assertThat(state.getContext().get("Test")).isEqualTo("Test");
          assertThat(state.getName()).isEqualTo("Test");
          assertThat(state.getAllRecoveredErrors()).isEmpty();
          assertThat(state.getAllErrors()).isEmpty();
          assertThat(state.getAllWarnings()).hasSize(2);
        })
        .verifyComplete();
  }

  @Test
  final void givenWarningAndErrorLambdaExecution_stepFlow_shouldError() {
    StepFlow<FlowContext, Object> stepFlow = StepFlowBuilder
        .defaultBuilder()
        .named("Test")
        .execution((flow, state, metadata) -> {
          flow.addErrors(List.of(new FlowTechnicalException(flow, "Error 1"), new FlowFunctionalException(flow, "Error 2")));
          flow.addWarnings(List.of(new FlowTechnicalException(flow, "Warning 1"), new FlowFunctionalException(flow, "Warning 2")));
          return Mono.just(state);
        })
        .build();

    StepVerifier
        .create(stepFlow.run(FlowContext.create()))
        .assertNext(state -> {
          assertThat(state.getStatus()).isEqualTo(State.Status.ERROR);
          assertThat(state.getName()).isEqualTo("Test");
          assertThat(state.getAllRecoveredErrors()).isEmpty();
          assertThat(state.getAllErrors()).hasSize(2);
          assertThat(state.getAllWarnings()).hasSize(2);
        })
        .verifyComplete();
  }

  @Test
  final void givenRawErrorLambdaExecution_stepFlow_shouldError() {
    StepFlow<FlowContext, Object> stepFlow = StepFlowBuilder
        .defaultBuilder()
        .named("Test")
        .execution((flow, state, metadata) -> Mono.error(new RuntimeException("Raw error")))
        .build();

    StepVerifier
        .create(stepFlow.run(FlowContext.create()))
        .assertNext(state -> {
          assertThat(state.getStatus()).isEqualTo(State.Status.ERROR);
          assertThat(state.getName()).isEqualTo("Test");
          assertThat(state.getAllRecoveredErrors()).isEmpty();
          assertThat(state.getAllErrors()).hasSize(1);
          assertThat(state.getAllWarnings()).isEmpty();
          assertThat(state.getAllErrors().get(0).getType()).isEqualTo(FlowExceptionType.TECHNICAL);
        })
        .verifyComplete();
  }

  @Test
  final void givenRawFlowErrorLambdaExecution_stepFlow_shouldError() {
    StepFlow<FlowContext, Object> stepFlow = StepFlowBuilder
        .defaultBuilder()
        .named("Test")
        .execution((flow, state, metadata) -> Mono.error(new FlowFunctionalException(flow, "Raw")))
        .build();

    StepVerifier
        .create(stepFlow.run(FlowContext.create()))
        .assertNext(state -> {
          assertThat(state.getStatus()).isEqualTo(State.Status.ERROR);
          assertThat(state.getName()).isEqualTo("Test");
          assertThat(state.getAllRecoveredErrors()).isEmpty();
          assertThat(state.getAllErrors()).hasSize(1);
          assertThat(state.getAllWarnings()).isEmpty();
          assertThat(state.getAllErrors().get(0).getType()).isEqualTo(FlowExceptionType.FUNCTIONAL);
        })
        .verifyComplete();
  }

  static final class TestWork implements Step<FlowContext, Object> {
    @Override
    public Mono<State<FlowContext>> apply(StepFlow<FlowContext, Object> flow, State<FlowContext> state, Metadata<Object> metadata) {
      state.getContext().put("Test", "Test");
      return Mono.just(state);
    }
  }

  static final class TestWorkWithMetadata implements Step<FlowContext, StreamTokenizer> {
    @Override
    public Mono<State<FlowContext>> apply(StepFlow<FlowContext, StreamTokenizer> flow, State<FlowContext> state, Metadata<StreamTokenizer> metadata) {
      System.out.println(metadata.getData().getClass().getSimpleName());
      state.getContext().put("Test", "Test");
      return Mono.just(state);
    }
  }
}
