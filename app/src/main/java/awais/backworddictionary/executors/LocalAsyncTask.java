package awais.backworddictionary.executors;

import android.os.Binder;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import awais.backworddictionary.BuildConfig;

/**
 * same as {@link android.os.AsyncTask}
 *
 * @param <Param>
 * @param <Result>
 */
public abstract class LocalAsyncTask<Param, Result> {
    private static final int CORE_POOL_SIZE = 1;
    private static final int BACKUP_POOL_SIZE = 5;
    private static final int MAXIMUM_POOL_SIZE = 20;
    private static final int KEEP_ALIVE_SECONDS = 5;

    private static final int MESSAGE_POST_RESULT = 0x1;

    private static final ThreadFactory sThreadFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        @NonNull
        @Override
        public Thread newThread(final Runnable r) {
            return new Thread(r, "AwaisomeTask #" + mCount.getAndIncrement());
        }
    };
    private static final RejectedExecutionHandler sRunOnSerialPolicy = new RejectedExecutionHandler() {
        @Override
        public void rejectedExecution(final Runnable r, final ThreadPoolExecutor e) {
            synchronized (this) {
                if (sBackupExecutor == null) {
                    final LinkedBlockingQueue<Runnable> sBackupExecutorQueue = new LinkedBlockingQueue<>();
                    sBackupExecutor = new ThreadPoolExecutor(BACKUP_POOL_SIZE, BACKUP_POOL_SIZE,
                            KEEP_ALIVE_SECONDS, TimeUnit.SECONDS, sBackupExecutorQueue, sThreadFactory);
                    sBackupExecutor.allowCoreThreadTimeOut(true);
                }
            }
            sBackupExecutor.execute(r);
        }
    };

    public static final Executor THREAD_POOL_EXECUTOR;

    static {
        final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE,
                KEEP_ALIVE_SECONDS, TimeUnit.SECONDS, new SynchronousQueue<>(),
                sThreadFactory);
        threadPoolExecutor.setRejectedExecutionHandler(sRunOnSerialPolicy);
        THREAD_POOL_EXECUTOR = threadPoolExecutor;
    }

    private static ThreadPoolExecutor sBackupExecutor;
    private static InternalHandler sHandler;

    private volatile Status mStatus = Status.PENDING;
    private final Handler mHandler;
    private final FutureTask<Result> mFuture;
    private final WorkerRunnable<Param, Result> mWorker;
    private final AtomicBoolean mCancelled = new AtomicBoolean();
    private final AtomicBoolean mTaskInvoked = new AtomicBoolean();

    private static Handler getMainHandler() {
        synchronized (LocalAsyncTask.class) {
            if (sHandler == null) {
                sHandler = new InternalHandler(Looper.getMainLooper());
            }
            return sHandler;
        }
    }

    private Handler getHandler() {
        return mHandler;
    }

    public LocalAsyncTask() {
        mHandler = getMainHandler();
        mWorker = new WorkerRunnable<Param, Result>() {
            @Override
            public Result call() {
                mTaskInvoked.set(true);
                Result result = null;
                try {
                    Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                    result = doInBackground(mParam);
                    Binder.flushPendingCommands();
                } catch (final Throwable tr) {
                    mCancelled.set(true);
                    throw tr;
                } finally {
                    postResult(result);
                }
                return result;
            }
        };
        mFuture = new FutureTask<Result>(mWorker) {
            @Override
            protected void done() {
                try {
                    postResultIfNotInvoked(get());
                } catch (final InterruptedException e) {
                    if (BuildConfig.DEBUG) Log.e("AWAISKING_APP", "LocalAsyncTask", e);
                } catch (final ExecutionException e) {
                    throw new RuntimeException("An error occurred while executing doInBackground()", e.getCause());
                } catch (final CancellationException e) {
                    postResultIfNotInvoked(null);
                }
            }
        };
    }

    private void postResultIfNotInvoked(final Result result) {
        final boolean wasTaskInvoked = mTaskInvoked.get();
        if (!wasTaskInvoked) postResult(result);
    }

    private void postResult(final Result result) {
        getHandler().obtainMessage(MESSAGE_POST_RESULT, new LocalAsyncResult<>(this, result))
                .sendToTarget();
    }

    @WorkerThread
    protected abstract Result doInBackground(final Param param);

    @MainThread
    protected void onPreExecute() { }

    @SuppressWarnings({"UnusedDeclaration"})
    @MainThread
    protected void onPostExecute(final Result result) { }

    @SuppressWarnings({"UnusedParameters"})
    @MainThread
    protected void onCancelled(final Result result) { }

    public final boolean isCancelled() {
        return mCancelled.get();
    }

    public final void cancel(final boolean mayInterruptIfRunning) {
        mCancelled.set(true);
        mFuture.cancel(mayInterruptIfRunning);
    }

    /*
    not yet used
        public final Result get() throws InterruptedException, ExecutionException {
            return mFuture.get();
        }

        public final Result get(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            return mFuture.get(timeout, unit);
        }
    */

    @MainThread
    public final void execute() {
        execute(null);
    }

    @MainThread
    public final void execute(final Param param) {
        if (mStatus != Status.PENDING) {
            switch (mStatus) {
                case RUNNING:
                    throw new IllegalStateException("Cannot execute task: the task is already running.");
                case FINISHED:
                    throw new IllegalStateException("Cannot execute task: the task has already been executed (a task can be executed only once)");
            }
        }

        mStatus = Status.RUNNING;
        onPreExecute();
        mWorker.mParam = param;

        THREAD_POOL_EXECUTOR.execute(mFuture);
    }

    private void finish(final Result result) {
        if (isCancelled()) onCancelled(result);
        else onPostExecute(result);
        mStatus = Status.FINISHED;
    }

    private final static class InternalHandler extends Handler {
        public InternalHandler(final Looper looper) {
            super(looper);
        }

        @SuppressWarnings("unchecked")
        @Override
        public void handleMessage(@NonNull final Message msg) {
            final LocalAsyncResult<?> result = (LocalAsyncResult<?>) msg.obj;
            if (msg.what == MESSAGE_POST_RESULT) result.mTask.finish(result.mData);
        }
    }

    private static abstract class WorkerRunnable<Param, Result> implements Callable<Result> {
        Param mParam;
    }

    @SuppressWarnings("rawtypes")
    private final static class LocalAsyncResult<Data> {
        protected final LocalAsyncTask mTask;
        protected final Data mData;

        LocalAsyncResult(final LocalAsyncTask task, final Data data) {
            mTask = task;
            mData = data;
        }
    }

    public enum Status {
        PENDING,
        RUNNING,
        FINISHED,
    }
}