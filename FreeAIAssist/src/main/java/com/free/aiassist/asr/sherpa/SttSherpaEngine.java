package com.free.aiassist.asr.sherpa;

import com.free.aiassist.asr.stt.engine.SttByteArrayEngine;
import com.free.aiassist.asr.stt.engine.SttConfig;
import com.free.aiassist.asr.stt.engine.SttEngineBase;
import com.free.aiassist.asr.stt.io.SttMedia;

public class SttSherpaEngine extends SttByteArrayEngine {
    public static final String MODEL_PATH = "/Users/yangyang/ThirdParty/k2-fsa/sherpa-ncnn/sherpa-ncnn-streaming-zipformer-bilingual-zh-en-2023-02-13/";

    protected SttSherpaEngine(SttConfig sttConfig, SttMedia<byte[], String> sttMedia, OnSttUpdateListener sttOnSttUpdateListener) {
        super(sttConfig, sttMedia, sttOnSttUpdateListener);
    }

    @Override
    protected void initSttNativeEngine() {
        this.sttNativeEngine = new SherpaJNI();
        this.sttNativeEngine.start(MODEL_PATH);
    }

    public static class Builder extends SttEngineBase.Builder<SttSherpaEngine, byte[]> {

        @Override
        public SttSherpaEngine build() {
            return new SttSherpaEngine(this.sttConfig, this.sttMedia, this.sttOnSttUpdateListener);
        }
    }
}
