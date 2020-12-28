package awais.backworddictionary.executor;

import java.util.concurrent.Callable;

public interface ExecutorCallback<T> extends Callable<T> {
    default void preExecute() {}
    @Override
    T call();
    default void postExecute(final T result) {}
}