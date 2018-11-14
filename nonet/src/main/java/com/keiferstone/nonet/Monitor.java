package com.keiferstone.nonet;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.view.ViewGroup;
import android.widget.Toast;

import java.lang.ref.WeakReference;

import io.reactivex.Observable;

import static com.keiferstone.nonet.ConnectionStatus.CONNECTED;
import static com.keiferstone.nonet.ConnectionStatus.DISCONNECTED;
import static com.keiferstone.nonet.ConnectionStatus.UNKNOWN;

@SuppressWarnings( {"unused"} )
public class Monitor {
    private static final String TAG = "AWAISKING_APP";
    private WeakReference<Context> contextRef;
    private Configuration configuration;
    private final Handler handler;
    private boolean poll;
    private Toast toast;
    private Snackbar snackbar;
    private WeakReference<BannerView> bannerRef;
    private Callback callback;
    private Observable<Integer> observable;
    @ConnectionStatus private int connectionStatus;
    private final Runnable pollTaskRunnable = this::poll;
    private final ConnectivityReceiver connectivityReceiver = new ConnectivityReceiver() {
        @Override
        public void onConnectivityChanged(@ConnectionStatus int connectionStatus) {
            // Don't really care what connectionStatus is, poll no matter what.
            poll();
        }
    };

