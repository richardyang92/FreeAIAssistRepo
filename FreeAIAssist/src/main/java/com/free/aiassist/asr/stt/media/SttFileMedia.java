package com.free.aiassist.asr.stt.media;

import com.free.aiassist.asr.stt.io.SttException;
import com.free.aiassist.asr.stt.io.SttMedia;
import lombok.extern.slf4j.Slf4j;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;

@Slf4j
public class SttFileMedia implements SttMedia<byte[], String> {
    private final String path;
    private File sttFile;
    private AudioInputStream sttStream;
    private int markPos;
    private boolean eof;

    public SttFileMedia(String path) {
        this.path = path;
        this.markPos = 0;
        this.eof = false;
    }

    @Override
    public void open() throws SttException {
        this.sttFile = new File(this.path);
        if (!this.sttFile.exists())
            throw new SttException(String.format("file %s not exists!", this.path));
        try {
            sttStream = AudioSystem.getAudioInputStream(sttFile);
        } catch (UnsupportedAudioFileException | IOException e) {
            throw new SttException(e);
        }
    }

    @Override
    public void close() throws SttException {
        if (this.sttStream != null) {
            try {
                this.sttStream.close();
            } catch (IOException e) {
                throw new SttException(e);
            } finally {
                this.sttStream = null;
                this.sttFile = null;
            }
        }
    }

    @Override
    public boolean isOpened() {
        return this.sttFile != null
                && this.sttStream != null;
    }

    @Override
    public boolean isEof() {
        return this.eof;
    }

    @Override
    public int available() {
        int availableSize = 0;
        if (this.sttStream != null) {
            try {
                availableSize = Math.min(this.sttStream.available(), 4096);
            } catch (IOException e) {
                log.error(e.getLocalizedMessage());
            }
        }
        return availableSize;
    }

    @Override
    public int read(byte[] data) throws SttException {
        int readLen;
        try {
           readLen = this.sttStream.read(data);
        } catch (IOException e) {
            throw new SttException(e);
        }
        return readLen;
    }

    @Override
    public String getDescriptor() {
        return String.format("file://%s", this.path);
    }

    @Override
    public int read(byte[] bytes, int len) throws SttException {
        int increment = 0;
        try {
            increment = this.sttStream.read(bytes, markPos, len);
        } catch (IOException e) {
            throw new SttException(e);
        } finally {
            markPos += increment;
        }
        this.eof = increment == -1;
        return increment;
    }
}
