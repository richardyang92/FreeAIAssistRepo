package com.free.aiassist.asr.stt.engine;

public interface SttNativeEngine<T> {
    void start(String modelName);
    default void resume() { }
    default void pause() { }
    void stop();
    boolean isInit();
    boolean vad(T audios);
    String transcribe(T audios);

    default float[] convertPcmToAudioData(byte[] pcm, int len) {
        float[] floats = new float[len / 2];
        for (int i = 0, j = 0; i < pcm.length; i += 2, j++) {
            int intSample = (int) (pcm[i + 1]) << 8 | (int) (pcm[i]) & 0xFF;
            floats[j] = intSample / 32767.0f;
        }
        return floats;
    }
}
