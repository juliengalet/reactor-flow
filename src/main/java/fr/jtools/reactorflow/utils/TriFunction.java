package fr.jtools.reactorflow.utils;

import java.util.Objects;
import java.util.function.Function;

/**
 * A tri function implementation.
 *
 * @param <A> First parameter type
 * @param <B> Second parameter type
 * @param <C> Third parameter type
 * @param <R> Result type
 */
@FunctionalInterface
public interface TriFunction<A, B, C, R> {

  R apply(A a, B b, C c);

  default <V> TriFunction<A, B, C, V> andThen(
      Function<? super R, ? extends V> after) {
    Objects.requireNonNull(after);
    return (A a, B b, C c) -> after.apply(apply(a, b, c));
  }
}
