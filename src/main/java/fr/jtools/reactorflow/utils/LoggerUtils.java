package fr.jtools.reactorflow.utils;

import java.util.Arrays;
import java.util.stream.Collectors;

public class LoggerUtils {
  private LoggerUtils() {
  }

  public static String colorize(String string, ConsoleStyle... consoleStyles) {
    String attributes = Arrays.stream(consoleStyles).map(ConsoleStyle::toString).collect(Collectors.joining());
    String reset = Arrays.stream(consoleStyles).map(unused -> ConsoleStyle.RESET.toString()).collect(Collectors.joining());
    return String.format("%s%s%s", attributes, string, reset);
  }
}
