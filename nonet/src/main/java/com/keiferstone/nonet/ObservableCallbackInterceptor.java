package com.keiferstone.nonet;

import io.reactivex.ObservableEmitter;

class ObservableCallbackInterceptor implements Monitor.Callback {
    private final Monitor.Callback callback;
    private final ObservableEmitter<Integer> emitter;

    ObservableCallbackInterceptor(Monitor.Callback callback, ObservableEmitter<Integer> emitter) {
        this.callback = callback;
        this.emitter = emitter;
    }

    void stopEmitting() {
        if (emitter != null) emitter.onComplete();
    }

    @Override
    public void onConnectionEvent(@ConnectionStatus int connectionStatus) {
        if (callback != null) callback.onConnectionEvent(connectionStatus);
        if (emitter != null) emitter.onNext(connectionStatus);
    }
}