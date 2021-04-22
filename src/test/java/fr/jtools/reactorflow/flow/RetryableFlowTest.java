package fr.jtools.reactorflow.flow;

import fr.jtools.reactorflow.builder.RetryableFlowBuilder;
import fr.jtools.reactorflow.exception.FlowBuilderException;
import fr.jtools.reactorflow.exception.FlowTechnicalException;
import fr.jtools.reactorflow.exception.RecoverableFlowException;
import fr.jtools.reactorflow.report.FlowContext;
import fr.jtools.reactorflow.report.Metadata;
import fr.jtools.reactorflow.report.Report;
import fr.jtools.reactorflow.report.Status;
import fr.jtools.reactorflow.testutils.CustomContext;
import fr.jtools.reactorflow.testutils.SuccessStepFlow;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.concurrent.atomic.AtomicInteger;

import static fr.jtools.reactorflow.testutils.TestUtils.assertAndLog;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

final class RetryableFlowTest {
  @Test
  final void checkGlobalReportToString() {
    RetryableFlow<FlowContext> retryableFlow = RetryableFlowBuilder
        .defaultBuilder()
        .named("Test")
        .tryFlow(new RetryableStep<>(5, new AtomicInteger(), true).build("Retry"))
        .retryOn(RecoverableFlowException.ALL)
        .retryTimes(4)
        .delay(1)
        .build();

    StepVerifier.create(retryableFlow.run(FlowContext.create()))
        .assertNext(assertAndLog(globalReport -> assertThat(globalReport.toString()).isNotNull()))
        .verifyComplete();
  }

  @Test
  final void checkGlobalReportToPrettyString() {
    RetryableFlow<FlowContext> retryableFlow = RetryableFlowBuilder
        .defaultBuilder()
        .named("Test")
        .tryFlow(new RetryableStep<>(5, new AtomicInteger(), true).build("Retry"))
        .retryOn(RecoverableFlowException.ALL)
        .retryTimes(4)
        .delay(1)
        .build();

    StepVerifier.create(retryableFlow.run(FlowContext.create()))
        .assertNext(assertAndLog(globalReport -> assertThat(globalReport.toPrettyString()).isNotNull()))
        .verifyComplete();
  }

  @Test
  final void checkGlobalReportToTreeString() {
    RetryableFlow<FlowContext> retryableFlow = RetryableFlowBuilder
        .defaultBuilder()
        .named("Test")
        .tryFlow(new RetryableStep<>(5, new AtomicInteger(), true).build("Retry"))
        .retryOn(RecoverableFlowException.ALL)
        .retryTimes(4)
        .delay(1)
        .build();

    StepVerifier.create(retryableFlow.run(FlowContext.create()))
        .assertNext(assertAndLog(globalReport -> assertThat(globalReport.toTreeString()).isNotNull()))
        .verifyComplete();
  }

  @Test
  final void checkGlobalReportToPrettyTreeString() {
    RetryableFlow<FlowContext> retryableFlow = RetryableFlowBuilder
        .defaultBuilder()
        .named("Test")
        .tryFlow(new RetryableStep<>(5, new AtomicInteger(), true).build("Retry"))
        .retryOn(RecoverableFlowException.ALL)
        .retryTimes(4)
        .delay(1)
        .build();

    StepVerifier.create(retryableFlow.run(FlowContext.create()))
        .assertNext(assertAndLog(globalReport -> assertThat(globalReport.toPrettyTreeString()).isNotNull()))
        .verifyComplete();
  }

  @Test
  final void checkGlobalReportToPrettyExceptionsRaisedString() {
    RetryableFlow<FlowContext> retryableFlow = RetryableFlowBuilder
        .defaultBuilder()
        .named("Test")
        .tryFlow(new RetryableStep<>(5, new AtomicInteger(), true).build("Retry"))
        .retryOn(RecoverableFlowException.ALL)
        .retryTimes(4)
        .delay(1)
        .build();

    StepVerifier.create(retryableFlow.run(FlowContext.create()))
        .assertNext(assertAndLog(globalReport -> assertThat(globalReport.toPrettyExceptionsRaisedString()).isNotNull()))
        .verifyComplete();
  }

