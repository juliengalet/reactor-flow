package io.github.juliengalet.reactorflow.flow;

import io.github.juliengalet.reactorflow.report.FlowContext;
import io.github.juliengalet.reactorflow.report.Metadata;

/**
 * An interface defining a {@link StepFlow} execution, without specific metadata type
 * (see {@link StepWithMetadata} for the version with {@link Metadata} typing).
 * Can be implemented in any application, with dependency injection.
 *
 * @param <T> Context type
 */
public interface Step<T extends FlowContext> extends StepWithMetadata<T, Object> {

}