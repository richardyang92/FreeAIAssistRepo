package com.free.aiassist.nlu.api;

public interface NluHandler {
    void setNextHandler(NluHandler nextHandler);
    void handleNluRequest(NluRequest request);
    boolean hasNextHandler();
    NluHandler nextHandler();
}
