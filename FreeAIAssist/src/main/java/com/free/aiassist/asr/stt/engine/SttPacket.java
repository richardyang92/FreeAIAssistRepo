package com.free.aiassist.asr.stt.engine;

import lombok.Data;

@Data
public class SttPacket<T> {
    private long serialNum;
    private T audios;
    private int len;

    public SttPacket(T audios, int len) {
        this.audios = audios;
        this.len = len;
        this.serialNum = System.currentTimeMillis();
    }
}
