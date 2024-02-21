package com.free.aiassist.asr.stt.io.worker;

public interface SttInput<T, B> {
    void produce(T t, B b);
}
