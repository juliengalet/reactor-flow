package fr.jtools.reactorflow.flow;

import fr.jtools.reactorflow.exception.FlowException;
import fr.jtools.reactorflow.exception.FlowTechnicalException;
import fr.jtools.reactorflow.report.FlowContext;
import fr.jtools.reactorflow.report.GlobalReport;
import fr.jtools.reactorflow.report.Metadata;
import fr.jtools.reactorflow.report.Report;
import fr.jtools.reactorflow.report.Status;
import fr.jtools.reactorflow.utils.ConsoleStyle;
import fr.jtools.reactorflow.utils.PrettyPrint;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.concurrent.Queues;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static fr.jtools.reactorflow.utils.LoggerUtils.colorize;

/**
 * Abstract class managing all the {@link Flow}s.
 *
 * @param <T> Context type
 */
public abstract class Flow<T extends FlowContext> implements PrettyPrint {
  /**
   * The end time in nanosecond.
   */
  protected Long endTime = 0L;
  /**
   * The start time in nanosecond.
   */
  protected Long startTime = 0L;
  /**
   * The status.
   * Default is {@link Status#IGNORED} as the {@link Flow} is not executed.
   */
  protected Status status = Status.IGNORED;
  /**
   * A {@link List} containing all the errors.
   */
  private final List<FlowException> errors = Collections.synchronizedList(new ArrayList<>());
  /**
   * A {@link List} containing all the warnings.
   */
  private final List<FlowException> warnings = Collections.synchronizedList(new ArrayList<>());
  /**
   * A {@link List} containing all the recovered errors.
   */
  private final List<FlowException> recoveredErrors = Collections.synchronizedList(new ArrayList<>());

  /**
   * Abstract method that should return the {@link Flow} name.
   *
   * @return {@link Flow} name
   */
  public abstract String getName();

  /**
   * Abstract method that should implement how the {@link Flow} should be cloned with a new name.
   *
   * @param newName {@link Flow} new name
   * @return Cloned {@link Flow}
   */
  public abstract Flow<T> cloneFlow(String newName);

  /**
   * Abstract method that should implement how the {@link Flow} should be cloned.
   * It should call {@link Flow#cloneFlow(String)} with {@link Flow#getName()} as parameter.
   *
   * @return Cloned {@link Flow}
   */
  public abstract Flow<T> cloneFlow();

  /**
   * Abstract method that should describe how the {@link Flow} is executed.
   *
   * @param context  The previous {@link T} context
   * @param metadata A {@link Metadata} object
   * @return A {@link Mono} containing a {@link Report}
   */
  protected abstract Mono<Report<T>> execution(T context, Metadata<?> metadata);

  /**
   * Abstract method that should return a {@link List} containing the actual {@link Flow} children.
   * It is mandatory to be able to deduce the resulting status of the {@link Flow}.
   *
   * @return A {@link List} of {@link Flow}
   */
  protected abstract List<Flow<T>> getChildren();

  /**
   * Set {@link Flow} end time.
   */
  private void setEndTime() {
    this.endTime = System.nanoTime();
  }

  /**
   * Set {@link Flow} start time.
   */
  private void setStartTime() {
    this.startTime = System.nanoTime();
  }

  /**
   * Set {@link Flow} status.
   *
   * @param status A {@link Status}
   */
  private void setStatus(Status status) {
    this.status = status;
  }

  /**
   * Get {@link Flow} status.
   *
   * @return The {@link Flow} {@link Status}
   */
  public final Status getStatus() {
    return this.status;
  }

  /**
   * Get the {@link Flow} execution duration in nanoseconds.
   *
   * @return A {@link Long}
   */
  public final Long getDuration() {
    return this.endTime - this.startTime;
  }

  /**
   * Get the {@link Flow} execution duration in milliseconds.
   *
   * @return A {@link Double}
   */
  public final Double getDurationInMillis() {
    return BigDecimal.valueOf(getDuration()).divide(BigDecimal.valueOf(1000000), 2, RoundingMode.HALF_EVEN).doubleValue();
  }

  /**
   * Add a {@link List} of {@link FlowException} in {@link Flow#errors}.
   *
   * @param exceptions A {@link List} of {@link FlowException}
   */
  private void addErrors(List<FlowException> exceptions) {
    this.errors.addAll(exceptions);
  }

