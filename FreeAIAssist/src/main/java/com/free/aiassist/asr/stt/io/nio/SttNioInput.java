package com.free.aiassist.asr.stt.io.nio;

import com.free.aiassist.asr.stt.io.SttByteBuffer;
import com.free.aiassist.asr.stt.io.worker.SttInput;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SttNioInput implements SttInput<byte[], SttByteBuffer> {

    @Override
    public void produce(byte[] bytes, SttByteBuffer sttByteBuffer) {
        int readLen = bytes.length;
        if (sttByteBuffer.size() + readLen > sttByteBuffer.capacity()) {
            log.error("buff size isn't enough");
            return;
        }
        int alreadyWritten = 0;
        while (alreadyWritten < readLen) {
            int writtenLen = sttByteBuffer.write(bytes, readLen - alreadyWritten);
            alreadyWritten += writtenLen;
        }
    }
}