  @Test
  final void checkGlobalReportToExceptionsRaisedStringWithError() {
    RetryableFlow<FlowContext> retryableFlow = RetryableFlowBuilder
        .defaultBuilder()
        .named("Test")
        .tryFlow(new RetryableStep<>(5, new AtomicInteger(), true).build("Retry"))
        .retryOn(RecoverableFlowException.ALL)
        .retryTimes(2)
        .delay(1)
        .build();

    StepVerifier.create(retryableFlow.run(FlowContext.create()))
        .assertNext(assertAndLog(globalReport -> assertThat(globalReport.toExceptionsRaisedString()).isNotNull()))
        .verifyComplete();
  }

  @Test
  final void checkGlobalReportToExceptionsRaisedStringWithSuccess() {
    RetryableFlow<FlowContext> retryableFlow = RetryableFlowBuilder
        .defaultBuilder()
        .named("Test")
        .tryFlow(new RetryableStep<>(1, new AtomicInteger(), false).build("Retry"))
        .retryOn(RecoverableFlowException.ALL)
        .retryTimes(2)
        .delay(1)
        .build();

    StepVerifier.create(retryableFlow.run(FlowContext.create()))
        .assertNext(assertAndLog(globalReport -> assertThat(globalReport.toExceptionsRaisedString()).isNotNull()))
        .verifyComplete();
  }


  @Test
  final void checkGlobalReportToExceptionsRaisedString() {
    RetryableFlow<FlowContext> retryableFlow = RetryableFlowBuilder
        .defaultBuilder()
        .named("Test")
        .tryFlow(new RetryableStep<>(5, new AtomicInteger(), true).build("Retry"))
        .retryOn(RecoverableFlowException.ALL)
        .retryTimes(4)
        .delay(1)
        .build();

    StepVerifier.create(retryableFlow.run(FlowContext.create()))
        .assertNext(assertAndLog(globalReport -> assertThat(globalReport.toExceptionsRaisedString()).isNotNull()))
        .verifyComplete();
  }

