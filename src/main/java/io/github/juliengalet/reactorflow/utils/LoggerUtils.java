package io.github.juliengalet.reactorflow.utils;

import java.util.Arrays;
import java.util.stream.Collectors;

public class LoggerUtils {
  private LoggerUtils() {
  }

  /**
   * An utility method used to apply {@link ConsoleStyle}s on a {@link String}.
   *
   * @param string        A {@link String}
   * @param consoleStyles The {@link ConsoleStyle}s
   * @return A {@link String} with the styles
   */
  public static String colorize(String string, ConsoleStyle... consoleStyles) {
    String attributes = Arrays.stream(consoleStyles).map(ConsoleStyle::toString).collect(Collectors.joining());
    String reset = Arrays.stream(consoleStyles).map(unused -> ConsoleStyle.RESET.toString()).collect(Collectors.joining());
    return String.format("%s%s%s", attributes, string, reset);
  }
}
