package com.free.aiassist.test;

import lombok.extern.slf4j.Slf4j;

import javax.sound.sampled.*;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

@Slf4j
public class AsrNIOClient {
    public static void main(String[] args) {

        AudioFormat format = new AudioFormat(16000, 16, 1, true, false);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
        if (!AudioSystem.isLineSupported(info)) {
            return;
        }

        long now = System.currentTimeMillis();

        try {
            TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info);
            line.open();
            line.start();

            String host = "localhost";
            int port = 9999;
            try {
                Socket socket = new Socket(host, port);
                DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

                int bytesRead;

                while (System.currentTimeMillis() - now <= 20 * 1000) {
                    int totalLen = line.available();
                    if (totalLen == 0) continue;
//                    log.info("totalLen: {}", totalLen);
                    byte[] buffer = new byte[totalLen];
                    bytesRead = line.read(buffer, 0, buffer.length);
                    dos.write(buffer, 0, bytesRead);
//                    long currentTime = System.currentTimeMillis();
//                    log.info("send time: {}, write bytes: {}", currentTime, bytesRead);
                }

                dos.close();
                socket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            line.stop();
            line.close();
        } catch (LineUnavailableException e) {
            throw new RuntimeException(e);
        }
    }
}
