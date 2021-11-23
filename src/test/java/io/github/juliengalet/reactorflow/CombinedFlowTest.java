package io.github.juliengalet.reactorflow;

import io.github.juliengalet.reactorflow.builder.ConditionalFlowBuilder;
import io.github.juliengalet.reactorflow.builder.ParallelFlowBuilder;
import io.github.juliengalet.reactorflow.builder.RecoverableFlowBuilder;
import io.github.juliengalet.reactorflow.builder.SequentialFlowBuilder;
import io.github.juliengalet.reactorflow.builder.SwitchFlowBuilder;
import io.github.juliengalet.reactorflow.exception.RecoverableFlowException;
import io.github.juliengalet.reactorflow.flow.Flow;
import io.github.juliengalet.reactorflow.flow.SequentialFlow;
import io.github.juliengalet.reactorflow.report.FlowContext;
import io.github.juliengalet.reactorflow.report.Status;
import io.github.juliengalet.reactorflow.testutils.ErrorStepFlow;
import io.github.juliengalet.reactorflow.testutils.SuccessFinallyStepFlow;
import io.github.juliengalet.reactorflow.testutils.SuccessStepFlow;
import io.github.juliengalet.reactorflow.testutils.SuccessWithIntegerMetadataStepFlow;
import io.github.juliengalet.reactorflow.testutils.SuccessWithStringMetadataStepFlow;
import io.github.juliengalet.reactorflow.testutils.WarningStepFlow;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static io.github.juliengalet.reactorflow.testutils.TestUtils.assertAndLog;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * This class tests complex use cases with combined flows.
 */
final class CombinedFlowTest {
  @Test
  final void complexCase() {
    SequentialFlow<FlowContext> complexCase = SequentialFlowBuilder
        .defaultBuilder()
        .named("Complex case")
        .then(SuccessStepFlow.flowNamed("Seq 1"))
        .then(WarningStepFlow.flowNamed("Seq 2"))
        .then(RecoverableFlowBuilder
            .defaultBuilder()
            .named("Seq 3 > Recover")
            .tryFlow(ErrorStepFlow.flowNamed("Seq 3 > Recover > Try"))
            .recover(SuccessStepFlow.flowNamed("Seq 3 > Recover > Recover"))
            .recoverOn(RecoverableFlowException.ALL)
            .build()
        )
        .then(ParallelFlowBuilder
            .defaultBuilder()
            .named("Seq 4 > Parallel")
            .parallelize(List.of(
                SuccessStepFlow.flowNamed("Seq 4 > Parallel > 1"),
                SuccessStepFlow.flowNamed("Seq 4 > Parallel > 2"),
                SuccessStepFlow.flowNamed("Seq 4 > Parallel > 3")
            ))
            .mergeStrategy(ParallelFlowBuilder.defaultMergeStrategy())
            .build()
        )
        .then(ConditionalFlowBuilder
            .defaultBuilder()
            .named("Seq 5 > Conditional")
            .condition(context -> Objects.nonNull(context.get("Seq 3 > Recover > Try")))
            .caseTrue(SuccessStepFlow.flowNamed("Seq 5 > Conditional > True"))
            .caseFalse(SuccessStepFlow.flowNamed("Seq 5 > Conditional > False"))
            .build()
        )
        .doFinally(SwitchFlowBuilder
            .defaultBuilder()
            .named("Finally > Switch")
            .switchCondition(context -> (String) context.get("Seq 2"))
            .switchCase("Seq 1", SuccessStepFlow.flowNamed("Finally > Switch > Seq 1"))
            .switchCase("Seq 2", SuccessStepFlow.flowNamed("Finally > Switch > Seq 2"))
            .defaultCase(SuccessStepFlow.flowNamed("Finally > Switch > Default"))
            .build()
        )
        .build();

    StepVerifier
        .create(complexCase.run(FlowContext.create()))
        .assertNext(assertAndLog(globalReport -> assertThat(globalReport.getStatus()).isEqualTo(Status.WARNING)))
        .verifyComplete();
  }

