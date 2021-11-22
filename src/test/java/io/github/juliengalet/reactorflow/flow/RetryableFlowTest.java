package io.github.juliengalet.reactorflow.flow;

import io.github.juliengalet.reactorflow.builder.RetryableFlowBuilder;
import io.github.juliengalet.reactorflow.exception.FlowBuilderException;
import io.github.juliengalet.reactorflow.exception.FlowTechnicalException;
import io.github.juliengalet.reactorflow.exception.RecoverableFlowException;
import io.github.juliengalet.reactorflow.report.FlowContext;
import io.github.juliengalet.reactorflow.report.Metadata;
import io.github.juliengalet.reactorflow.report.Report;
import io.github.juliengalet.reactorflow.report.Status;
import io.github.juliengalet.reactorflow.testutils.CustomContext;
import io.github.juliengalet.reactorflow.testutils.SuccessStepFlow;
import io.github.juliengalet.reactorflow.testutils.TestUtils;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

final class RetryableFlowTest {
  @Test
  void checkGlobalReportToString() {
    RetryableFlow<FlowContext> retryableFlow = RetryableFlowBuilder
        .defaultBuilder()
        .named("Test")
        .tryFlow(new RetryableStep<>(5, new AtomicInteger(), true).build("Retry"))
        .retryOn(RecoverableFlowException.ALL)
        .retryTimes(4)
        .delay(1)
        .build();

    StepVerifier.create(retryableFlow.run(FlowContext.create()))
        .assertNext(TestUtils.assertAndLog(globalReport -> assertThat(globalReport.toString()).isNotNull()))
        .verifyComplete();
  }

  @Test
  void checkGlobalReportToPrettyString() {
    RetryableFlow<FlowContext> retryableFlow = RetryableFlowBuilder
        .defaultBuilder()
        .named("Test")
        .tryFlow(new RetryableStep<>(5, new AtomicInteger(), true).build("Retry"))
        .retryOn(RecoverableFlowException.ALL)
        .retryTimes(4)
        .delay(1)
        .build();

    StepVerifier.create(retryableFlow.run(FlowContext.create()))
        .assertNext(TestUtils.assertAndLog(globalReport -> assertThat(globalReport.toPrettyString()).isNotNull()))
        .verifyComplete();
  }

  @Test
  void checkGlobalReportToTreeString() {
    RetryableFlow<FlowContext> retryableFlow = RetryableFlowBuilder
        .defaultBuilder()
        .named("Test")
        .tryFlow(new RetryableStep<>(5, new AtomicInteger(), true).build("Retry"))
        .retryOn(RecoverableFlowException.ALL)
        .retryTimes(4)
        .delay(1)
        .build();

    StepVerifier.create(retryableFlow.run(FlowContext.create()))
        .assertNext(TestUtils.assertAndLog(globalReport -> assertThat(globalReport.toTreeString()).isNotNull()))
        .verifyComplete();
  }

  @Test
  void checkGlobalReportToPrettyTreeString() {
    RetryableFlow<FlowContext> retryableFlow = RetryableFlowBuilder
        .defaultBuilder()
        .named("Test")
        .tryFlow(new RetryableStep<>(5, new AtomicInteger(), true).build("Retry"))
        .retryOn(RecoverableFlowException.ALL)
        .retryTimes(4)
        .delay(1)
        .build();

    StepVerifier.create(retryableFlow.run(FlowContext.create()))
        .assertNext(TestUtils.assertAndLog(globalReport -> assertThat(globalReport.toPrettyTreeString()).isNotNull()))
        .verifyComplete();
  }

  @Test
  void checkGlobalReportToPrettyExceptionsRaisedString() {
    RetryableFlow<FlowContext> retryableFlow = RetryableFlowBuilder
        .defaultBuilder()
        .named("Test")
        .tryFlow(new RetryableStep<>(5, new AtomicInteger(), true).build("Retry"))
        .retryOn(RecoverableFlowException.ALL)
        .retryTimes(4)
        .delay(1)
        .build();

    StepVerifier.create(retryableFlow.run(FlowContext.create()))
        .assertNext(TestUtils.assertAndLog(globalReport -> assertThat(globalReport.toPrettyExceptionsRaisedString()).isNotNull()))
        .verifyComplete();
  }