  /**
   * Add a {@link List} of {@link FlowException} in {@link Flow#warnings}.
   *
   * @param exceptions A {@link List} of {@link FlowException}
   */
  private void addWarnings(List<FlowException> exceptions) {
    this.warnings.addAll(exceptions);
  }

  /**
   * Add a {@link List} of {@link FlowException} in {@link Flow#recoveredErrors}.
   *
   * @param exceptions A {@link List} of {@link FlowException}
   */
  private void addRecoveredErrors(List<FlowException> exceptions) {
    this.recoveredErrors.addAll(exceptions);
  }

  /**
   * Get a copy of the actual {@link Flow#errors} {@link List}.
   *
   * @return The copied {@link List}
   */
  protected final List<FlowException> getErrors() {
    return List.copyOf(this.errors);
  }

  /**
   * Get a copy of the actual {@link Flow#warnings} {@link List}.
   *
   * @return The copied {@link List}
   */
  protected final List<FlowException> getWarnings() {
    return List.copyOf(this.warnings);
  }

  /**
   * The actual {@link Flow} execution.
   * It runs {@link Flow#execution(FlowContext, Metadata)} and :
   * <ul>
   *   <li>calls {@link Flow#setStartTime()}</li>
   *   <li>adds {@link FlowException} in {@link Flow#errors} in case of empty result</li>
   *   <li>adds {@link FlowException} in {@link Flow#errors} in case of raw error</li>
   *   <li>calls {@link Flow#setEndTime()}</li>
   *   <li>
   *     calls {@link Flow#setStatus(Status)} with {@link Status#SUCCESS}, {@link Status#WARNING} or {@link Status#ERROR},
   *     by calling {@link Flow#flowOrChildrenHasError()} and {@link Flow#flowOrChildrenHasWarning()}
   *   </li>
   * </ul>
   *
   * @param context  The previous {@link T} context
   * @param metadata A {@link Metadata} object
   * @return A {@link Report}
   */
  protected final Mono<Report<T>> execute(T context, Metadata<?> metadata) {
    this.setStartTime();
    return Mono.defer(() -> execution(context, metadata))
        .switchIfEmpty(Mono.error(new FlowTechnicalException(String.format(
            "%s has an empty result",
            this.getName()
        ))))
        .onErrorResume(throwable -> {
          FlowException rawError = throwable instanceof FlowException ?
              (FlowException) throwable :
              new FlowTechnicalException(throwable, throwable.getMessage());
          this.setEndTime();
          this.setStatus(Status.ERROR);
          return Mono.just(Report.error(context, rawError));
        })
        .map(report -> {
          this.setEndTime();
          this.addWarnings(report
              .getWarnings()
              .stream()
              .map(exception -> exception.flowConcerned(this))
              .collect(Collectors.toList())
          );
          this.addErrors(report
              .getErrors()
              .stream()
              .map(exception -> exception.flowConcerned(this))
              .collect(Collectors.toList())
          );
          this.setStatus(flowOrChildrenHasError() ?
              Status.ERROR :
              flowOrChildrenHasWarning() ?
                  Status.WARNING :
                  Status.SUCCESS
          );
          // Return success as now all errors and warnings are handled and stored in the flow.
          return Report.success(report.getContext());
        });
  }

  /**
   * Run the {@link Flow} and all its children with an initial {@link T} context.
   *
   * @param initialContext The initial context
   * @return A {@link Mono} containing the resulting {@link GlobalReport}
   */
  public final Mono<GlobalReport<T>> run(T initialContext) {
    return this.execute(initialContext, Metadata.empty())
        .map(report -> GlobalReport.create(report.getContext(), this));
  }

  /**
   * Run the {@link Flow} and all its children with an initial {@link T} context.
   *
   * @param initialContextMono The initial context
   * @return A {@link Mono} containing the resulting {@link GlobalReport}
   */
  public final Mono<GlobalReport<T>> run(Mono<T> initialContextMono) {
    return initialContextMono
        .flatMap(initialContext -> this.execute(initialContext, Metadata.empty()))
        .map(report -> GlobalReport.create(report.getContext(), this));
  }

  /**
   * Run a {@link Flow} and all its children with an initial {@link T} context, from a {@link Flux}.
   *
   * @param initialContextFlux The initial contexts
   * @return A {@link Flux} containing the resulting {@link GlobalReport}
   */
  public final Flux<GlobalReport<T>> run(Flux<T> initialContextFlux) {
    return this.run(initialContextFlux, Queues.SMALL_BUFFER_SIZE);
  }