    Monitor(Context context) {
        this.contextRef = new WeakReference<>(context);
        this.configuration = new Configuration();
        this.handler = new Handler();
        this.poll = false;
        this.toast = null;
        this.snackbar = null;
        this.bannerRef = new WeakReference<>(null);
        this.callback = null;
        this.observable = null;
        this.connectionStatus = UNKNOWN;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Monitor)) return false;

        Context context = getContext();
        if (context == null) return super.equals(o);

        Monitor monitor = (Monitor) o;
        return context.equals(monitor.getContext());
    }

    @Override
    public int hashCode() {
        Context context = getContext();
        return context != null ? context.hashCode() : super.hashCode();
    }

    void start() {
        registerConnectivityReceiver();
        schedulePollTask();
    }

    void stop() {
        unregisterConnectivityReceiver();
        cancelPollTask();
        cancelToast();
        dismissSnackbar();
        destroyObservable();
    }

    void poll() {
        PollTask.run(configuration, connectionStatus -> {
            Monitor.this.connectionStatus = connectionStatus;

            if (callback != null) callback.onConnectionEvent(connectionStatus);

            BannerView banner = getBanner();
            if (connectionStatus == DISCONNECTED) {
                if (toast != null) toast.show();
                if (snackbar != null) snackbar.show();
                if (banner != null) banner.show();
            } else {
                cancelToast();
                dismissSnackbar();
                if (banner != null) banner.hide();
            }

            schedulePollTask();
        });
    }

    @Nullable
    Context getContext() {
        return contextRef.get();
    }

    void setContext(Context context) {
        this.contextRef = new WeakReference<>(context);
    }

    private void registerConnectivityReceiver() {
        Context context = getContext();
        if (context != null)
            context.registerReceiver(connectivityReceiver, ConnectivityReceiver.getIntentFilter());
    }

    private void unregisterConnectivityReceiver() {
        Context context = getContext();
        if (context != null) context.unregisterReceiver(connectivityReceiver);
    }

    private void schedulePollTask() {
        if (poll) {
            int pollFrequency = connectionStatus == CONNECTED
                    ? configuration.getConnectedPollFrequency()
                    : configuration.getDisconnectedPollFrequency();
            if (pollFrequency > 0 && pollFrequency != Configuration.NEVER)
                handler.postDelayed(pollTaskRunnable, pollFrequency * 1000);
        }
    }

    private void cancelPollTask() {
        handler.removeCallbacks(pollTaskRunnable);
    }

    private void cancelToast() {
        if (toast != null) toast.cancel();
    }

    private void dismissSnackbar() {
        if (snackbar != null) snackbar.dismiss();
    }

    @Nullable
    private BannerView getBanner() {
        return bannerRef.get();
    }

    @NonNull
    private Observable<Integer> createObservable() {
        if (observable == null)
            observable = Observable.create(e -> callback = new ObservableCallbackInterceptor(callback, e));
        return observable;
    }

    private void destroyObservable() {
        if (callback instanceof ObservableCallbackInterceptor)
            ((ObservableCallbackInterceptor) callback).stopEmitting();
        observable = null;
    }

    public interface Callback {
        void onConnectionEvent(@ConnectionStatus int connectionStatus);
    }

    @SuppressWarnings( "UnusedReturnValue" )
    public static class Builder {
        final Monitor monitor;

        Builder(Check check) {
            monitor = check;
        }

        Builder(Context context) {
            monitor = new Monitor(context);
        }

        /**
         * Set the configuration for this {@link Monitor}.
         *
         * @param configuration The configuration to set.
         * @return This {@link Monitor.Builder}.
         */
        public Builder configure(Configuration configuration) {
            monitor.configuration = configuration;
            return this;
        }

        /**
         * Enable polling for this monitor.
         *
         * @return This {@link Monitor.Builder}.
         */
        public Builder poll() {
            monitor.poll = true;
            return this;
        }

        /**
         * Show the default {@link Toast} message when there is no connectivity.
         *
         * @return This {@link Monitor.Builder}.
         */
        public Builder toast() {
            monitor.toast = ToastFactory.getToast(monitor.getContext());
            return this;
        }

        /**
         * Show a {@link Toast} with the specified message when there is no connectivity.
         *
         * @param message The message to show.
         * @return This {@link Monitor.Builder}.
         */
        public Builder toast(String message) {
            monitor.toast = ToastFactory.getToast(monitor.getContext(), message);
            return this;
        }

        /**
         * Show a {@link Toast} with the specified message when there is no connectivity.
         *
         * @param messageRes The message to show.
         * @return This {@link Monitor.Builder}.
         */
        public Builder toast(@StringRes int messageRes) {
            monitor.toast = ToastFactory.getToast(monitor.getContext(), messageRes);
            return this;
        }

        /**
         * Show a custom {@link Toast} when there is no connectivity.
         *
         * @param toast The {@link Toast} to show.
         * @return This {@link Monitor.Builder}.
         */
        public Builder toast(Toast toast) {
            monitor.toast = toast;
            return this;
        }

        /**
         * Show the default {@link Snackbar} message when there is no connectivity.
         *
         * @return This {@link Monitor.Builder}.
         */
        public Builder snackbar() {
            monitor.snackbar = SnackbarFactory.getSnackbar(monitor.getContext());
            return this;
        }

        /**
         * Show a {@link Snackbar} with the specified message when there is no connectivity.
         *
         * @param message The message to show.
         * @return This {@link Monitor.Builder}.
         */
        public Builder snackbar(String message) {
            monitor.snackbar = SnackbarFactory.getSnackbar(monitor.getContext(), message);
            return this;
        }

        /**
         * Show a {@link Snackbar} with the specified message when there is no connectivity.
         *
         * @param messageRes The message to show.
         * @return This {@link Monitor.Builder}.
         */
        public Builder snackbar(@StringRes int messageRes) {
            monitor.snackbar = SnackbarFactory.getSnackbar(monitor.getContext(), messageRes);
            return this;
        }

        /**
         * Show a custom {@link Snackbar} when there is no connectivity.
         *
         * @param snackbar The {@link Snackbar} to show.
         * @return This {@link Monitor.Builder}.
         */
        public Builder snackbar(Snackbar snackbar) {
            monitor.snackbar = snackbar;
            return this;
        }

        /**
         * Show the default {@link BannerView} message when there is no connectivity.
         *
         * @return This {@link Monitor.Builder}.
         */
        public Builder banner() {
            monitor.bannerRef = new WeakReference<>(BannerFactory.getBanner(monitor.getContext()));
            return this;
        }

        /**
         * Show a {@link BannerView} with the specified message when there is no connectivity.
         *
         * @param message The message to show.
         * @return This {@link Monitor.Builder}.
         */
        public Builder banner(String message) {
            monitor.bannerRef = new WeakReference<>(BannerFactory.getBanner(monitor.getContext(), message));
            return this;
        }

        /**
         * Show a {@link BannerView} with the specified message when there is no connectivity.
         *
         * @param messageRes The message to show.
         * @return This {@link Monitor.Builder}.
         */
        public Builder banner(@StringRes int messageRes) {
            monitor.bannerRef = new WeakReference<>(BannerFactory.getBanner(monitor.getContext(), messageRes));
            return this;
        }

        /**
         * Show a custom {@link BannerView} when there is no connectivity.
         *
         * @param banner The {@link BannerView} to show. This banner is presumed to
         *               already be inflated and attached to a parent.
         * @return This {@link Monitor.Builder}.
         */
        public Builder banner(BannerView banner) {
            monitor.bannerRef = new WeakReference<>(banner);
            return this;
        }

        /**
         * Show a custom {@link BannerView} when there is no connectivity.
         *
         * @param bannerRes A {@link LayoutRes} containing a single {@link BannerView} to
         *                  be inflated and shown.
         * @param parent    The parent to inflate this banner into. If null, banner will be attached
         *                  to the {@link android.app.Activity}'s layout root.
         * @return This {@link Monitor.Builder}.
         */
        public Builder banner(@LayoutRes int bannerRes, @Nullable ViewGroup parent) {
            monitor.bannerRef = new WeakReference<>(BannerFactory.getBanner(monitor.getContext(), bannerRes, parent));
            return this;
        }

        /**
         * Set a {@link Callback} to be invoked when there is a connectivity event.
         *
         * @param callback The callback to set.
         * @return This {@link Monitor.Builder}.
         */
        public Builder callback(Callback callback) {
            monitor.callback = callback;
            return this;
        }

        /**
         * Observe this {@link Monitor} for connectivity events. When there is a connectivity event,
         * the {@link ConnectionStatus} will be emitted to {@link io.reactivex.Observer#onNext(Object)}.
         *
         * @return An {@link Observable} that emits a {@link ConnectionStatus} on connectivity events.
         */
        public Observable<Integer> observe() {
            return monitor.createObservable();
        }
    }
}
