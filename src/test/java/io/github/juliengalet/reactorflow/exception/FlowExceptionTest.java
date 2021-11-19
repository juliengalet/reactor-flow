package io.github.juliengalet.reactorflow.exception;

import io.github.juliengalet.reactorflow.builder.StepFlowBuilder;
import io.github.juliengalet.reactorflow.flow.NoOpFlow;
import io.github.juliengalet.reactorflow.report.FlowContext;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

final class FlowExceptionTest {
  @Test
  void buildFlowTechnicalExceptionWithRootCause() {
    FlowTechnicalException flowTechnicalException = new FlowTechnicalException(new RuntimeException("Error"), "Message");

    assertThat(flowTechnicalException.toString()).isNotNull();
    assertThat(flowTechnicalException.toPrettyString()).isNotNull();
    assertThat(flowTechnicalException.getCause().getMessage()).isEqualTo("Error");
    assertThat(flowTechnicalException.getMessage()).isEqualTo("Message");
    assertThat(flowTechnicalException.getType()).isEqualTo(FlowExceptionType.TECHNICAL);
  }

  @Test
  void buildFlowTechnicalExceptionWithRootCauseAndFlowConcerned() {
    NoOpFlow<FlowContext> noOpFlow = NoOpFlow.named("NoOp");
    FlowException flowException = new FlowTechnicalException(new RuntimeException("Error"), "Message").flowConcerned(noOpFlow);

    assertThat(flowException.toString()).isNotNull();
    assertThat(flowException.toPrettyString()).isNotNull();
    assertThat(flowException.getCause().getMessage()).isEqualTo("Error");
    assertThat(flowException.getMessage()).isEqualTo("Message");
    assertThat(flowException.getType()).isEqualTo(FlowExceptionType.TECHNICAL);
    assertThat(flowException.getFlowConcerned()).isEqualTo(noOpFlow);
  }


  @Test
  void buildFlowTechnicalExceptionWithoutRootCause() {
    FlowTechnicalException flowTechnicalException = new FlowTechnicalException("Message");

    assertThat(flowTechnicalException.toString()).isNotNull();
    assertThat(flowTechnicalException.toPrettyString()).isNotNull();
    assertThat(flowTechnicalException.getCause()).isNull();
    assertThat(flowTechnicalException.getMessage()).isEqualTo("Message");
    assertThat(flowTechnicalException.getType()).isEqualTo(FlowExceptionType.TECHNICAL);
  }

  @Test
  void buildFlowFunctionalExceptionWithRootCause() {
    FlowFunctionalException flowFunctionalException = new FlowFunctionalException(new RuntimeException("Error"), "Message");

    assertThat(flowFunctionalException.toString()).isNotNull();
    assertThat(flowFunctionalException.toPrettyString()).isNotNull();
    assertThat(flowFunctionalException.getCause().getMessage()).isEqualTo("Error");
    assertThat(flowFunctionalException.getMessage()).isEqualTo("Message");
    assertThat(flowFunctionalException.getType()).isEqualTo(FlowExceptionType.FUNCTIONAL);
  }

  @Test
  void buildFlowFunctionalExceptionWithoutRootCause() {
    FlowFunctionalException flowFunctionalException = new FlowFunctionalException("Message");

    assertThat(flowFunctionalException.toString()).isNotNull();
    assertThat(flowFunctionalException.toPrettyString()).isNotNull();
    assertThat(flowFunctionalException.getCause()).isNull();
    assertThat(flowFunctionalException.getMessage()).isEqualTo("Message");
    assertThat(flowFunctionalException.getType()).isEqualTo(FlowExceptionType.FUNCTIONAL);
  }

  @Test
  void buildFlowBuilderExceptionWithRootCause() {
    FlowBuilderException flowBuilderException = new FlowBuilderException(new RuntimeException("Error"), StepFlowBuilder.class, "Message");

    assertThat(flowBuilderException.toString()).isNotNull();
    assertThat(flowBuilderException.toPrettyString()).isNotNull();
    assertThat(flowBuilderException.getCause().getMessage()).isEqualTo("Error");
    assertThat(flowBuilderException.getMessage()).isEqualTo("StepFlowBuilder: Message");
    assertThat(flowBuilderException.getType()).isEqualTo(FlowExceptionType.BUILDER);
  }

  @Test
  void buildFlowBuilderExceptionWithoutRootCause() {
    FlowBuilderException flowBuilderException = new FlowBuilderException(StepFlowBuilder.class, "Message");

    assertThat(flowBuilderException.toString()).isNotNull();
    assertThat(flowBuilderException.toPrettyString()).isNotNull();
    assertThat(flowBuilderException.getCause()).isNull();
    assertThat(flowBuilderException.getMessage()).isEqualTo("StepFlowBuilder: Message");
    assertThat(flowBuilderException.getType()).isEqualTo(FlowExceptionType.BUILDER);
  }

  @Test
  void givenTechnicalException_isRecoverable_shouldReturnRightBooleans() {
    assertThat(new FlowTechnicalException("Message").isRecoverable(RecoverableFlowException.ALL)).isTrue();
    assertThat(new FlowTechnicalException("Message").isRecoverable(RecoverableFlowException.TECHNICAL)).isTrue();
    assertThat(new FlowTechnicalException("Message").isRecoverable(RecoverableFlowException.FUNCTIONAL)).isFalse();
    assertThat(new FlowTechnicalException("Message").isRecoverable(RecoverableFlowException.NONE)).isFalse();
  }

  @Test
  void givenFunctionalException_isRecoverable_shouldReturnRightBooleans() {
    assertThat(new FlowFunctionalException("Message").isRecoverable(RecoverableFlowException.ALL)).isTrue();
    assertThat(new FlowFunctionalException("Message").isRecoverable(RecoverableFlowException.TECHNICAL)).isFalse();
    assertThat(new FlowFunctionalException("Message").isRecoverable(RecoverableFlowException.FUNCTIONAL)).isTrue();
    assertThat(new FlowFunctionalException("Message").isRecoverable(RecoverableFlowException.NONE)).isFalse();
  }

  @Test
  void givenBuilderException_isRecoverable_shouldReturnRightBooleans() {
    assertThat(new FlowBuilderException(StepFlowBuilder.class, "Message").isRecoverable(RecoverableFlowException.ALL)).isTrue();
    assertThat(new FlowBuilderException(StepFlowBuilder.class, "Message").isRecoverable(RecoverableFlowException.TECHNICAL)).isFalse();
    assertThat(new FlowBuilderException(StepFlowBuilder.class, "Message").isRecoverable(RecoverableFlowException.FUNCTIONAL)).isFalse();
    assertThat(new FlowBuilderException(StepFlowBuilder.class, "Message").isRecoverable(RecoverableFlowException.NONE)).isFalse();
  }
}
