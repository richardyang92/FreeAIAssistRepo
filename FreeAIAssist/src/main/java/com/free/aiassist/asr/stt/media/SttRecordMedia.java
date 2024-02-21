package com.free.aiassist.asr.stt.media;

import com.free.aiassist.asr.stt.engine.SttConfig;
import com.free.aiassist.asr.stt.io.SttException;
import com.free.aiassist.asr.stt.io.SttMedia;

import javax.sound.sampled.*;

public class SttRecordMedia implements SttMedia<byte[], String> {
    private final DataLine.Info info;
    private TargetDataLine line;
    private boolean open;

    public SttRecordMedia(SttConfig sttConfig) {
        int sampleRate = sttConfig.getSampleRate();
        int channels = sttConfig.getChannels();
        int sampleFormat = sttConfig.getSampleFormat();
        AudioFormat format = new AudioFormat(sampleRate,
                sampleFormat, channels, true, false);
        this.info = new DataLine.Info(TargetDataLine.class, format);
        this.open = false;
    }

    @Override
    public void open() throws SttException {
        if (!AudioSystem.isLineSupported(this.info)) return;
        try {
            this.line = (TargetDataLine) AudioSystem.getLine(this.info);
            this.line.open();
            this.line.start();
        } catch (LineUnavailableException e) {
            throw new SttException(e);
        }
        this.open = true;
    }

    @Override
    public void close() throws SttException {
        this.open = false;
        this.line.stop();
        this.line.close();
    }

    @Override
    public boolean isOpened() {
        return this.open;
    }

    @Override
    public boolean isEof() {
        return !this.open;
    }

    @Override
    public int available() {
        return this.line.available();
    }

    @Override
    public int read(byte[] data) throws SttException {
        return this.line.read(data, 0, data.length);
    }

    @Override
    public int read(byte[] data, int len) throws SttException {
        return this.line.read(data, 0, len);
    }

    @Override
    public String getDescriptor() {
        return "capture:0";
    }
}
