package com.free.aiassist.nlu.api;

import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class NluHandlerBase implements NluHandler {
    protected NluHandler nextHandler;
    protected ObjectMapper objectMapper;

    public NluHandlerBase() {
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void setNextHandler(NluHandler nextHandler) {
        this.nextHandler = nextHandler;
    }

    @Override
    public boolean hasNextHandler() {
        return this.nextHandler != null;
    }

    @Override
    public NluHandler nextHandler() {
        return this.nextHandler;
    }
}
