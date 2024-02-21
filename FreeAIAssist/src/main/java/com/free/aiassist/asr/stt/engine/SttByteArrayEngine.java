package com.free.aiassist.asr.stt.engine;

import com.free.aiassist.asr.stt.io.SttByteBuffer;
import com.free.aiassist.asr.stt.io.SttException;
import com.free.aiassist.asr.stt.io.SttMedia;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@Slf4j
public abstract class SttByteArrayEngine extends SttDefaultEngine<byte[], SttPacket<byte[]>> {
    private static final boolean DEBUG = false;

    private FileOutputStream debugPcmFos;

    protected SttByteArrayEngine(SttConfig sttConfig, SttMedia<byte[], String> sttMedia, OnSttUpdateListener sttOnSttUpdateListener) {
        super(sttConfig, sttMedia, sttOnSttUpdateListener);
    }

    @Override
    public void start() {
        super.start();
        if (DEBUG) {
            try {
                File debugRecord = new File(
                        this.sttConfig.getDebugFilePrefix(),
                        "debug.pcm");
                if (debugRecord.exists()) {
                    boolean ret = debugRecord.delete();
                    log.info("del {} ret={}", debugRecord.getName(), ret);
                }
                if (debugRecord.createNewFile()) {
                    this.debugPcmFos = new FileOutputStream(debugRecord);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void stop() {
        super.stop();
        if (DEBUG && this.debugPcmFos != null) {
            try {
                this.debugPcmFos.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    protected void initSttBuffer() {
        float maxBufferSecs = this.sttConfig.getMaxBufferSecs();
        int buffCap = (int) (calculateBufferSizePerSec() * maxBufferSecs);
        this.sttBuffer = new SttByteBuffer(buffCap);
    }

    @Override
    protected void initSttInput() {
        this.sttInput = (sttMedia, sttBuffer) -> {
            try {
                int inSize = sttMedia.available();
                if (inSize <= 0) return;
                byte[] in = new byte[inSize];
                int readLen = sttMedia.read(in);
                if (readLen < 0) log.error("read from {} failed", sttMedia.getDescriptor());
                else {
//                    log.info("inSize: {}, readLen: {}", inSize, readLen);
                    writeNBytesBuffer(sttBuffer, in, readLen);
                }
            } catch (SttException e) {
                log.error(e.getLocalizedMessage());
            }
        };
    }

    @Override
    protected void initSttOutput() {
        float vadPktSecs = this.sttConfig.getVadPktSecs();
        final int vadPktSize = (int) (calculateBufferSizePerSec() * vadPktSecs);

        this.sttOutput = (sttBuffer, sttQueue) -> {
            if (sttBuffer.size() < vadPktSize) return;
            byte[] vadBuffer = readNBytesBuffer(sttBuffer, vadPktSize);
            boolean hasAudio = this.sttNativeEngine.vad(vadBuffer);

            if (hasAudio) {
                try {
                    sttQueue.put(new SttPacket<>(vadBuffer, vadPktSize));
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    @Override
    protected void initSttParser() {
        float vadPktSecs = this.sttConfig.getVadPktSecs();
        float maxPktSecs = this.sttConfig.getMaxPktSecs();
        final int maxPktCount = (int) (maxPktSecs / vadPktSecs);
        final int vadPktSize = (int) (calculateBufferSizePerSec() * vadPktSecs);

        this.sttProcessor = (sttQueue, callback) -> {
            if (sttQueue.isEmpty()) return;
            try {
                int pktCount = Math.min(sttQueue.size(), maxPktCount);
                int pcmPktSize = vadPktSize * pktCount;
                byte[] pcmPkt = new byte[pcmPktSize];
                for (int i = 0; i < pktCount; i++) {
                    SttPacket<byte[]> sttPacket = sttQueue.take();
                    byte[] vadPkt = sttPacket.getAudios();
                    System.arraycopy(vadPkt, 0, pcmPkt, i * vadPktSize, vadPktSize);
                }
                if (DEBUG && this.debugPcmFos != null) {
                    try {
                        this.debugPcmFos.write(pcmPkt, 0, pcmPktSize);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
//                long currentTime = System.currentTimeMillis();
//                log.info("receive time: {}", currentTime);
                String txt = this.sttNativeEngine.transcribe(pcmPkt);
                if (callback != null) {
                    callback.onProcessComplete(txt);
                }
                if (this.sttMedia.isWritable()) {
                    this.sttMedia.write(txt);
                }
            } catch (InterruptedException e) {
                log.error(e.getLocalizedMessage());
            }
        };
    }

    @Override
    public void onProcessComplete(String txt) {
        this.sttOnSttUpdateListener.onSttUpdate(txt);
    }
}
