package com.free.aiassist.asr.sherpa;

import com.free.aiassist.asr.stt.engine.SttNativeEngine;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SherpaJNI implements SttNativeEngine<byte[]> {
    static {
        System.loadLibrary("sherpa_jni");
    }

    private boolean sherpaInit;

    public native boolean init(String modelDir);

    public native void reset();
    public native void restart();

    public native String fullTranscribe(float[] audios);

    public native void close();

    @Override
    public void start(String modelName) {
        this.sherpaInit = init(modelName);
    }

    @Override
    public void resume() {
        restart();
    }

    @Override
    public void pause() {
        reset();
    }

    @Override
    public void stop() {
        close();
    }

    @Override
    public boolean isInit() {
        return this.sherpaInit;
    }

    @Override
    public boolean vad(byte[] audios) {
        return true;
    }

    @Override
    public String transcribe(byte[] audios) {
        float[] pcmF32 = convertPcmToAudioData(audios, audios.length);
        return fullTranscribe(pcmF32);
    }
}
