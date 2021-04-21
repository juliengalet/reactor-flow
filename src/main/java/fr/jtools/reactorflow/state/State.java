package fr.jtools.reactorflow.state;

import fr.jtools.reactorflow.exception.FlowException;
import fr.jtools.reactorflow.flow.Flow;
import fr.jtools.reactorflow.utils.console.ConsoleStyle;
import fr.jtools.reactorflow.utils.console.PrettyPrint;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static fr.jtools.reactorflow.utils.console.LoggerUtils.colorize;

/**
 * This class stores the state of a {@link Flow} execution, initialized by {@link Flow#run(T)}.
 *
 * @param <T> Context type
 */
public class State<T extends FlowContext> implements PrettyPrint {
  /**
   * The {@link State} global context.
   */
  private final T context;

  /**
   * The root {@link Flow}, aka the one on which you call {@link Flow#run(T)}.
   */
  private final Flow<T> root;

  /**
   * Create a {@link State} with the initial {@link T} context value.
   *
   * @param initialContext The initial {@link T} context
   * @param root           The {@link State#root} {@link Flow}
   * @param <T>            Context type
   * @return A {@link State}
   */
  public static <T extends FlowContext> State<T> initiate(T initialContext, Flow<T> root) {
    return new State<>(initialContext, root);
  }

  private State(T initialContext, Flow<T> root) {
    this.root = root;
    this.context = initialContext;
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
   * Get {@link State} global {@link State.Status}.
   * It corresponds the {@link State.Status} of {@link State#root}.
   *
   * @return A {@link State.Status}
   */
  public Status getStatus() {
    return this.root.getStatus();
  }

  /**
   * Get all {@link FlowException} errors that occurred during {@link State#root} execution.
   *
   * @return The {@link FlowException} errors
   */
  public List<FlowException> getAllErrors() {
    return this.root.getErrorsForFlowAndChildren();
  }

  /**
   * Get all {@link FlowException} warnings that occurred during {@link State#root} execution.
   *
   * @return The {@link FlowException} warnings
   */
  public List<FlowException> getAllWarnings() {
    return this.root.getWarningsForFlowAndChildren();
  }

  /**
   * Get all {@link FlowException} recovered errors that occurred during {@link State#root} execution.
   *
   * @return The {@link FlowException} recovered errors
   */
  public List<FlowException> getAllRecoveredErrors() {
    return this.root.getRecoveredErrorsForFlowAndChildren();
  }

  /**
   * The global name of the {@link Flow} executed, aka the name of {@link State#root}.
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
   * Get the {@link State#root} {@link Flow} type.
   *
   * @return The {@link State#root} {@link Flow} type
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
        colorize(this.getStatus().name(), State.getStatusConsoleStyle(this.getStatus())),
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
   * Get the {@link ConsoleStyle} matching a {@link State.Status}.
   *
   * @param status A {@link State.Status}
   * @return The {@link ConsoleStyle}
   */
  public static ConsoleStyle getStatusConsoleStyle(State.Status status) {
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

  /**
   * The status that a {@link Flow} can have after its execution or when it is not executed.
   */
  public enum Status {
    /**
     * Non executed {@link Flow}.
     */
    IGNORED,
    /**
     * {@link Flow} executed with at least one warning in itself or in its children.
     */
    WARNING,
    /**
     * {@link Flow} executed with no warning or error in itself or in its children.
     */
    SUCCESS,
    /**
     * {@link Flow} executed with at least one valid error in itself or in its children.
     * An error is valid if it has not be recovered, or if it is not ignore by a {@link fr.jtools.reactorflow.flow.FlowStatusPolicy}.
     */
    ERROR
  }
}
