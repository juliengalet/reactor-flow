package fr.jtools.reactorflow;

import fr.jtools.reactorflow.builder.ConditionalFlowBuilder;
import fr.jtools.reactorflow.builder.ParallelFlowBuilder;
import fr.jtools.reactorflow.builder.RecoverableFlowBuilder;
import fr.jtools.reactorflow.builder.SequentialFlowBuilder;
import fr.jtools.reactorflow.builder.SwitchFlowBuilder;
import fr.jtools.reactorflow.exception.RecoverableFlowException;
import fr.jtools.reactorflow.flow.SequentialFlow;
import fr.jtools.reactorflow.report.FlowContext;
import fr.jtools.reactorflow.report.Status;
import fr.jtools.reactorflow.testutils.ErrorStepFlow;
import fr.jtools.reactorflow.testutils.SuccessStepFlow;
import fr.jtools.reactorflow.testutils.WarningStepFlow;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Objects;

import static fr.jtools.reactorflow.testutils.TestUtils.assertAndLog;
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
        .assertNext(assertAndLog(globalReport -> {
          assertThat(globalReport.getStatus()).isEqualTo(Status.WARNING);
        }))
        .verifyComplete();
  }
}
