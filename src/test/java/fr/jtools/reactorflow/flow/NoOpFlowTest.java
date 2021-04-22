package fr.jtools.reactorflow.flow;

import fr.jtools.reactorflow.report.FlowContext;
import fr.jtools.reactorflow.testutils.CustomContext;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import static fr.jtools.reactorflow.testutils.TestUtils.assertAndLog;
import static org.assertj.core.api.Assertions.assertThat;

final class NoOpFlowTest {
  @Test
  final void noOpFlow_shouldKeepState() {
    StepVerifier
        .create(NoOpFlow.named("Test").run(FlowContext.create()))
        .expectNextCount(1)
        .verifyComplete();
  }

  @Test
  final void noOpFlow_shouldKeepCustomState() {
    StepVerifier
        .create(NoOpFlow.<CustomContext>named("Test").run(new CustomContext()))
        .assertNext(assertAndLog(globalReport -> assertThat(globalReport.getContext().customField).isEqualTo("CUSTOM")))
        .verifyComplete();
  }

  @Test
  final void givenClonedWithNewName_noOpFlow_shouldKeepState() {
    StepVerifier
        .create(NoOpFlow.named("Test").cloneFlow("Test copy").run(FlowContext.create()))
        .assertNext(assertAndLog(globalReport -> assertThat(globalReport.getName()).isEqualTo("Test copy")))
        .verifyComplete();
  }

  @Test
  final void givenCloned_noOpFlow_shouldKeepState() {
    StepVerifier
        .create(NoOpFlow.named("Test").cloneFlow().run(FlowContext.create()))
        .assertNext(assertAndLog(globalReport -> assertThat(globalReport.getName()).isEqualTo("Test")))
        .verifyComplete();
  }
}
