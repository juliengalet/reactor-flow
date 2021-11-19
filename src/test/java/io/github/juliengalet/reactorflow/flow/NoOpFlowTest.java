package io.github.juliengalet.reactorflow.flow;

import io.github.juliengalet.reactorflow.report.FlowContext;
import io.github.juliengalet.reactorflow.testutils.CustomContext;
import io.github.juliengalet.reactorflow.testutils.TestUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

final class NoOpFlowTest {
  @Test
  void noOpFlow_shouldKeepState() {
    StepVerifier
        .create(NoOpFlow.named("Test").run(FlowContext.create()))
        .expectNextCount(1)
        .verifyComplete();
  }

  @Test
  void noOpFlow_shouldKeepCustomState() {
    StepVerifier
        .create(NoOpFlow.<CustomContext>named("Test").run(new CustomContext()))
        .assertNext(TestUtils.assertAndLog(globalReport -> assertThat(globalReport.getContext().customField).isEqualTo("CUSTOM")))
        .verifyComplete();
  }

  @Test
  void givenClonedWithNewName_noOpFlow_shouldKeepState() {
    StepVerifier
        .create(NoOpFlow.named("Test").cloneFlow("Test copy").run(FlowContext.create()))
        .assertNext(TestUtils.assertAndLog(globalReport -> Assertions.assertThat(globalReport.getName()).isEqualTo("Test copy")))
        .verifyComplete();
  }

  @Test
  void givenCloned_noOpFlow_shouldKeepState() {
    StepVerifier
        .create(NoOpFlow.named("Test").cloneFlow().run(FlowContext.create()))
        .assertNext(TestUtils.assertAndLog(globalReport -> Assertions.assertThat(globalReport.getName()).isEqualTo("Test")))
        .verifyComplete();
  }
}