  @Test
  @SuppressWarnings("unchecked")
  void complexCaseWithMetadata() {
    String STRING_LIST_KEY = "STRING_LIST";
    String INTEGER_LIST_KEY = "INTEGER_LIST";
    String INTEGER_LIST_2_KEY = "INTEGER_LIST_2";

    Flow<FlowContext> flowToTest = ParallelFlowBuilder
        .builderForMetadataType(String.class)
        .named("Parallel")
        .parallelizeFromArray(flowContext -> (List<String>) flowContext.get(STRING_LIST_KEY))
        .parallelizedFlow(
            SequentialFlowBuilder
                .defaultBuilder()
                .named("Parallelized sequential")
                .then(SuccessWithStringMetadataStepFlow.flowNamed("String metadata step"))
                .then(ParallelFlowBuilder
                    .builderForMetadataType(Integer.class)
                    .named("Nested parallel")
                    .parallelizeFromArray(flowContext -> (List<Integer>) flowContext.get(INTEGER_LIST_KEY))
                    .parallelizedFlow(SuccessWithIntegerMetadataStepFlow.flowNamed("Integer metadata step"))
                    .mergeStrategy(ParallelFlowBuilder.defaultMergeStrategy())
                    .build()
                )
                .then(ParallelFlowBuilder
                    .builderForMetadataType(Integer.class)
                    .named("Nested parallel 2")
                    .parallelizeFromArray(flowContext -> (List<Integer>) flowContext.get(INTEGER_LIST_2_KEY))
                    .parallelizedFlow(SequentialFlowBuilder
                        .defaultBuilder()
                        .named("Nested sequential")
                        .then(SuccessWithIntegerMetadataStepFlow.flowNamed("Integer metadata step 2"))
                        .then(ErrorStepFlow.flowNamed("Error"))
                        .doFinally(SuccessFinallyStepFlow.flowNamed("Nested finally"))
                        .build()
                    )
                    .mergeStrategy(ParallelFlowBuilder.defaultMergeStrategy())
                    .build()
                )
                .doFinally(SuccessFinallyStepFlow.flowNamed("Finally"))
                .build()
        )
        .mergeStrategy(ParallelFlowBuilder.defaultMergeStrategy())
        .build();

    FlowContext initialContext = FlowContext.createFrom(Map.of(
        STRING_LIST_KEY, List.of("Item 1", "Item 2", "Item 3"),
        INTEGER_LIST_KEY, List.of(1, 2, 3),
        INTEGER_LIST_2_KEY, List.of(4, 5, 6)
    ));

    StepVerifier
        .create(flowToTest.run(initialContext))
        .assertNext(assertAndLog(globalReport -> {
          assertThat(globalReport.getStatus()).isEqualTo(Status.ERROR);
          Set<Map.Entry<String, Object>> entrySet = globalReport.getContext().getEntrySet();

          long finallyEntries = entrySet.stream().filter(entry -> entry.getKey().startsWith("Finally |")).count();
          long nestedFinallyEntries = entrySet.stream().filter(entry -> entry.getKey().startsWith("Nested finally |")).count();
          long integerMetadataEntries = entrySet.stream().filter(entry -> entry.getKey().startsWith("Integer metadata step |")).count();
          long integerMetadata2Entries = entrySet.stream().filter(entry -> entry.getKey().startsWith("Integer metadata step 2 |")).count();
          long stringMetadataEntries = entrySet.stream().filter(entry -> entry.getKey().startsWith("String metadata step |")).count();

          assertThat(finallyEntries).isEqualTo(3);
          assertThat(nestedFinallyEntries).isEqualTo(9);
          assertThat(integerMetadataEntries).isEqualTo(9);
          assertThat(integerMetadata2Entries).isEqualTo(9);
          assertThat(stringMetadataEntries).isEqualTo(3);
        }))
        .verifyComplete();
  }
}
