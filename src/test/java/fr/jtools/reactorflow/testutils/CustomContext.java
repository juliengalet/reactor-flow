package fr.jtools.reactorflow.testutils;

import fr.jtools.reactorflow.report.FlowContext;

import java.util.Map;

public final class CustomContext extends FlowContext {
  public String customField = "CUSTOM";

  public static CustomContext createFrom(Map<String, Object> initialMap) {
    CustomContext customContext = new CustomContext();
    customContext.putAll(initialMap);
    return customContext;
  }
}