  @Test
  final void givenSuccessOnFirstTry_retryableFlow_shouldSuccess() {
    RetryableFlow<FlowContext> retryableFlow = RetryableFlowBuilder
        .defaultBuilder()
        .named("Test")
        .tryFlow(new RetryableStep<>(1, new AtomicInteger(), false).build("Retry"))
        .retryOn(RecoverableFlowException.ALL)
        .retryTimes(1)
        .delay(1)
        .build();

    StepVerifier.create(retryableFlow.run(FlowContext.create()))
        .assertNext(assertAndLog(globalReport -> {
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
  final void givenWarningOnFirstTry_retryableFlow_shouldWarning() {
    RetryableFlow<FlowContext> retryableFlow = RetryableFlowBuilder
        .defaultBuilder()
        .named("Test")
        .tryFlow(new RetryableStep<>(1, new AtomicInteger(), true).build("Retry"))
        .retryOn(RecoverableFlowException.ALL)
        .retryTimes(1)
        .delay(1)
        .build();

    StepVerifier.create(retryableFlow.run(FlowContext.create()))
        .assertNext(assertAndLog(globalReport -> {
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
  final void givenErrorOnSomeTries_retryableFlow_shouldSuccess() {
    RetryableFlow<FlowContext> retryableFlow = RetryableFlowBuilder
        .defaultBuilder()
        .named("Test")
        .tryFlow(new RetryableStep<>(5, new AtomicInteger(), false).build("Retry"))
        .retryOn(RecoverableFlowException.ALL)
        .retryTimes(4)
        .delay(1)
        .build();

    StepVerifier.create(retryableFlow.run(FlowContext.create()))
        .assertNext(assertAndLog(globalReport -> {
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
  final void givenErrorOnSomeTriesAndCustomContext_retryableFlow_shouldSuccess() {
    RetryableFlow<CustomContext> retryableFlow = RetryableFlowBuilder
        .builderForContextOfType(CustomContext.class)
        .named("Test")
        .tryFlow(new RetryableStep<CustomContext>(5, new AtomicInteger(), false).build("Retry"))
        .retryOn(RecoverableFlowException.ALL)
        .retryTimes(4)
        .delay(1)
        .build();

    StepVerifier.create(retryableFlow.run(new CustomContext()))
        .assertNext(assertAndLog(globalReport -> {
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
  final void givenErrorOnSomeTriesClonedWithNewName_retryableFlow_shouldSuccess() {
    RetryableFlow<FlowContext> retryableFlow = RetryableFlowBuilder
        .defaultBuilder()
        .named("Test")
        .tryFlow(new RetryableStep<>(5, new AtomicInteger(), false).build("Retry"))
        .retryOn(RecoverableFlowException.ALL)
        .retryTimes(4)
        .delay(1)
        .build();

    StepVerifier.create(retryableFlow.cloneFlow("Test copy").run(FlowContext.create()))
        .assertNext(assertAndLog(globalReport -> {
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
  final void givenErrorOnSomeTriesCloned_retryableFlow_shouldSuccess() {
    RetryableFlow<FlowContext> retryableFlow = RetryableFlowBuilder
        .defaultBuilder()
        .named("Test")
        .tryFlow(new RetryableStep<>(5, new AtomicInteger(), false).build("Retry"))
        .retryOn(RecoverableFlowException.ALL)
        .retryTimes(4)
        .delay(1)
        .build();

    StepVerifier.create(retryableFlow.cloneFlow().run(FlowContext.create()))
        .assertNext(assertAndLog(globalReport -> {
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
  final void givenErrorOnSomeTriesWithDelay_retryableFlow_shouldSuccess() {
    RetryableFlow<FlowContext> retryableFlow = RetryableFlowBuilder
        .defaultBuilder()
        .named("Test")
        .tryFlow(new RetryableStep<>(5, new AtomicInteger(), false).build("Retry"))
        .retryOn(RecoverableFlowException.ALL)
        .retryTimes(4)
        .delay(100)
        .build();

    StepVerifier.create(retryableFlow.run(FlowContext.create()))
        .assertNext(assertAndLog(globalReport -> {
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
  final void givenErrorOnSomeTriesAndNotRecoverable_retryableFlow_shouldError() {
    RetryableFlow<FlowContext> retryableFlow = RetryableFlowBuilder
        .defaultBuilder()
        .named("Test")
        .tryFlow(new RetryableStep<>(5, new AtomicInteger(), false).build("Retry"))
        .retryOn(RecoverableFlowException.FUNCTIONAL)
        .retryTimes(4)
        .delay(1)
        .build();

    StepVerifier.create(retryableFlow.run(FlowContext.create()))
        .assertNext(assertAndLog(globalReport -> {
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
  final void givenErrorOnSomeTries_retryableFlow_shouldWarning() {
    RetryableFlow<FlowContext> retryableFlow = RetryableFlowBuilder
        .defaultBuilder()
        .named("Test")
        .tryFlow(new RetryableStep<>(5, new AtomicInteger(), true).build("Retry"))
        .retryOn(RecoverableFlowException.ALL)
        .retryTimes(4)
        .delay(1)
        .build();

    StepVerifier.create(retryableFlow.run(FlowContext.create()))
        .assertNext(assertAndLog(globalReport -> {
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
  final void givenErrorOneTry_retryableFlow_shouldSuccess() {
    RetryableFlow<FlowContext> retryableFlow = RetryableFlowBuilder
        .defaultBuilder()
        .named("Test")
        .tryFlow(new RetryableStep<>(2, new AtomicInteger(), false).build("Retry"))
        .retryOn(RecoverableFlowException.ALL)
        .retryTimes(4)
        .delay(1)
        .build();

    StepVerifier.create(retryableFlow.run(FlowContext.create()))
        .assertNext(assertAndLog(globalReport -> {
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
  final void givenErrorOnAllTries_retryableFlow_shouldSuccess() {
    RetryableFlow<FlowContext> retryableFlow = RetryableFlowBuilder
        .defaultBuilder()
        .named("Test")
        .tryFlow(new RetryableStep<>(6, new AtomicInteger(), true).build("Retry"))
        .retryOn(RecoverableFlowException.ALL)
        .retryTimes(4)
        .delay(1)
        .build();

    StepVerifier.create(retryableFlow.run(FlowContext.create()))
        .assertNext(assertAndLog(globalReport -> {
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
  final void givenNullName_retryableFlow_shouldNotBuild() {
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
  final void givenNullTryFlow_retryableFlow_shouldNotBuild() {
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
  final void givenNullRetryOn_retryableFlow_shouldNotBuild() {
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
  final void givenNullRetryTimes_retryableFlow_shouldNotBuild() {
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
  final void givenNullDelay_retryableFlow_shouldNotBuild() {
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
