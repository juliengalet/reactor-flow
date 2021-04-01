package fr.jtools.reactorflow.exception;

public final class FlowBuilderException extends FlowException {
  public static <T> String mapMessage(Class<T> builder, String message) {
    return String.format("%s: %s", builder.getSimpleName(), message);
  }

  @Override
  public FlowExceptionType getType() {
    return FlowExceptionType.BUILDER;
  }

  public <T> FlowBuilderException(Class<T> builder, String message) {
    super(FlowBuilderException.mapMessage(builder, message));
  }

  public <T> FlowBuilderException(Throwable cause, Class<T> builder, String message) {
    super(cause, FlowBuilderException.mapMessage(builder, message));
  }
}
