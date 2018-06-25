package ru.sbt.integration.orchestration.fsmhelper;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public class ValueOrError<V, E> {
    private V value;
    private E error;

    /**
     * Common instance for {@code empty()}.
     */
    private static final ValueOrError<?, ?> EMPTY = new ValueOrError<>();

    private ValueOrError() {
        this.value = null;
        this.error = null;
    }

    private ValueOrError(V v, E e) {
        this.value = v;
        this.error = e;
    }

    public static <V, E> ValueOrError<V, E> value(V v) {
        return new ValueOrError<>(v, null);
    }

    public static <V, E> ValueOrError<V, E> value(V v, Class<E> classOf) {
        return new ValueOrError<V, E>(v, null);
    }

    public static <V, E> ValueOrError<V, E> error(E e) {
        return new ValueOrError<>(null, e);
    }

    public static <V, E> ValueOrError<V, E> ofNullable(V value) {
        return value == null ? empty() : value(value);
    }

    public static <T, E> ValueOrError<T, E> empty() {
        return (ValueOrError<T, E>) EMPTY;
    }

    public <U> ValueOrError<U, E> map(Function<? super V, ? extends U> mapper) {
        Objects.requireNonNull(mapper);
        if (!isPresent())
            return empty();
        else {
            return ValueOrError.ofNullable(mapper.apply(value));
        }
    }

    public void fold(Consumer<V> ifValue, Consumer<E> ifError) {
        if (value == null) {
            ifError.accept(error);
        } else {
            ifValue.accept(value);
        }
    }

    public <R> R foldWithReturn(Function<? super V, ? extends R> onValue, Function<? super E, ? extends R> onError) {
        if (value == null) {
            return onError.apply(error);
        } else {
            return onValue.apply(value);
        }
    }

    public <R> R foldError(Function<? super E, ? extends R> onError) {
        if (error == null) {
            throw new NoSuchElementException("No error present");
        }
        return onError.apply(error);
    }

    public boolean isPresent() {
        return value != null;
    }

    public boolean isError() {
        return error != null;
    }

    public void onError(Consumer<? super E> onError) {
        if (error != null) {
            onError.accept(error);
        }
    }

    public V get() {
        if (value == null) {
            throw new NoSuchElementException("No value present");
        }
        return value;
    }

    public E getError() {
        if (error == null) {
            throw new NoSuchElementException("No error present");
        }
        return error;
    }
}