package fr.jtools.reactorflow.flow;

import fr.jtools.reactorflow.exception.FlowException;
import fr.jtools.reactorflow.exception.RecoverableFlowException;
import fr.jtools.reactorflow.state.FlowContext;
import fr.jtools.reactorflow.state.Metadata;
import fr.jtools.reactorflow.state.State;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class RetryableFlow<T extends FlowContext> extends Flow<T> {
  private final Flow<T> flow;
  private final List<Flow<T>> flowsToRetry;
  private final String name;
  private RecoverableFlowException retryOn = RecoverableFlowException.TECHNICAL;
  private Integer retryTimes = 1;
  private Integer delay = 100;

  public static <T extends FlowContext> RetryableFlow<T> create(String name, Flow<T> flow, Integer retryTimes, Integer delay, RecoverableFlowException retryOn) {
    return new RetryableFlow<>(name, flow, retryTimes, delay, retryOn);
  }

  private RetryableFlow(String name, Flow<T> flow, Integer retryTimes, Integer delay, RecoverableFlowException retryOn) {
    this.name = name;
    this.flow = flow;
    if (Objects.nonNull(retryTimes)) {
      this.retryTimes = retryTimes;
    }
    if (Objects.nonNull(delay)) {
      this.delay = delay;
    }
    if (Objects.nonNull(retryOn)) {
      this.retryOn = retryOn;
    }

    ArrayList<Flow<T>> toRetry = new ArrayList<>();
    for (int c = 0; c < this.retryTimes; c++) {
      toRetry.add(this.flow.cloneFlow(String.format("%s (Retry %d)", this.flow.getName(), c + 1)));
    }
    this.flowsToRetry = toRetry;
  }

  @Override
  public final String getName() {
    return name;
  }

  @Override
  protected final Mono<State<T>> execution(State<T> previousState, Metadata<?> metadata) {
    return this.tryExecution(previousState, new AtomicInteger(0), this.flow, metadata);
  }

  @Override
  public final RetryableFlow<T> cloneFlow(String newName) {
    return RetryableFlow.create(newName, this.flow.cloneFlow(), this.retryTimes, this.delay, this.retryOn);
  }

  @Override
  public final RetryableFlow<T> cloneFlow() {
    return this.cloneFlow(this.getName());
  }

  @Override
  protected final List<Flow<T>> getChildren() {
    return Stream.concat(Stream.of(flow), this.flowsToRetry.stream()).collect(Collectors.toList());
  }

  @Override
  protected boolean flowOrChildrenHasError() {
    return !FlowStatusPolicy.flowAndOneChildSucceeded().test(this);
  }

  private Mono<State<T>> tryExecution(State<T> previousState, AtomicInteger counter, Flow<T> flow, Metadata<?> metadata) {
    return flow.execute(previousState, Metadata.from(metadata))
        .flatMap(state -> {
          int count = counter.getAndIncrement();
          List<FlowException> exceptionsForFlow = flow.getErrorsForFlowAndChildren();
          if (
              !exceptionsForFlow.isEmpty() &&
                  count < this.retryTimes &&
                  exceptionsForFlow.stream().allMatch(exception -> exception.isRecoverable(this.retryOn))
          ) {
            flow.cleanErrorsForFlowAndChildren();
            return Mono
                .delay(Duration.ofMillis(delay))
                .flatMap(unused -> this.tryExecution(
                    state,
                    counter,
                    this.flowsToRetry.get(count),
                    Metadata.from(metadata)
                ));
          }

          return Mono.just(state);
        });
  }
}
