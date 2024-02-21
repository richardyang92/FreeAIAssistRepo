package com.free.aiassist.nlu;

import com.free.aiassist.nlu.api.NluHandler;
import com.free.aiassist.nlu.api.NluIntentAndSlots;
import com.free.aiassist.nlu.api.NluProcessor;
import com.free.aiassist.nlu.api.NluRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NluTextProcessor implements NluProcessor {
    private NluHandler first;
    private OnProcessComplete onProcessComplete;

    public NluTextProcessor() {
        this.first = null;
    }

    public NluProcessor addNluHandler(NluHandler handler) {
        if (this.first == null)
            this.first = handler;
        else {
            NluHandler handlerPtr = this.first;
            while (handlerPtr.hasNextHandler()) {
                handlerPtr = handlerPtr.nextHandler();
            }
            handlerPtr.setNextHandler(handler);
        }
        return this;
    }

    @Override
    public NluProcessor addListener(OnProcessComplete onProcessComplete) {
        this.onProcessComplete = onProcessComplete;
        return this;
    }

    @Override
    public void processText(String query) {
        log.info("process text: {}", query);
        NluRequest nluRequest = new NluRequest();
        nluRequest.setQuery(query);
        this.first.handleNluRequest(nluRequest);
        NluIntentAndSlots intentAndSlots = nluRequest.getIntentAndSlots();
        log.info("process end: {}", intentAndSlots);
        if (this.onProcessComplete != null) {
            this.onProcessComplete.onProcessComplete(query, intentAndSlots);
        }
    }
}
