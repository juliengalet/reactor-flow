package fr.jtools.reactorflow.state;

import fr.jtools.reactorflow.exception.FlowBuilderException;
import fr.jtools.reactorflow.exception.FlowException;
import fr.jtools.reactorflow.flow.Flow;
import fr.jtools.reactorflow.utils.ConsoleStyle;
import fr.jtools.reactorflow.utils.PrettyPrint;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static fr.jtools.reactorflow.utils.LoggerUtils.colorize;

public class State<T extends FlowContext> implements PrettyPrint {
  private final T context;
  private Flow<T> root;

  public static <T extends FlowContext> State<T> initiate(T initialContext) {
    return new State<>(initialContext);
  }

  public static State<FlowContext> initiateDefaultFrom(FlowContext initialContext) {
    return new State<>(initialContext);
  }

  public static State<FlowContext> initiateDefault() {
    return new State<>(FlowContext.create());
  }

  public State(T initialContext) {
    this.context = initialContext;
  }

  public T getContext() {
    return this.context;
  }

  public Status getStatus() {
    return this.root.getStatus();
  }

  public List<FlowException> getAllErrors() {
    return List.copyOf(this.root.getErrorsForFlowAndChildren());
  }

  public List<FlowException> getAllWarnings() {
    return List.copyOf(this.root.getWarningsForFlowAndChildren());
  }

  public List<FlowException> getAllRecoveredErrors() {
    return List.copyOf(this.root.getRecoveredErrorsForFlowAndChildren());
  }

  public String getName() {
    return this.root.getName();
  }

  public void setRoot(Flow<T> flow) {
    if (Objects.nonNull(this.root)) {
      throw new FlowBuilderException(State.class, "root can not be set two times");
    }
    this.root = flow;
  }

  @Override
  public String toString() {
    return String.format(
        "Summary%n%s - %s named %s ended in %s (%s)%n",
        this.root.getStatus().name(),
        this.root.getClass().getSimpleName(),
        this.root.getName(),
        String.format(Locale.US, "%.2f ms", this.root.getDurationInMillis()),
        this.root.hashCode()
    );
  }

  @Override
  public String toPrettyString() {
    return String.format(
        "%s%n%s - %s named %s ended in %s (%s)%n",
        colorize("Summary", ConsoleStyle.MAGENTA_BOLD),
        colorize(this.root.getStatus().name(), State.getStatusConsoleStyle(this.root.getStatus())),
        colorize(this.root.getClass().getSimpleName(), ConsoleStyle.BLUE_BOLD),
        colorize(this.root.getName(), ConsoleStyle.WHITE_BOLD),
        colorize(String.format(Locale.US, "%.2f ms", this.root.getDurationInMillis()), ConsoleStyle.MAGENTA_BOLD),
        colorize(String.valueOf(this.root.hashCode()), ConsoleStyle.BLACK_BOLD)
    );
  }

  public String toTreeString() {
    return this.root.toTreeString();
  }

  public String toPrettyTreeString() {
    return this.root.toPrettyTreeString();
  }

  public String toPrettyErrorsAndWarningsString() {
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

  public String toErrorsAndWarningsString() {
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

  public enum Status {
    IGNORED,
    WARNING,
    SUCCESS,
    ERROR
  }
}
