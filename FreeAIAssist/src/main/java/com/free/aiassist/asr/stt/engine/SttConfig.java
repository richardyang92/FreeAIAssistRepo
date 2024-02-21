package com.free.aiassist.asr.stt.engine;

import com.free.aiassist.asr.constant.Language;
import com.free.aiassist.asr.constant.PCMFormat;
import lombok.Data;

@Data
public class SttConfig {
    private Language language;
    private int sampleRate;
    private int channels;
    private int sampleFormat;
    private float vadPktSecs;
    private float maxPktSecs;
    private float maxBufferSecs;
    private String debugFilePrefix;

    public static SttConfig createConfig(float minPktSecs, float maxPktSecs) {
        SttConfig sttConfig = new SttConfig();
        sttConfig.setSampleRate(PCMFormat.SAMPLE_RATE_16000);
        sttConfig.setChannels(PCMFormat.AUDIO_CHANNEL_MONO);
        sttConfig.setSampleFormat(PCMFormat.SAMPLE_FORMAT_16BIT);
        sttConfig.setLanguage(Language.ZH);
        sttConfig.setVadPktSecs(minPktSecs);
        sttConfig.setMaxPktSecs(maxPktSecs);
        sttConfig.setMaxBufferSecs(15.0f);
        return sttConfig;
    }
}
