package com.free.aiassist.test;

import com.free.aiassist.asr.sherpa.SttSherpaEngine;
import com.free.aiassist.asr.stt.engine.SttConfig;
import com.free.aiassist.asr.stt.engine.SttEngine;
import com.free.aiassist.asr.stt.io.nio.SttNioInput;
import com.free.aiassist.asr.stt.io.nio.SttNioOutput;
import com.free.aiassist.asr.stt.media.SttStreamMedia;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AsrApplication {

    public static void main(String[] args) {
        SttConfig sttConfig = SttConfig.createConfig(.2f, 5f);
        sttConfig.setDebugFilePrefix("src/main/resources");
        SttEngine sttEngine = new SttSherpaEngine.Builder()
                .configure(sttConfig)
//                .setMedia(new SttRecordMedia(sttConfig))
                .setMedia(new SttStreamMedia.Builder()
                        .bindPort(9999)
                        .setNioInput(new SttNioInput())
                        .setNioOutput(new SttNioOutput())
                        .setSttConfig(sttConfig)
                        .build())
                .setListener(txt -> log.info("stt: {}", txt))
                .build();
        sttEngine.init();
        sttEngine.start();

//        try {
//            Thread.sleep(10 * 1000);
//        } catch (InterruptedException e) {
//            log.error(e.getLocalizedMessage());
//        }
//
//        sttEngine.pause();
//
        try {
            Thread.sleep(120 * 1000);
        } catch (InterruptedException e) {
            log.error(e.getLocalizedMessage());
        }
//
//        sttEngine.resume();
//
//        try {
//            Thread.sleep(10 * 1000);
//        } catch (InterruptedException e) {
//            log.error(e.getLocalizedMessage());
//        }

        sttEngine.stop();
        sttEngine.release();
    }
}