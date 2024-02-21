package com.free.aiassist.asr.stt.io;

public interface SttMedia<I, O> {
    void open() throws SttException;
    void close() throws SttException;
    boolean isOpened();
    boolean isEof();
    int available();
    int read(I data) throws SttException;
    int read(I data, int len) throws SttException;
    String getDescriptor();

    default boolean isWritable() {
        return false;
    }

    default void write(O data) { }
}
