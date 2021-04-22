package fr.jtools.reactorflow.report;

import fr.jtools.reactorflow.exception.FlowFunctionalException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

final class ReportTest {
  @Test
  final void success_shouldBuildReport() {
    FlowContext context = FlowContext.create();
    Report<FlowContext> report = Report.success(context);

    assertThat(report.getContext()).isEqualTo(context);
    assertThat(report.getErrors()).isEmpty();
    assertThat(report.getWarnings()).isEmpty();
  }

  @Test
  final void successWithWarning_shouldBuildReport() {
    FlowContext context = FlowContext.create();
    Report<FlowContext> report = Report.successWithWarning(
        context,
        new FlowFunctionalException("Warning")
    );

    assertThat(report.getContext()).isEqualTo(context);
    assertThat(report.getErrors()).isEmpty();
    assertThat(report.getWarnings()).hasSize(1);
  }

  @Test
  final void successWithWarnings_shouldBuildReport() {
    FlowContext context = FlowContext.create();
    Report<FlowContext> report = Report.successWithWarning(
        context,
        List.of(
            new FlowFunctionalException("Warning"),
            new FlowFunctionalException("Warning 2")
        ));

    assertThat(report.getContext()).isEqualTo(context);
    assertThat(report.getErrors()).isEmpty();
    assertThat(report.getWarnings()).hasSize(2);
  }

  @Test
  final void error_shouldBuildReport() {
    FlowContext context = FlowContext.create();
    Report<FlowContext> report = Report.error(
        context,
        new FlowFunctionalException("Error")
    );

    assertThat(report.getContext()).isEqualTo(context);
    assertThat(report.getWarnings()).isEmpty();
    assertThat(report.getErrors()).hasSize(1);
  }

  @Test
  final void errors_shouldBuildReport() {
    FlowContext context = FlowContext.create();
    Report<FlowContext> report = Report.error(
        context,
        List.of(
            new FlowFunctionalException("Error"),
            new FlowFunctionalException("Error 2")
        ));

    assertThat(report.getContext()).isEqualTo(context);
    assertThat(report.getWarnings()).isEmpty();
    assertThat(report.getErrors()).hasSize(2);
  }

  @Test
  final void errorWithWarning_shouldBuildReport() {
    FlowContext context = FlowContext.create();
    Report<FlowContext> report = Report.errorWithWarning(
        context,
        new FlowFunctionalException("Error"),
        new FlowFunctionalException("Warning")
    );

    assertThat(report.getContext()).isEqualTo(context);
    assertThat(report.getWarnings()).hasSize(1);
    assertThat(report.getErrors()).hasSize(1);
  }

  @Test
  final void errorsWithWarning_shouldBuildReport() {
    FlowContext context = FlowContext.create();
    Report<FlowContext> report = Report.errorWithWarning(
        context,
        List.of(
            new FlowFunctionalException("Error"),
            new FlowFunctionalException("Error 2")
        ),
        new FlowFunctionalException("Warning")
    );

    assertThat(report.getContext()).isEqualTo(context);
    assertThat(report.getWarnings()).hasSize(1);
    assertThat(report.getErrors()).hasSize(2);
  }

  @Test
  final void errorWithWarnings_shouldBuildReport() {
    FlowContext context = FlowContext.create();
    Report<FlowContext> report = Report.errorWithWarning(
        context,
        new FlowFunctionalException("Error"),
        List.of(
            new FlowFunctionalException("Warning"),
            new FlowFunctionalException("Warning 2")
        ));

    assertThat(report.getContext()).isEqualTo(context);
    assertThat(report.getWarnings()).hasSize(2);
    assertThat(report.getErrors()).hasSize(1);
  }

  @Test
  final void errorsWithWarnings_shouldBuildReport() {
    FlowContext context = FlowContext.create();
    Report<FlowContext> report = Report.errorWithWarning(
        context,
        List.of(
            new FlowFunctionalException("Error"),
            new FlowFunctionalException("Error 2")
        ),
        List.of(
            new FlowFunctionalException("Warning"),
            new FlowFunctionalException("Warning 2")
        ));

    assertThat(report.getContext()).isEqualTo(context);
    assertThat(report.getWarnings()).hasSize(2);
    assertThat(report.getErrors()).hasSize(2);
  }
}
