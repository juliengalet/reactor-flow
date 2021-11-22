package io.github.juliengalet.reactorflow.report;

import io.github.juliengalet.reactorflow.exception.FlowException;
import io.github.juliengalet.reactorflow.flow.Flow;
import io.github.juliengalet.reactorflow.utils.ConsoleStyle;
import io.github.juliengalet.reactorflow.utils.PrettyPrint;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.github.juliengalet.reactorflow.utils.LoggerUtils.colorize;

/**
 * This class stores the global report of a {@link Flow} execution, created by {@link Flow#run(FlowContext)}.
 *
 * @param <T> Context type
 */
public class GlobalReport<T extends FlowContext> implements PrettyPrint {
  public static final String ERROR_TEMPLATE = "%s - %s";
  /**
   * The {@link GlobalReport} global context.
   */
  private final T context;

  /**
   * The root {@link Flow}, aka the one on which you call {@link Flow#run(T)}.
   */
  private final Flow<T> rootFlow;

  /**
   * Create a {@link GlobalReport} with the {@link T} context value.
   *
   * @param context The {@link T} context
   * @param rootFlow    The {@link GlobalReport#rootFlow} {@link Flow}
   * @param <T>     Context type
   * @return A {@link GlobalReport}
   */
  public static <T extends FlowContext> GlobalReport<T> create(T context, Flow<T> rootFlow) {
    return new GlobalReport<>(context, rootFlow);
  }

  private GlobalReport(T context, Flow<T> rootFlow) {
    this.rootFlow = rootFlow;
    this.context = context;
  }

  /**
   * Get {@link T} context.
   *
   * @return The {@link T} context
   */
  public T getContext() {
    return this.context;
  }

  /**
   * Get {@link GlobalReport} global {@link Status}.
   * It corresponds the {@link Status} of {@link GlobalReport#rootFlow}.
   *
   * @return A {@link Status}
   */
  public Status getStatus() {
    return this.rootFlow.getStatus();
  }

  /**
   * Get all {@link FlowException} errors that occurred during {@link GlobalReport#rootFlow} execution.
   *
   * @return The {@link FlowException} errors
   */
  public List<FlowException> getAllErrors() {
    return this.rootFlow.getErrorsForFlowAndChildren();
  }

  /**
   * Get all {@link FlowException} warnings that occurred during {@link GlobalReport#rootFlow} execution.
   *
   * @return The {@link FlowException} warnings
   */
  public List<FlowException> getAllWarnings() {
    return this.rootFlow.getWarningsForFlowAndChildren();
  }

  /**
   * Get all {@link FlowException} recovered errors that occurred during {@link GlobalReport#rootFlow} execution.
   *
   * @return The {@link FlowException} recovered errors
   */
  public List<FlowException> getAllRecoveredErrors() {
    return this.rootFlow.getRecoveredErrorsForFlowAndChildren();
  }

  /**
   * The global name of the {@link Flow} executed, aka the name of {@link GlobalReport#rootFlow}.
   *
   * @return The name
   */
  public String getName() {
    return this.rootFlow.getName();
  }

  /**
   * Get the global duration of the {@link Flow} in milliseconds.
   *
   * @return The duration as a {@link Double}
   */
  public Double getDurationInMillis() {
    return this.rootFlow.getDurationInMillis();
  }

  /**
   * Get the {@link GlobalReport#rootFlow} {@link Flow} type.
   *
   * @return The {@link GlobalReport#rootFlow} {@link Flow} type
   */
  public String getRootType() {
    return this.rootFlow.getClass().getSimpleName();
  }

  /**
   * Get a string representation of the summary result of the executed {@link Flow}.
   *
   * @return A {@link String} representing the {@link Flow}
   */
  @Override
  public String toString() {
    return String.format(
        "Summary%n%s - %s named %s ended in %s (%s)%n",
        this.getStatus().name(),
        this.getRootType(),
        this.getName(),
        String.format(Locale.US, "%.2f ms", this.getDurationInMillis()),
        this.rootFlow.hashCode()
    );
  }

