package awais.backworddictionary.executor;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import awais.backworddictionary.BuildConfig;

public final class TaskExecutor {
    private static final Handler handler = new Handler(Looper.getMainLooper());
    private static final ThreadPoolExecutor executor = new ThreadPoolExecutor(0, Integer.MAX_VALUE,
            30, TimeUnit.SECONDS, new SynchronousQueue<>());

    public static <T> void executeAsync(final ExecutorCallback<T> callable) {
        try {
            callable.preExecute();
            executor.execute(new RunnableTask<>(callable));
        } catch (final Exception e) {
            handler.post(new RunnableTaskForHandler<>(callable, null));
            if (BuildConfig.DEBUG)
                Log.e("AWAISKING_APP", "", e);
        }
    }

    private static final class RunnableTask<T> implements Runnable {
        private final ExecutorCallback<T> callable;

        public RunnableTask(final ExecutorCallback<T> callable) {
            this.callable = callable;
        }

        @Override
        public void run() {
            T result = null;
            try {
                result = callable.call();
            } catch (final Exception e) {
                if (BuildConfig.DEBUG)
                    Log.e("AWAISKING_APP", "", e);
            }
            handler.post(new RunnableTaskForHandler<>(callable, result));
        }
    }

    private static final class RunnableTaskForHandler<T> implements Runnable {
        private final ExecutorCallback<T> callable;
        private final T result;

        public RunnableTaskForHandler(final ExecutorCallback<T> callable, final T result) {
            this.callable = callable;
            this.result = result;
        }

        @Override
        public void run() {
            callable.postExecute(result);
        }
    }
}