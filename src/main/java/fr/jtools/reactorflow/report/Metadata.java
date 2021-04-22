package fr.jtools.reactorflow.report;

import fr.jtools.reactorflow.exception.FlowException;

import java.util.ArrayList;
import java.util.List;

/**
 * Class used to store metadata during a {@link fr.jtools.reactorflow.flow.Flow}
 * It is used with {@link fr.jtools.reactorflow.flow.ParallelFlow} when looping on an array from the context,
 * or with {@link fr.jtools.reactorflow.flow.SequentialFlow}, to have the previous errors during doFinally {@link fr.jtools.reactorflow.flow.Flow}.
 *
 * @param <M> The metadata type
 */
public class Metadata<M> {
  private final List<FlowException> errors = new ArrayList<>();
  private final List<FlowException> warnings = new ArrayList<>();
  private final M data;

  public static <M> Metadata<M> create(M data) {
    return new Metadata<>(data);
  }

  public static <M> Metadata<M> empty() {
    return new Metadata<>(null);
  }

  public static <M> Metadata<M> from(Metadata<M> metadata) {
    return Metadata.from(metadata, metadata.getData());
  }

  public static <M> Metadata<M> from(Metadata<M> metadata, M newData) {
    return new Metadata<>(newData).addErrors(metadata.getErrors()).addWarnings(metadata.getWarnings());
  }

  private Metadata(M data) {
    this.data = data;
  }

  public Metadata<M> addErrors(List<FlowException> exceptions) {
    this.errors.addAll(exceptions);
    return this;
  }

  public Metadata<M> addWarnings(List<FlowException> exceptions) {
    this.warnings.addAll(exceptions);
    return this;
  }

  public List<FlowException> getErrors() {
    return this.errors;
  }

  public List<FlowException> getWarnings() {
    return this.warnings;
  }

  public M getData() {
    return this.data;
  }
}
