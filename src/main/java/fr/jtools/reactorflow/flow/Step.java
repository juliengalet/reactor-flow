package fr.jtools.reactorflow.flow;

import fr.jtools.reactorflow.report.FlowContext;

/**
 * An interface defining a {@link StepFlow} execution, without specific metadata type
 * (see {@link StepWithMetadata} for the version with {@link fr.jtools.reactorflow.report.Metadata} typing).
 * Can be implemented in any application, with dependency injection.
 *
 * @param <T> Context type
 */
interface Step<T extends FlowContext> extends StepWithMetadata<T, Object> {

}