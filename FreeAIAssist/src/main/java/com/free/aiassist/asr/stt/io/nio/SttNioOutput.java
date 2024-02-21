package com.free.aiassist.asr.stt.io.nio;

import com.free.aiassist.asr.stt.io.SttByteBuffer;
import com.free.aiassist.asr.stt.io.worker.SttOutput;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SttNioOutput implements SttOutput<SttByteBuffer, byte[]> {
    @Override
    public void consume(SttByteBuffer sttByteBuffer, byte[] bytes) {
        int writeLen = bytes.length;
        if (writeLen == 0) return;

        byte[] out = new byte[writeLen];
        int alreadyRead = 0;

//        sttByteBuffer.dump();
//        log.info("stt buff size: {}, writeLen: {}", sttByteBuffer.size(), writeLen);

        while (alreadyRead < writeLen) {
            int readLen = sttByteBuffer.read(out, writeLen - alreadyRead);
            copyBytesFromOffset(out, bytes, alreadyRead, readLen);
            alreadyRead += readLen;
        }
    }

    private void copyBytesFromOffset(byte[] source, byte[] dest, int offset, int len) {
        if (len >= 0) System.arraycopy(source, 0, dest, offset, len);
    }
}
