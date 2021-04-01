package fr.jtools.reactorflow.state;

import fr.jtools.reactorflow.utils.ConsoleStyle;
import fr.jtools.reactorflow.utils.PrettyPrint;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static fr.jtools.reactorflow.utils.LoggerUtils.colorize;

/**
 * Default context, using a ConcurrentHashMap, in order to be thread safe during ParallelFlow.
 * Be careful about the objects inside the ConcurrentHashMap as they can not be thread safe.
 * If you prefer use your own context, be careful about thread safe considerations for ParallelFlow.
 */
public class FlowContext implements PrettyPrint {
  private Map<String, Object> context = new ConcurrentHashMap<>();

  public static FlowContext create() {
    return new FlowContext();
  }

  public static FlowContext createFrom(Map<String, Object> initialMap) {
    return new FlowContext(initialMap);
  }

  public FlowContext() {
  }

  private FlowContext(Map<String, Object> initialMap) {
    context = new ConcurrentHashMap<>(initialMap);
  }

  public void put(String key, Object value) {
    this.context.put(key, value);
  }

  public Object get(String key) {
    return this.context.get(key);
  }

  public Set<Map.Entry<String, Object>> getEntrySet() {
    return this.context.entrySet();
  }

  @Override
  public String toString() {
    return String.format(
        "Context%n%s%n",
        this.getEntrySet()
            .stream()
            .map(entry -> String.format(
                "%s - %s",
                entry.getKey(),
                Objects.nonNull(entry.getKey()) ? entry.getValue().toString() : "null"
            ))
            .collect(Collectors.joining("\n"))
    );
  }

  @Override
  public String toPrettyString() {
    return String.format(
        "%s%n%s%n",
        colorize("Context", ConsoleStyle.MAGENTA_BOLD),
        this.getEntrySet()
            .stream()
            .map(entry -> String.format(
                "%s - %s",
                colorize(entry.getKey(), ConsoleStyle.BLUE_BOLD),
                Objects.nonNull(entry.getKey()) ? entry.getValue().toString() : "null"
            ))
            .collect(Collectors.joining("\n"))
    );
  }
}