  /**
   * Run a {@link Flow} and all its children with an initial {@link T} context, from a {@link Flux}, sequentially.
   *
   * @param initialContextFlux The initial contexts
   * @return A {@link Flux} containing the resulting {@link GlobalReport}
   */
  public final Flux<GlobalReport<T>> runSequential(Flux<T> initialContextFlux) {
    return this.run(initialContextFlux, 1);
  }

  /**
   * Run a {@link Flow} and all its children with an initial {@link T} context, from a {@link Flux},
   * with a specified concurrency.
   * Warning: if the {@link Flux} provided emits an error, it will break the reactor chain.
   *
   * @param initialContextFlux The initial contexts
   * @param concurrency        The concurrency
   * @return A {@link Flux} containing the resulting {@link GlobalReport}
   */
  public final Flux<GlobalReport<T>> run(Flux<T> initialContextFlux, int concurrency) {
    return initialContextFlux
        .flatMap(
            initialContext -> {
              Flow<T> clonedFlow = this.cloneFlow(String.format("%s (%s)", this.getName(), initialContext.hashCode()));

              return clonedFlow
                  .execute(initialContext, Metadata.empty())
                  .map(report -> GlobalReport.create(report.getContext(), clonedFlow));
            },
            concurrency
        );
  }

  /**
   * Get a {@link String}, representing the actual {@link Flow}, without its children.
   *
   * @return The {@link String}
   */
  @Override
  public final String toString() {
    return String.format(
        "%s named %s (%s)",
        this.getClass().getSimpleName(),
        this.getName(),
        this.hashCode()
    );
  }

  /**
   * Get a {@link String}, colorized, representing the actual {@link Flow}, without its children.
   *
   * @return The {@link String}
   */
  @Override
  public final String toPrettyString() {
    return String.format(
        "%s named %s (%s)",
        colorize(this.getClass().getSimpleName(), ConsoleStyle.BLUE_BOLD),
        colorize(this.getName(), ConsoleStyle.WHITE_BOLD),
        colorize(String.valueOf(this.hashCode()), ConsoleStyle.BLACK_BOLD)
    );
  }

  /**
   * Get a {@link String}, colorized, representing the actual {@link Flow} and its children.
   *
   * @return The {@link String}
   */
  public final String toPrettyTreeString() {
    return String.format(
        "%s%n%s%n",
        colorize("Flow tree", ConsoleStyle.MAGENTA_BOLD),
        this.printPrettyTree(0)
    );
  }

  /**
   * Get a {@link String} representing the actual {@link Flow} and its children.
   *
   * @return The {@link String}
   */
  public final String toTreeString() {
    return String.format(
        "Flow tree%n%s%n",
        this.printTree(0)
    );
  }

  /**
   * Get a {@link String}, colorized, representing the actual {@link Flow} and its children, with a fixed increment.
   *
   * @param increment An increment
   * @return The {@link String}
   */
  private String printPrettyTree(int increment) {
    StringBuilder stringBuilder = new StringBuilder();

    stringBuilder.append(" ".repeat(Math.max(0, increment)));
    stringBuilder.append(String.format(
        Locale.US,
        "%s - %s named %s ended in %s (%s)",
        colorize(this.getStatus().name(), GlobalReport.getStatusConsoleStyle(this.getStatus())),
        colorize(this.getClass().getSimpleName(), ConsoleStyle.BLUE_BOLD),
        colorize(this.getName(), ConsoleStyle.WHITE_BOLD),
        colorize(String.format(Locale.US, "%.2f ms", this.getDurationInMillis()), ConsoleStyle.MAGENTA_BOLD),
        colorize(String.valueOf(this.hashCode()), ConsoleStyle.BLACK_BOLD)
    ));

    for (Flow<T> child : this.getChildren()) {
      stringBuilder.append("\n").append(child.printPrettyTree(increment + 4));
    }
    return stringBuilder.toString();
  }

  /**
   * Get a {@link String} representing the actual {@link Flow} and its children, with a fixed increment.
   *
   * @param increment An increment
   * @return The {@link String}
   */
  private String printTree(int increment) {
    StringBuilder stringBuilder = new StringBuilder();

    stringBuilder.append(" ".repeat(Math.max(0, increment)));
    stringBuilder.append(String.format(
        Locale.US,
        "%s - %s named %s ended in %s (%s)",
        this.getStatus().name(),
        this.getClass().getSimpleName(),
        this.getName(),
        String.format(Locale.US, "%.2f ms", this.getDurationInMillis()),
        this.hashCode()
    ));

    for (Flow<T> child : this.getChildren()) {
      stringBuilder.append("\n").append(child.printTree(increment + 4));
    }
    return stringBuilder.toString();
  }

