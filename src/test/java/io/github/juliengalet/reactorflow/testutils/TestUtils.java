package io.github.juliengalet.reactorflow.testutils;

import io.github.juliengalet.reactorflow.report.FlowContext;
import io.github.juliengalet.reactorflow.report.GlobalReport;

import java.util.function.Consumer;

public final class TestUtils {
  private TestUtils() {
  }

  public static <T extends FlowContext> Consumer<GlobalReport<T>> assertAndLog(Consumer<GlobalReport<T>> assertions) {
    return globalReport -> {
      System.out.println(globalReport.toPrettyString());
      System.out.println(globalReport.toPrettyExceptionsRaisedString());
      System.out.println(globalReport.getContext().toPrettyString());
      System.out.println(globalReport.toPrettyTreeString());
      assertions.accept(globalReport);
    };
  }
}
