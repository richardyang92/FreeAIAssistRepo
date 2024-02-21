package com.free.aiassist;

import com.free.aiassist.asr.sherpa.SttSherpaEngine;
import com.free.aiassist.asr.stt.engine.SttConfig;
import com.free.aiassist.asr.stt.engine.SttEngine;
import com.free.aiassist.asr.stt.io.nio.SttNioInput;
import com.free.aiassist.asr.stt.io.nio.SttNioOutput;
import com.free.aiassist.asr.stt.media.SttStreamMedia;
import com.free.aiassist.nlu.NluTextProcessor;
import com.free.aiassist.nlu.api.*;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class AIAssist implements SttEngine.OnSttUpdateListener, NluProcessor.OnProcessComplete {
    private static final String API_URL = "http://localhost:5000/queryIntentAndFillSlots";
    private static final int MAX_REPEAT_COUNT = 10;

    private final SttEngine sttEngine;
    private final NluProcessor nluProcessor;
    private ExecutorService executorService;

    private String curSttText;
    private int repeatNum;

    private final AtomicBoolean aiAssistPause;
    private final Lock aiAssistLock;
    private final Condition aiAssistCond;

    public AIAssist() {
//        this.nluProcessor = new NluTextProcessor()
//                .addNluHandler(new JointBertIntentQueries(API_URL))
//                .addNluHandler(new JointBertIntentParser())
//                .addNluHandler(new JointBertSlotsParser())
//                .addNluHandler(new JointBertEnWordModifier())
//                .addListener(this);
        this.nluProcessor = new NluTextProcessor()
                .addNluHandler(new NluHandlerBase() {
                    @Override
                    public void setNextHandler(NluHandler nextHandler) {
                        // empty
                    }

                    @Override
                    public void handleNluRequest(NluRequest request) {
                        // do nothing
                    }

                    @Override
                    public boolean hasNextHandler() {
                        return false;
                    }

                    @Override
                    public NluHandler nextHandler() {
                        return null;
                    }
                })
                .addListener(this);

        SttConfig sttConfig = SttConfig.createConfig(.2f, 5.f);
        sttConfig.setDebugFilePrefix("src/main/resources");
        this.sttEngine = new SttSherpaEngine.Builder()
                .configure(sttConfig)
//                .setMedia(new SttRecordMedia(sttConfig))
                .setMedia(new SttStreamMedia.Builder()
                        .bindPort(9999)
                        .setNioInput(new SttNioInput())
                        .setNioOutput(new SttNioOutput())
                        .setSttConfig(sttConfig)
                        .build())
                .setListener(this)
                .build();
        this.curSttText = "";
        this.aiAssistPause = new AtomicBoolean(false);
        this.aiAssistLock = new ReentrantLock();
        this.aiAssistCond = this.aiAssistLock.newCondition();
    }

    public void start() {
        if (this.executorService == null) {
            this.executorService = Executors.newFixedThreadPool(2);
        }
        this.sttEngine.init();
        this.sttEngine.start();
    }

    public void stop() {
        this.sttEngine.stop();
        this.sttEngine.release();
        if (this.executorService != null) {
            this.executorService.shutdown();
        }
    }

    @Override
    public void onSttUpdate(String txt) {
        if (!this.curSttText.equals(txt)) {
            this.curSttText = txt;
            log.info("stt: {}", this.curSttText);
        } else {
            if (this.curSttText.isEmpty()) return;
            if (repeatNum++ < MAX_REPEAT_COUNT) return;
            repeatNum = 0;
            this.executorService.submit(() -> {
                this.aiAssistLock.lock();
                this.sttEngine.pause();
                this.aiAssistPause.getAndSet(true);
                this.aiAssistCond.signal();
                this.aiAssistLock.unlock();
            });
            this.executorService.submit(() ->
                    this.nluProcessor.processText(this.curSttText));
        }
    }

    @Override
    public void onProcessComplete(String query, NluIntentAndSlots intentAndSlots) {
        this.aiAssistLock.lock();
        log.info("query={}, intentAndSlots={}", query, intentAndSlots);
        if (!this.aiAssistPause.get()) {
            try {
                this.aiAssistCond.await();
            } catch (InterruptedException e) {
                log.error(e.getLocalizedMessage());
            }
        }
        this.curSttText = "";
        this.sttEngine.resume();
        this.aiAssistPause.getAndSet(false);
        this.aiAssistLock.unlock();
    }

    public static void main(String[] args) {
        AIAssist aiAssist = new AIAssist();
        aiAssist.start();
        log.info("AIAssist start");
        try {
            Thread.sleep(120 * 1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        aiAssist.stop();
        log.info("AIAssist stop");
    }
}
