package fr.jtools.reactorflow.testutils;

import fr.jtools.reactorflow.state.FlowContext;
import fr.jtools.reactorflow.state.State;

import java.util.function.Consumer;

public final class TestUtils {
  private TestUtils() {
  }

  public static <T extends FlowContext> Consumer<State<T>> assertAndLog(Consumer<State<T>> assertions) {
    return state -> {
      System.out.println(state.toPrettyString());
      System.out.println(state.toPrettyExceptionsRaisedString());
      System.out.println(state.getContext().toPrettyString());
      System.out.println(state.toPrettyTreeString());
      assertions.accept(state);
    };
  }
}
