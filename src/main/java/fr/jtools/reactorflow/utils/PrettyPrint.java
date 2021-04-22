package fr.jtools.reactorflow.utils;

/**
 * Interface used to specify that a class should implement a method to have a stylized version of its {@link Object#toString()} method.
 */
public interface PrettyPrint {
  String toPrettyString();
}
