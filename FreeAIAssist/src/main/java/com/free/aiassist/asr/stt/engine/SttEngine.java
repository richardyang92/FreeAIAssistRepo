package com.free.aiassist.asr.stt.engine;

public interface SttEngine {
    void init();
    void start();
    void pause();
    void resume();
    void stop();
    void release();
    boolean isPause();

    interface OnSttUpdateListener {
        void onSttUpdate(String txt);
    }
}