  @Test
  void checkGlobalReportToExceptionsRaisedStringWithError() {
    RetryableFlow<FlowContext> retryableFlow = RetryableFlowBuilder
        .defaultBuilder()
        .named("Test")
        .tryFlow(new RetryableStep<>(5, new AtomicInteger(), true).build("Retry"))
        .retryOn(RecoverableFlowException.ALL)
        .retryTimes(2)
        .delay(1)
        .build();

    StepVerifier.create(retryableFlow.run(FlowContext.create()))
        .assertNext(TestUtils.assertAndLog(globalReport -> assertThat(globalReport.toExceptionsRaisedString()).isNotNull()))
        .verifyComplete();
  }

  @Test
  void checkGlobalReportToExceptionsRaisedStringWithSuccess() {
    RetryableFlow<FlowContext> retryableFlow = RetryableFlowBuilder
        .defaultBuilder()
        .named("Test")
        .tryFlow(new RetryableStep<>(1, new AtomicInteger(), false).build("Retry"))
        .retryOn(RecoverableFlowException.ALL)
        .retryTimes(2)
        .delay(1)
        .build();

    StepVerifier.create(retryableFlow.run(FlowContext.create()))
        .assertNext(TestUtils.assertAndLog(globalReport -> assertThat(globalReport.toExceptionsRaisedString()).isNotNull()))
        .verifyComplete();
  }


  @Test
  void checkGlobalReportToExceptionsRaisedString() {
    RetryableFlow<FlowContext> retryableFlow = RetryableFlowBuilder
        .defaultBuilder()
        .named("Test")
        .tryFlow(new RetryableStep<>(5, new AtomicInteger(), true).build("Retry"))
        .retryOn(RecoverableFlowException.ALL)
        .retryTimes(4)
        .delay(1)
        .build();

    StepVerifier.create(retryableFlow.run(FlowContext.create()))
        .assertNext(TestUtils.assertAndLog(globalReport -> assertThat(globalReport.toExceptionsRaisedString()).isNotNull()))
        .verifyComplete();
  }