  /**
   * Get all errors ({@link FlowException}s) for the actual {@link Flow} and its children.
   *
   * @return A {@link List} containing all the {@link FlowException}s
   */
  public final List<FlowException> getErrorsForFlowAndChildren() {
    return this.getErrorsForFlowAndChildren(this, new ArrayList<>());
  }

  /**
   * Get all errors ({@link FlowException}s) for a {@link Flow} and its children.
   *
   * @param flow     A {@link Flow}
   * @param initList A mutable {@link List} that will contains the {@link FlowException}s
   * @return A {@link List} containing all the {@link FlowException}s
   */
  private List<FlowException> getErrorsForFlowAndChildren(Flow<T> flow, List<FlowException> initList) {
    initList.addAll(flow.errors);

    flow.getChildren().forEach(child -> this.getErrorsForFlowAndChildren(child, initList));

    return initList;
  }

  /**
   * Clean all errors ({@link FlowException}s) for the actual {@link Flow} and its children,
   * and add it to {@link Flow#recoveredErrors}.
   */
  protected final void cleanErrorsForFlowAndChildren() {
    this.cleanErrorsForFlowAndChildren(this);
  }

  /**
   * Clean all errors ({@link FlowException}s) for a {@link Flow} and its children,
   * and add it to its {@link Flow#recoveredErrors}.
   *
   * @param flow A {@link Flow}
   */
  private void cleanErrorsForFlowAndChildren(Flow<T> flow) {
    flow.addRecoveredErrors(flow.errors);
    flow.errors.clear();

    flow.getChildren().forEach(this::cleanErrorsForFlowAndChildren);
  }

  /**
   * Get all warnings ({@link FlowException}s) for the actual {@link Flow} and its children.
   *
   * @return A {@link List} containing all the {@link FlowException}s
   */
  public final List<FlowException> getWarningsForFlowAndChildren() {
    return this.getWarningsForFlowAndChildren(this, new ArrayList<>());
  }

  /**
   * Get all warnings ({@link FlowException}s) for a {@link Flow} and its children.
   *
   * @param flow     A {@link Flow}
   * @param initList A mutable {@link List} that will contains the {@link FlowException}s
   * @return A {@link List} containing all the {@link FlowException}s
   */
  private List<FlowException> getWarningsForFlowAndChildren(Flow<T> flow, List<FlowException> initList) {
    initList.addAll(flow.warnings);

    flow.getChildren().forEach(child -> this.getWarningsForFlowAndChildren(child, initList));

    return initList;
  }

  /**
   * Get all recovered errors ({@link FlowException}s) for the actual {@link Flow} and its children.
   *
   * @return A {@link List} containing all the {@link FlowException}s
   */
  public final List<FlowException> getRecoveredErrorsForFlowAndChildren() {
    return this.getRecoveredErrorsForFlowAndChildren(this, new ArrayList<>());
  }

  /**
   * Get all recovered errors ({@link FlowException}s) for a {@link Flow} and its children.
   *
   * @param flow     A {@link Flow}
   * @param initList A mutable {@link List} that will contains the {@link FlowException}s
   * @return A {@link List} containing all the {@link FlowException}s
   */
  private List<FlowException> getRecoveredErrorsForFlowAndChildren(Flow<T> flow, List<FlowException> initList) {
    initList.addAll(flow.recoveredErrors);

    flow.getChildren().forEach(child -> this.getRecoveredErrorsForFlowAndChildren(child, initList));

    return initList;
  }

  /**
   * Overridable method to know if the actual {@link Flow} has errors (by also checking its children).
   * Default is that actual {@link Flow} and all its children should have no error to return false.
   *
   * @return A {@link Boolean}
   */
  protected boolean flowOrChildrenHasError() {
    return !FlowStatusPolicy.flowAndAllChildrenSucceeded().test(this);
  }

  /**
   * Overridable method to know if the actual {@link Flow} has warnings (by also checking its children).
   * Default is that actual {@link Flow} and all its children should have no warning to return false.
   *
   * @return A {@link Boolean}
   */
  protected boolean flowOrChildrenHasWarning() {
    return FlowStatusPolicy.flowOrChildHasWarning().test(this);
  }
}
