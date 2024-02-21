package com.free.aiassist.asr.stt.io.nio;

import com.free.aiassist.asr.stt.io.SttException;

import java.nio.channels.SelectionKey;

public interface SttNioHandler {
    void handleAccept(SelectionKey key) throws SttException;
    void handleRead(SelectionKey key) throws SttException;
    void handleWrite(SelectionKey key) throws SttException;
}