  @Test
  void givenSuccessOnFirstTry_retryableFlow_shouldSuccess() {
    RetryableFlow<FlowContext> retryableFlow = RetryableFlowBuilder
        .defaultBuilder()
        .named("Test")
        .tryFlow(new RetryableStep<>(1, new AtomicInteger(), false).build("Retry"))
        .retryOn(RecoverableFlowException.ALL)
        .retryTimes(1)
        .delay(1)
        .build();

    StepVerifier.create(retryableFlow.run(FlowContext.create()))
        .assertNext(TestUtils.assertAndLog(globalReport -> {
          assertThat(globalReport.getName()).isEqualTo("Test");
          assertThat(globalReport.getContext().get("Success")).isEqualTo("Success");
          assertThat(globalReport.getStatus()).isEqualTo(Status.SUCCESS);
          assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          assertThat(globalReport.getAllErrors()).isEmpty();
          assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  void givenWarningOnFirstTry_retryableFlow_shouldWarning() {
    RetryableFlow<FlowContext> retryableFlow = RetryableFlowBuilder
        .defaultBuilder()
        .named("Test")
        .tryFlow(new RetryableStep<>(1, new AtomicInteger(), true).build("Retry"))
        .retryOn(RecoverableFlowException.ALL)
        .retryTimes(1)
        .delay(1)
        .build();

    StepVerifier.create(retryableFlow.run(FlowContext.create()))
        .assertNext(TestUtils.assertAndLog(globalReport -> {
          assertThat(globalReport.getName()).isEqualTo("Test");
          assertThat(globalReport.getContext().get("Success")).isEqualTo("Success");
          assertThat(globalReport.getStatus()).isEqualTo(Status.WARNING);
          assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          assertThat(globalReport.getAllErrors()).isEmpty();
          assertThat(globalReport.getAllWarnings()).hasSize(1);
        }))
        .verifyComplete();
  }

  @Test
  void givenErrorOnSomeTries_retryableFlow_shouldSuccess() {
    RetryableFlow<FlowContext> retryableFlow = RetryableFlowBuilder
        .defaultBuilder()
        .named("Test")
        .tryFlow(new RetryableStep<>(5, new AtomicInteger(), false).build("Retry"))
        .retryOn(RecoverableFlowException.ALL)
        .retryTimes(4)
        .delay(1)
        .build();

    StepVerifier.create(retryableFlow.run(FlowContext.create()))
        .assertNext(TestUtils.assertAndLog(globalReport -> {
          assertThat(globalReport.getName()).isEqualTo("Test");
          assertThat(globalReport.getContext().get("Success")).isEqualTo("Success");
          assertThat(globalReport.getStatus()).isEqualTo(Status.SUCCESS);
          assertThat(globalReport.getAllRecoveredErrors()).hasSize(4);
          assertThat(globalReport.getAllErrors()).isEmpty();
          assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  void givenErrorOnSomeTriesAndCustomContext_retryableFlow_shouldSuccess() {
    RetryableFlow<CustomContext> retryableFlow = RetryableFlowBuilder
        .builderForContextOfType(CustomContext.class)
        .named("Test")
        .tryFlow(new RetryableStep<CustomContext>(5, new AtomicInteger(), false).build("Retry"))
        .retryOn(RecoverableFlowException.ALL)
        .retryTimes(4)
        .delay(1)
        .build();

    StepVerifier.create(retryableFlow.run(new CustomContext()))
        .assertNext(TestUtils.assertAndLog(globalReport -> {
          assertThat(globalReport.getName()).isEqualTo("Test");
          assertThat(globalReport.getContext().customField).isNotNull();
          assertThat(globalReport.getContext().get("Success")).isEqualTo("Success");
          assertThat(globalReport.getStatus()).isEqualTo(Status.SUCCESS);
          assertThat(globalReport.getAllRecoveredErrors()).hasSize(4);
          assertThat(globalReport.getAllErrors()).isEmpty();
          assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  void givenErrorOnSomeTriesClonedWithNewName_retryableFlow_shouldSuccess() {
    RetryableFlow<FlowContext> retryableFlow = RetryableFlowBuilder
        .defaultBuilder()
        .named("Test")
        .tryFlow(new RetryableStep<>(5, new AtomicInteger(), false).build("Retry"))
        .retryOn(RecoverableFlowException.ALL)
        .retryTimes(4)
        .delay(1)
        .build();

    StepVerifier.create(retryableFlow.cloneFlow("Test copy").run(FlowContext.create()))
        .assertNext(TestUtils.assertAndLog(globalReport -> {
          assertThat(globalReport.getName()).isEqualTo("Test copy");
          assertThat(globalReport.getContext().get("Success")).isEqualTo("Success");
          assertThat(globalReport.getStatus()).isEqualTo(Status.SUCCESS);
          assertThat(globalReport.getAllRecoveredErrors()).hasSize(4);
          assertThat(globalReport.getAllErrors()).isEmpty();
          assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  void givenErrorOnSomeTriesCloned_retryableFlow_shouldSuccess() {
    RetryableFlow<FlowContext> retryableFlow = RetryableFlowBuilder
        .defaultBuilder()
        .named("Test")
        .tryFlow(new RetryableStep<>(5, new AtomicInteger(), false).build("Retry"))
        .retryOn(RecoverableFlowException.ALL)
        .retryTimes(4)
        .delay(1)
        .build();

    StepVerifier.create(retryableFlow.cloneFlow().run(FlowContext.create()))
        .assertNext(TestUtils.assertAndLog(globalReport -> {
          assertThat(globalReport.getName()).isEqualTo("Test");
          assertThat(globalReport.getContext().get("Success")).isEqualTo("Success");
          assertThat(globalReport.getStatus()).isEqualTo(Status.SUCCESS);
          assertThat(globalReport.getAllRecoveredErrors()).hasSize(4);
          assertThat(globalReport.getAllErrors()).isEmpty();
          assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  void givenErrorOnSomeTriesWithDelay_retryableFlow_shouldSuccess() {
    RetryableFlow<FlowContext> retryableFlow = RetryableFlowBuilder
        .defaultBuilder()
        .named("Test")
        .tryFlow(new RetryableStep<>(5, new AtomicInteger(), false).build("Retry"))
        .retryOn(RecoverableFlowException.ALL)
        .retryTimes(4)
        .delay(100)
        .build();

    StepVerifier.create(retryableFlow.run(FlowContext.create()))
        .assertNext(TestUtils.assertAndLog(globalReport -> {
          assertThat(globalReport.getDurationInMillis()).isGreaterThan(400);
          assertThat(globalReport.getName()).isEqualTo("Test");
          assertThat(globalReport.getContext().get("Success")).isEqualTo("Success");
          assertThat(globalReport.getStatus()).isEqualTo(Status.SUCCESS);
          assertThat(globalReport.getAllRecoveredErrors()).hasSize(4);
          assertThat(globalReport.getAllErrors()).isEmpty();
          assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  void givenErrorOnSomeTriesAndNotRecoverable_retryableFlow_shouldError() {
    RetryableFlow<FlowContext> retryableFlow = RetryableFlowBuilder
        .defaultBuilder()
        .named("Test")
        .tryFlow(new RetryableStep<>(5, new AtomicInteger(), false).build("Retry"))
        .retryOn(RecoverableFlowException.FUNCTIONAL)
        .retryTimes(4)
        .delay(1)
        .build();

    StepVerifier.create(retryableFlow.run(FlowContext.create()))
        .assertNext(TestUtils.assertAndLog(globalReport -> {
          assertThat(globalReport.getName()).isEqualTo("Test");
          assertThat(globalReport.getContext().get("Success")).isNull();
          assertThat(globalReport.getStatus()).isEqualTo(Status.ERROR);
          assertThat(globalReport.getAllRecoveredErrors()).isEmpty();
          assertThat(globalReport.getAllErrors()).hasSize(1);
          assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  void givenErrorOnSomeTries_retryableFlow_shouldWarning() {
    RetryableFlow<FlowContext> retryableFlow = RetryableFlowBuilder
        .defaultBuilder()
        .named("Test")
        .tryFlow(new RetryableStep<>(5, new AtomicInteger(), true).build("Retry"))
        .retryOn(RecoverableFlowException.ALL)
        .retryTimes(4)
        .delay(1)
        .build();

    StepVerifier.create(retryableFlow.run(FlowContext.create()))
        .assertNext(TestUtils.assertAndLog(globalReport -> {
          assertThat(globalReport.getName()).isEqualTo("Test");
          assertThat(globalReport.getContext().get("Success")).isEqualTo("Success");
          assertThat(globalReport.getStatus()).isEqualTo(Status.WARNING);
          assertThat(globalReport.getAllRecoveredErrors()).hasSize(4);
          assertThat(globalReport.getAllErrors()).isEmpty();
          assertThat(globalReport.getAllWarnings()).hasSize(1);
        }))
        .verifyComplete();
  }

  @Test
  void givenErrorOneTry_retryableFlow_shouldSuccess() {
    RetryableFlow<FlowContext> retryableFlow = RetryableFlowBuilder
        .defaultBuilder()
        .named("Test")
        .tryFlow(new RetryableStep<>(2, new AtomicInteger(), false).build("Retry"))
        .retryOn(RecoverableFlowException.ALL)
        .retryTimes(4)
        .delay(1)
        .build();

    StepVerifier.create(retryableFlow.run(FlowContext.create()))
        .assertNext(TestUtils.assertAndLog(globalReport -> {
          assertThat(globalReport.getName()).isEqualTo("Test");
          assertThat(globalReport.getContext().get("Success")).isEqualTo("Success");
          assertThat(globalReport.getStatus()).isEqualTo(Status.SUCCESS);
          assertThat(globalReport.getAllRecoveredErrors()).hasSize(1);
          assertThat(globalReport.getAllErrors()).isEmpty();
          assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  void givenErrorOnAllTries_retryableFlow_shouldSuccess() {
    RetryableFlow<FlowContext> retryableFlow = RetryableFlowBuilder
        .defaultBuilder()
        .named("Test")
        .tryFlow(new RetryableStep<>(6, new AtomicInteger(), true).build("Retry"))
        .retryOn(RecoverableFlowException.ALL)
        .retryTimes(4)
        .delay(1)
        .build();

    StepVerifier.create(retryableFlow.run(FlowContext.create()))
        .assertNext(TestUtils.assertAndLog(globalReport -> {
          assertThat(globalReport.getName()).isEqualTo("Test");
          assertThat(globalReport.getContext().get("Success")).isNull();
          assertThat(globalReport.getStatus()).isEqualTo(Status.ERROR);
          assertThat(globalReport.getAllRecoveredErrors()).hasSize(4);
          assertThat(globalReport.getAllErrors()).hasSize(1);
          assertThat(globalReport.getAllWarnings()).isEmpty();
        }))
        .verifyComplete();
  }

  @Test
  void givenNullName_retryableFlow_shouldNotBuild() {
    assertThatExceptionOfType(FlowBuilderException.class).isThrownBy(() -> RetryableFlowBuilder
        .defaultBuilder()
        .named(null)
        .tryFlow(SuccessStepFlow.flowNamed("Test"))
        .retryOn(RecoverableFlowException.TECHNICAL)
        .retryTimes(1)
        .delay(1)
        .build()
    );
  }

  @Test
  void givenNullTryFlow_retryableFlow_shouldNotBuild() {
    assertThatExceptionOfType(FlowBuilderException.class).isThrownBy(() -> RetryableFlowBuilder
        .defaultBuilder()
        .named("Test")
        .tryFlow(null)
        .retryOn(RecoverableFlowException.TECHNICAL)
        .retryTimes(1)
        .delay(1)
        .build()
    );
  }

  @Test
  void givenNullRetryOn_retryableFlow_shouldNotBuild() {
    assertThatExceptionOfType(FlowBuilderException.class).isThrownBy(() -> RetryableFlowBuilder
        .defaultBuilder()
        .named("Test")
        .tryFlow(SuccessStepFlow.flowNamed("Test"))
        .retryOn(null)
        .retryTimes(1)
        .delay(1)
        .build()
    );
  }

  @Test
  void givenNullRetryTimes_retryableFlow_shouldNotBuild() {
    assertThatExceptionOfType(FlowBuilderException.class).isThrownBy(() -> RetryableFlowBuilder
        .defaultBuilder()
        .named("Test")
        .tryFlow(SuccessStepFlow.flowNamed("Test"))
        .retryOn(RecoverableFlowException.TECHNICAL)
        .retryTimes(null)
        .delay(1)
        .build()
    );
  }

  @Test
  void givenNullDelay_retryableFlow_shouldNotBuild() {
    assertThatExceptionOfType(FlowBuilderException.class).isThrownBy(() -> RetryableFlowBuilder
        .defaultBuilder()
        .named("Test")
        .tryFlow(SuccessStepFlow.flowNamed("Test"))
        .retryOn(RecoverableFlowException.TECHNICAL)
        .retryTimes(1)
        .delay(null)
        .build()
    );
  }

  private static final class RetryableStep<T extends FlowContext> implements Step<T> {
    private final Integer successOnTry;
    private final AtomicInteger counter;
    private final Boolean shouldWarning;

    public RetryableStep(Integer successOnTry, AtomicInteger counter, Boolean shouldWarning) {
      this.counter = counter;
      this.successOnTry = successOnTry;
      this.shouldWarning = shouldWarning;
    }

    @Override
    public Mono<Report<T>> apply(T context, Metadata<Object> metadata) {
      Integer tryNumber = counter.incrementAndGet();

      if (tryNumber.equals(successOnTry)) {
        context.put("Success", "Success");
        if (Boolean.TRUE.equals(shouldWarning)) {
          return Mono.just(Report.successWithWarning(context, new FlowTechnicalException("Warning retry " + tryNumber.toString())));
        }
        return Mono.just(Report.success(context));
      }
      return Mono.just(Report.error(context, new FlowTechnicalException("Error retry " + tryNumber.toString())));
    }
  }
}
