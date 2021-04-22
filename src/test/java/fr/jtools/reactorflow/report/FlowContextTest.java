package fr.jtools.reactorflow.report;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

final class FlowContextTest {
  @Test
  final void create_shouldCreateEmptyFlowContext() {
    FlowContext context = FlowContext.create();

    assertThat(context.getEntrySet()).isEmpty();
  }

  @Test
  final void createFrom_shouldCreateEmptyFlowContext() {
    FlowContext context = FlowContext.createFrom(Map.of("key", "value"));

    assertThat(context.get("key")).isEqualTo("value");
  }

  @Test
  final void givenContext_put_shouldAddAValue() {
    FlowContext context = FlowContext.create();
    context.put("key", "value");

    assertThat(context.get("key")).isEqualTo("value");
  }

  @Test
  final void givenContext_putAll_shouldAddValues() {
    FlowContext context = FlowContext.create();
    context.putAll(Map.of("key", "value", "key2", "value2"));

    assertThat(context.get("key")).isEqualTo("value");
    assertThat(context.get("key2")).isEqualTo("value2");
  }

  @Test
  final void givenContext_getEntrySet_shouldReturnValues() {
    FlowContext context = FlowContext.create();
    context.putAll(Map.of("key", "value", "key2", "value2"));

    assertThat(context.getEntrySet()).hasSize(2);
  }

  @Test
  final void givenContext_toString_shouldNotThrow() {
    FlowContext context = FlowContext.create();
    context.putAll(Map.of("key", "value", "key2", "value2"));

    assertThat(context.toString()).isNotNull();
  }

  @Test
  final void givenContext_toPrettyString_shouldNotThrow() {
    FlowContext context = FlowContext.create();
    context.putAll(Map.of("key", "value", "key2", "value2"));

    assertThat(context.toPrettyString()).isNotNull();
  }
}
