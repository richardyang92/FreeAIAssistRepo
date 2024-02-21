package com.free.aiassist.asr.stt.io;

import java.io.IOException;

public class SttException extends IOException {
    public SttException() {
        super();
    }

    public SttException(String message) {
        super(message);
    }

    public SttException(String message, Throwable cause) {
        super(message, cause);
    }

    public SttException(Throwable cause) {
        super(cause);
    }
}
