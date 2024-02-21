package com.free.aiassist.asr.stt.io;

public interface SttBuffer<T> {
    int write(T in, int len);
    int read(T out, int len);
    int size();
    int capacity();
}
