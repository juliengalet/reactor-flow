package fr.jtools.reactorflow.report;

import fr.jtools.reactorflow.exception.FlowException;
import fr.jtools.reactorflow.flow.Flow;
import fr.jtools.reactorflow.utils.ConsoleStyle;
import fr.jtools.reactorflow.utils.PrettyPrint;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static fr.jtools.reactorflow.utils.LoggerUtils.colorize;

/**
 * This class stores the global report of a {@link Flow} execution, created by {@link Flow#run(FlowContext)}.
 *
 * @param <T> Context type
 */
public class GlobalReport<T extends FlowContext> implements PrettyPrint {
  /**
   * The {@link GlobalReport} global context.
   */
  private final T context;

  /**
   * The root {@link Flow}, aka the one on which you call {@link Flow#run(T)}.
   */
  private final Flow<T> root;

  /**
   * Create a {@link GlobalReport} with the {@link T} context value.
   *
   * @param context The {@link T} context
   * @param root    The {@link GlobalReport#root} {@link Flow}
   * @param <T>     Context type
   * @return A {@link GlobalReport}
   */
  public static <T extends FlowContext> GlobalReport<T> create(T context, Flow<T> root) {
    return new GlobalReport<>(context, root);
  }

  private GlobalReport(T context, Flow<T> root) {
    this.root = root;
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
   * It corresponds the {@link Status} of {@link GlobalReport#root}.
   *
   * @return A {@link Status}
   */
  public Status getStatus() {
    return this.root.getStatus();
  }

  /**
   * Get all {@link FlowException} errors that occurred during {@link GlobalReport#root} execution.
   *
   * @return The {@link FlowException} errors
   */
  public List<FlowException> getAllErrors() {
    return this.root.getErrorsForFlowAndChildren();
  }

  /**
   * Get all {@link FlowException} warnings that occurred during {@link GlobalReport#root} execution.
   *
   * @return The {@link FlowException} warnings
   */
  public List<FlowException> getAllWarnings() {
    return this.root.getWarningsForFlowAndChildren();
  }

  /**
   * Get all {@link FlowException} recovered errors that occurred during {@link GlobalReport#root} execution.
   *
   * @return The {@link FlowException} recovered errors
   */
  public List<FlowException> getAllRecoveredErrors() {
    return this.root.getRecoveredErrorsForFlowAndChildren();
  }

  /**
   * The global name of the {@link Flow} executed, aka the name of {@link GlobalReport#root}.
   *
   * @return The name
   */
  public String getName() {
    return this.root.getName();
  }

  /**
   * Get the global duration of the {@link Flow} in milliseconds.
   *
   * @return The duration as a {@link Double}
   */
  public Double getDurationInMillis() {
    return this.root.getDurationInMillis();
  }

  /**
   * Get the {@link GlobalReport#root} {@link Flow} type.
   *
   * @return The {@link GlobalReport#root} {@link Flow} type
   */
  public String getRootType() {
    return this.root.getClass().getSimpleName();
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
        this.root.hashCode()
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
        colorize(String.valueOf(this.root.hashCode()), ConsoleStyle.BLACK_BOLD)
    );
  }

  /**
   * Get a string representation of the result of the executed {@link Flow}, in a tree containing all its subflows.
   *
   * @return A {@link String} representing the tree
   */
  public String toTreeString() {
    return this.root.toTreeString();
  }

  /**
   * Get a colorized string representation of the result of the executed {@link Flow}, in a tree containing all its subflows.
   *
   * @return A {@link String} representing the tree
   */
  public String toPrettyTreeString() {
    return this.root.toPrettyTreeString();
  }

  /**
   * Get a colorized string representation of the exceptions raised by the executed {@link Flow}, or its subflows.
   *
   * @return A {@link String} representing the exceptions
   */
  public String toPrettyExceptionsRaisedString() {
    List<String> exceptionMessages = Stream
        .concat(
            this.root.getErrorsForFlowAndChildren()
                .stream()
                .map(error -> String.format(
                    "%s - %s",
                    colorize(Status.ERROR.name(), ConsoleStyle.RED_BOLD),
                    error.toPrettyString()
                )),
            Stream.concat(
                this.root.getWarningsForFlowAndChildren()
                    .stream()
                    .map(error -> String.format(
                        "%s - %s",
                        colorize(Status.WARNING.name(), ConsoleStyle.YELLOW_BOLD),
                        error.toPrettyString()
                    )),
                this.root.getRecoveredErrorsForFlowAndChildren()
                    .stream()
                    .map(error -> String.format(
                        "%s - %s",
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
            this.root.getErrorsForFlowAndChildren()
                .stream()
                .map(error -> String.format(
                    "%s - %s",
                    Status.ERROR.name(),
                    error.toString()
                )),
            Stream.concat(
                this.root.getWarningsForFlowAndChildren()
                    .stream()
                    .map(error -> String.format(
                        "%s - %s",
                        Status.WARNING.name(),
                        error.toString()
                    )),
                this.root.getRecoveredErrorsForFlowAndChildren()
                    .stream()
                    .map(error -> String.format(
                        "%s - %s",
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
