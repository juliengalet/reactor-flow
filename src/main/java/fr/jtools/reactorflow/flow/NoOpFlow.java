package fr.jtools.reactorflow.flow;

import fr.jtools.reactorflow.state.FlowContext;
import fr.jtools.reactorflow.state.Metadata;
import fr.jtools.reactorflow.state.State;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

/**
 * Class managing a no op {@link Flow} (aka a {@link Flow} that does nothing).
 *
 * @param <T> Context type
 */
public final class NoOpFlow<T extends FlowContext> extends Flow<T> {
  /**
   * The name.
   */
  private final String name;

  /**
   * Static method used to create a {@link NoOpFlow}.
   *
   * @param name {@link NoOpFlow#name}
   * @param <T>  Context type
   * @return A {@link NoOpFlow}
   */
  public static <T extends FlowContext> NoOpFlow<T> named(String name) {
    return new NoOpFlow<>(name);
  }

  private NoOpFlow(String name) {
    this.name = name;
  }

  /**
   * Get the {@link ConditionalFlow} name.
   *
   * @return The name
   */
  @Override
  public final String getName() {
    return name;
  }

  /**
   * {@link NoOpFlow} execution.
   * It just returns the previous {@link State}.
   *
   * @param previousState The previous {@link State}
   * @param metadata      A {@link Metadata} object
   * @return The new {@link State}
   */
  @Override
  protected final Mono<State<T>> execution(State<T> previousState, Metadata<?> metadata) {
    return Mono.just(previousState);
  }

  /**
   * Clone the {@link NoOpFlow} with a new name.
   *
   * @param newName {@link NoOpFlow} new name
   * @return Cloned {@link NoOpFlow}
   */
  @Override
  public NoOpFlow<T> cloneFlow(String newName) {
    return NoOpFlow.named(newName);
  }

  /**
   * Clone the {@link NoOpFlow}.
   *
   * @return Cloned {@link NoOpFlow}
   */
  @Override
  public NoOpFlow<T> cloneFlow() {
    return this.cloneFlow(this.getName());
  }

  /**
   * Get {@link NoOpFlow} children, aka {@link Collections#emptyList()}.
   *
   * @return An empty list
   */
  @Override
  protected List<Flow<T>> getChildren() {
    return Collections.emptyList();
  }
}
