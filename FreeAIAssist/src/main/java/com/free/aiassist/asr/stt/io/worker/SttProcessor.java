package com.free.aiassist.asr.stt.io.worker;

public interface SttProcessor<T> {
    void process(final T t, Callback callback);

    interface Callback {
        void onProcessComplete(String txt);
    }
}