  /**
   * Get a colorized string representation of the summary result of the executed {@link Flow}.
   *
   * @return A {@link String} representing the {@link Flow}
   */
  @Override
  public String toPrettyString() {
    return String.format(
        "%s%n%s - %s named %s ended in %s (%s)%n",
        colorize("Summary", ConsoleStyle.MAGENTA_BOLD),
        colorize(this.getStatus().name(), GlobalReport.getStatusConsoleStyle(this.getStatus())),
        colorize(this.getRootType(), ConsoleStyle.BLUE_BOLD),
        colorize(this.getName(), ConsoleStyle.WHITE_BOLD),
        colorize(String.format(Locale.US, "%.2f ms", this.getDurationInMillis()), ConsoleStyle.MAGENTA_BOLD),
        colorize(String.valueOf(this.rootFlow.hashCode()), ConsoleStyle.BLACK_BOLD)
    );
  }

  /**
   * Get a string representation of the result of the executed {@link Flow}, in a tree containing all its subflows.
   *
   * @return A {@link String} representing the tree
   */
  public String toTreeString() {
    return this.rootFlow.toTreeString();
  }

  /**
   * Get a colorized string representation of the result of the executed {@link Flow}, in a tree containing all its subflows.
   *
   * @return A {@link String} representing the tree
   */
  public String toPrettyTreeString() {
    return this.rootFlow.toPrettyTreeString();
  }

  /**
   * Get a colorized string representation of the exceptions raised by the executed {@link Flow}, or its subflows.
   *
   * @return A {@link String} representing the exceptions
   */
  public String toPrettyExceptionsRaisedString() {
    List<String> exceptionMessages = Stream
        .concat(
            this.rootFlow.getErrorsForFlowAndChildren()
                .stream()
                .map(error -> String.format(
                    ERROR_TEMPLATE,
                    colorize(Status.ERROR.name(), ConsoleStyle.RED_BOLD),
                    error.toPrettyString()
                )),
            Stream.concat(
                this.rootFlow.getWarningsForFlowAndChildren()
                    .stream()
                    .map(error -> String.format(
                        ERROR_TEMPLATE,
                        colorize(Status.WARNING.name(), ConsoleStyle.YELLOW_BOLD),
                        error.toPrettyString()
                    )),
                this.rootFlow.getRecoveredErrorsForFlowAndChildren()
                    .stream()
                    .map(error -> String.format(
                        ERROR_TEMPLATE,
                        colorize("RECOVERED", ConsoleStyle.WHITE_BOLD),
                        error.toPrettyString()
                    ))
            )
        )
        .collect(Collectors.toList());

    return String.format(
        "%s%n%s%n",
        colorize("Errors and warnings", ConsoleStyle.MAGENTA_BOLD),
        exceptionMessages.isEmpty() ?
            colorize("No error or warning", ConsoleStyle.GREEN_BOLD) :
            String.join("\n", exceptionMessages)
    );
  }

  /**
   * Get a string representation of the exceptions raised by the executed {@link Flow}, or its subflows.
   *
   * @return A {@link String} representing the exceptions
   */
  public String toExceptionsRaisedString() {
    List<String> exceptionMessages = Stream
        .concat(
            this.rootFlow.getErrorsForFlowAndChildren()
                .stream()
                .map(error -> String.format(
                    ERROR_TEMPLATE,
                    Status.ERROR.name(),
                    error.toString()
                )),
            Stream.concat(
                this.rootFlow.getWarningsForFlowAndChildren()
                    .stream()
                    .map(error -> String.format(
                        ERROR_TEMPLATE,
                        Status.WARNING.name(),
                        error.toString()
                    )),
                this.rootFlow.getRecoveredErrorsForFlowAndChildren()
                    .stream()
                    .map(error -> String.format(
                        ERROR_TEMPLATE,
                        "RECOVERED",
                        error.toString()
                    ))
            )
        )
        .collect(Collectors.toList());

    return String.format(
        "%s%n%s%n",
        "Errors and warnings",
        exceptionMessages.isEmpty() ?
            "No error or warning" :
            String.join("\n", exceptionMessages)
    );
  }

  /**
   * Get the {@link ConsoleStyle} matching a {@link Status}.
   *
   * @param status A {@link Status}
   * @return The {@link ConsoleStyle}
   */
  public static ConsoleStyle getStatusConsoleStyle(Status status) {
    switch (status) {
      case SUCCESS:
        return ConsoleStyle.GREEN_BOLD;
      case ERROR:
        return ConsoleStyle.RED_BOLD;
      case WARNING:
        return ConsoleStyle.YELLOW_BOLD;
      default:
        return ConsoleStyle.WHITE_BOLD;
    }
  }
}
