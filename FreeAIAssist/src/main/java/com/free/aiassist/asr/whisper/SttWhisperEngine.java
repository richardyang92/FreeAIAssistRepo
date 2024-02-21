package com.free.aiassist.asr.whisper;

import com.free.aiassist.asr.stt.engine.SttByteArrayEngine;
import com.free.aiassist.asr.stt.engine.SttConfig;
import com.free.aiassist.asr.stt.engine.SttEngineBase;
import com.free.aiassist.asr.stt.io.SttMedia;

public class SttWhisperEngine extends SttByteArrayEngine {
    public static final String MODEL_NAME = "ggml-large-v2.bin";

    private SttWhisperEngine(SttConfig sttConfig, SttMedia<byte[], String> sttMedia, OnSttUpdateListener sttOnSttUpdateListener) {
        super(sttConfig, sttMedia, sttOnSttUpdateListener);
    }

    @Override
    protected void initSttNativeEngine() {
        this.sttNativeEngine = new WhisperJNI();
        this.sttNativeEngine.start(MODEL_NAME);
    }

    public static class Builder extends SttEngineBase.Builder<SttWhisperEngine, byte[]> {

        @Override
        public SttWhisperEngine build() {
            return new SttWhisperEngine(this.sttConfig, this.sttMedia, this.sttOnSttUpdateListener);
        }
    }
}
