package com.free.aiassist.asr.stt.engine;

import com.free.aiassist.asr.stt.io.SttException;
import com.free.aiassist.asr.stt.io.SttMedia;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Slf4j
public abstract class SttEngineBase<T> implements SttEngine {
    private static final int N_MAX_PROCESS = Runtime.getRuntime().availableProcessors() + 1;

    protected SttMedia<T, String> sttMedia;
    protected SttConfig sttConfig;
    protected OnSttUpdateListener sttOnSttUpdateListener;

    @Getter
    private boolean sttTaskDone;
    private ExecutorService sttService;
    private Future<Void> sttFuture;

    protected SttEngineBase(SttConfig sttConfig,
                            SttMedia<T, String> sttMedia,
                            OnSttUpdateListener sttOnSttUpdateListener) {
        this.sttConfig = sttConfig;
        this.sttMedia = sttMedia;
        this.sttOnSttUpdateListener = sttOnSttUpdateListener;
        this.sttTaskDone = false;
    }

    @Override
    public void init() {
        if (sttService == null) {
            sttService = Executors.newFixedThreadPool(N_MAX_PROCESS);
        }
    }

    @Override
    public void start() {
        if (!this.sttMedia.isOpened()) {
            try {
                this.sttMedia.open();
            } catch (SttException e) {
                log.error("open {} failed", this.sttMedia.getDescriptor());
                return;
            }
        }
        final Callable<Void> sttTask = () -> {
            doStt();
            return null;
        };
        this.sttFuture = this.sttService.submit(sttTask);
    }

    @Override
    public void stop() {
        this.sttTaskDone = true;
        if (this.sttMedia.isOpened()) {
            try {
                this.sttMedia.close();
            } catch (SttException e) {
                log.error("can't close {}", this.sttMedia.getDescriptor());
            }
        }
        if (!this.sttFuture.isCancelled()) {
            this.sttFuture.cancel(true);
        }
    }

    @Override
    public void release() {
        if (sttService != null) {
            sttService.shutdown();
        }
    }

    protected abstract void doStt() throws SttException;

    public abstract static class Builder<E extends SttEngineBase<I>, I> {
        protected SttMedia<I, String> sttMedia;
        protected SttConfig sttConfig;
        protected OnSttUpdateListener sttOnSttUpdateListener;

        public Builder<E, I> configure(SttConfig config) {
            this.sttConfig = config;
            return this;
        }

        public Builder<E, I> setMedia(SttMedia<I, String> media) {
            this.sttMedia = media;
            return this;
        }

        public Builder<E, I> setListener(OnSttUpdateListener onSttUpdateListener) {
            this.sttOnSttUpdateListener = onSttUpdateListener;
            return this;
        }

        public abstract E build();
    }
}
