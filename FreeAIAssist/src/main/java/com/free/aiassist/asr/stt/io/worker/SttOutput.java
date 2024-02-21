package com.free.aiassist.asr.stt.io.worker;

public interface SttOutput<B, T> {
    void consume(B b, T t);
}
