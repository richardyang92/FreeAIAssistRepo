package com.free.aiassist.asr.stt.engine;

import com.free.aiassist.asr.stt.io.SttBuffer;
import com.free.aiassist.asr.stt.io.SttException;
import com.free.aiassist.asr.stt.io.SttMedia;
import com.free.aiassist.asr.stt.io.worker.SttInput;
import com.free.aiassist.asr.stt.io.worker.SttOutput;
import com.free.aiassist.asr.stt.io.worker.SttProcessor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public abstract class SttDefaultEngine<I, O>
        extends SttEngineBase<I> implements SttProcessor.Callback {
    protected final BlockingQueue<O> sttQueue = new LinkedBlockingQueue<>();

    protected SttBuffer<I> sttBuffer;
    private Lock sttBufferLock;
    private Condition readCond;
    private Condition writeCond;
    private Semaphore queueSem;

    private Lock sttPauseLock;
    private AtomicBoolean pauseFlag;
    private AtomicBoolean transcribeFlag;
    private Condition pauseCond;
    private Condition transcribeCond;

    protected ByteArrayInput<I> sttInput;
    protected SttPacketOutput<I, O> sttOutput;
    protected SttSpeechProcessor<O> sttProcessor;
    protected SttNativeEngine<I> sttNativeEngine;

    protected Thread sttProducer;
    protected Thread sttConsumer;
    protected Thread sttParser;

    protected SttDefaultEngine(SttConfig sttConfig,
                               SttMedia<I, String> sttMedia,
                               OnSttUpdateListener sttOnSttUpdateListener) {
        super(sttConfig, sttMedia, sttOnSttUpdateListener);
        initBufferLocks();
        initTranscribeLocks();
        initSttNativeEngine();
        initSttBuffer();
        initSttInput();
        initSttOutput();
        initSttParser();
    }

    @Override
    public void start() {
        if (!this.sttNativeEngine.isInit()) {
            log.error("whisper model init failed");
            return;
        }
        super.start();
    }

    @Override
    public void pause() {
        log.info("stt engine pause");
        this.sttPauseLock.lock();
        try {
            if (this.transcribeFlag.get())
                this.pauseCond.await();
        } catch (InterruptedException e) {
            log.error(e.getLocalizedMessage());
        }
        this.sttNativeEngine.pause();
        this.pauseFlag.getAndSet(true);
        this.sttPauseLock.unlock();
    }

    @Override
    public void resume() {
        log.info("stt engine resume");
        this.sttPauseLock.lock();
        this.sttNativeEngine.resume();
        this.pauseFlag.getAndSet(false);
        this.transcribeCond.signal();
        this.sttPauseLock.unlock();
    }

    @Override
    public void stop() {
        super.stop();
        if (this.sttNativeEngine.isInit()) {
            this.sttNativeEngine.stop();
        }
    }

    @Override
    public boolean isPause() {
        return this.pauseFlag.get();
    }

    @Override
    protected void doStt() throws SttException {
        if (!this.sttNativeEngine.isInit()) {
            log.error("whisper engine isn't init");
            return;
        }
        if (!this.sttMedia.isOpened()) {
            log.error("media {} is not opened", this.sttMedia.getDescriptor());
            return;
        }
        this.sttProducer = new Thread(() -> {
            while (this.sttMedia.isOpened()
                    && !this.sttMedia.isEof()) {
                if (this.pauseFlag.get()) continue;
                this.sttBufferLock.lock();
                int curBuffSize = this.sttBuffer.size();
                int buffTotalCap = this.sttBuffer.capacity();
                try {
                    if (curBuffSize + this.sttMedia.available() > buffTotalCap) {
                        this.writeCond.await();
                    }
                    this.sttInput.produce(this.sttMedia, this.sttBuffer);
                    this.readCond.signal();
                } catch (InterruptedException e) {
                    log.error(e.getLocalizedMessage());
                } finally {
                    this.sttBufferLock.unlock();
                }
            }
        });
        this.sttConsumer = new Thread(() -> {
            while (!isSttTaskDone()) {
                if (this.pauseFlag.get()) continue;
                this.sttBufferLock.lock();
                try {
                    if (this.sttBuffer.size() == 0) {
                        this.readCond.await();
                    }
                    this.queueSem.acquire();
                    this.sttOutput.consume(this.sttBuffer, this.sttQueue);
                    this.queueSem.release();
                    this.writeCond.signal();
                } catch (InterruptedException e) {
                    log.error(e.getLocalizedMessage());
                } finally {
                    this.sttBufferLock.unlock();
                }
            }
        });
        this.sttParser = new Thread(() -> {
            while (!isSttTaskDone()) {
                this.sttPauseLock.lock();
                try {
                    if (this.pauseFlag.get())
                        this.transcribeCond.await();
                } catch (InterruptedException e) {
                    log.error(e.getLocalizedMessage());
                }

                this.transcribeFlag.getAndSet(true);
                try {
                    this.queueSem.acquire();
                    this.sttProcessor.process(this.sttQueue, this);
                    this.queueSem.release();
                } catch (InterruptedException e) {
                    log.error(e.getLocalizedMessage());
                } finally {
                    this.transcribeFlag.getAndSet(false);
                    this.pauseCond.signal();
                }
                this.sttPauseLock.unlock();
            }
        });

        this.sttProducer.start();
        this.sttConsumer.start();
        this.sttParser.start();

        try {
            this.sttProducer.join();
            this.sttConsumer.join();
            this.sttParser.join();
        } catch (InterruptedException e) {
            throw new SttException(e);
        }
    }

    private void initBufferLocks() {
        this.sttBufferLock = new ReentrantLock();
        this.readCond = this.sttBufferLock.newCondition();
        this.writeCond = this.sttBufferLock.newCondition();
        this.queueSem = new Semaphore(1);
    }

    private void initTranscribeLocks() {
        this.pauseFlag = new AtomicBoolean(false);
        this.transcribeFlag = new AtomicBoolean(false);
        this.sttPauseLock = new ReentrantLock();
        this.pauseCond = this.sttPauseLock.newCondition();
        this.transcribeCond = this.sttPauseLock.newCondition();
    }

    protected abstract void initSttNativeEngine();

    protected abstract void initSttBuffer();

    protected abstract void initSttInput();

    protected abstract void initSttOutput();

    protected abstract void initSttParser();

    protected interface ByteArrayInput<I> extends SttInput<SttMedia<I, String>, SttBuffer<I>> { }
    protected interface SttPacketOutput<I, O> extends SttOutput<SttBuffer<I>, BlockingQueue<O>> { }
    protected interface SttSpeechProcessor<E> extends SttProcessor<BlockingQueue<E>> { }

    protected byte[] readNBytesBuffer(SttBuffer<byte[]> sttBuffer, int len) {
        int alreadyRead = 0;
        byte[] out = new byte[len];
        while (alreadyRead < len) {
            int readLen = sttBuffer.read(out, len - alreadyRead);
            alreadyRead += readLen;
        }
        return out;
    }

    protected void writeNBytesBuffer(SttBuffer<byte[]> sttBuffer, byte[] in, int len) {
        int alreadyWritten = 0;
        while (alreadyWritten < len) {
            int writtenLen = sttBuffer.write(in, len - alreadyWritten);
            alreadyWritten += writtenLen;
        }
    }

    protected int calculateBufferSizePerSec() {
        int sampleRate = this.sttConfig.getSampleRate();
        int channels = this.sttConfig.getChannels();
        int sampleFormat = this.sttConfig.getSampleFormat();
        return sampleRate * channels * sampleFormat / 8;
    }
}
