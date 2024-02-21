package com.free.aiassist.asr.stt.media;

import com.free.aiassist.asr.stt.engine.SttConfig;
import com.free.aiassist.asr.stt.io.SttException;
import com.free.aiassist.asr.stt.io.SttMedia;
import com.free.aiassist.asr.stt.io.nio.SttNioInput;
import com.free.aiassist.asr.stt.io.nio.SttNioOutput;
import com.free.aiassist.asr.stt.io.nio.SttNioServer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SttStreamMedia extends SttNioServer implements SttMedia<byte[], String> {
    public SttStreamMedia(int bindPort,
                          SttNioInput sttNioInput,
                          SttNioOutput sttNioOutput,
                          SttConfig sttConfig) {
        super(bindPort, sttNioInput, sttNioOutput, sttConfig);
    }

    @Override
    public void open() throws SttException {
        start();
    }

    @Override
    public void close() throws SttException {
        stop();
    }

    @Override
    public boolean isOpened() {
        return isOpen();
    }

    @Override
    public boolean isEof() {
        return isInterrupt();
    }

    @Override
    public int available() {
        return this.sttByteBuffer.size();
    }

    @Override
    public int read(byte[] data) throws SttException {
        return read(data, data.length);
    }

    @Override
    public int read(byte[] bytes, int len) throws SttException {
        int readLen = 0;
        this.sttLock.lock();
        try {
            while (getSttBufferSize() == 0) {
                this.readCond.await();
            }
//            log.info("bytes size={}", bytes.length);
            this.sttNioOutput.consume(this.sttByteBuffer, bytes);
            readLen = len;
            this.writeCond.signal();
        } catch (InterruptedException e) {
            log.error(e.getLocalizedMessage());
        } finally {
            this.sttLock.unlock();
        }
        return readLen;
    }

    @Override
    public String getDescriptor() {
        return String.format("socket://localhost:%s", this.getBindPort());
    }

    @Override
    public boolean isWritable() {
        return true;
    }

    @Override
    public void write(String data) {
        this.sttWriter.write(data + "\n");
    }

    public static class Builder extends AbstractBuilder<SttStreamMedia> {
        public SttStreamMedia build() {
            return new SttStreamMedia(this.bindPort, this.sttNioInput, this.sttNioOutput, this.sttConfig);
        }
    }
}
