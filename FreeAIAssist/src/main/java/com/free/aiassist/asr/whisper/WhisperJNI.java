package com.free.aiassist.asr.whisper;

import com.free.aiassist.asr.stt.engine.SttNativeEngine;

public class WhisperJNI implements SttNativeEngine<byte[]> {
    static {
        System.loadLibrary("whisper_jni");
    }

    private boolean whisperInit;

    @Override
    public void start(String modelName) {
        this.whisperInit = init(modelName);
    }

    @Override
    public void stop() {
        close();
    }

    @Override
    public boolean isInit() {
        return this.whisperInit;
    }

    @Override
    public boolean vad(byte[] audios) {
        float[] pcmF32 = convertPcmToAudioData(audios, audios.length);
        return vadSimple(pcmF32);
    }

    @Override
    public String transcribe(byte[] audios) {
        float[] pcmF32 = convertPcmToAudioData(audios, audios.length);
        return fullTranscribe(pcmF32);
    }

    public native boolean init(String modelPath);

    public native String fullTranscribe(float[] audios);

    public native boolean vadSimple(float[] audios);

    public native void close();
}
