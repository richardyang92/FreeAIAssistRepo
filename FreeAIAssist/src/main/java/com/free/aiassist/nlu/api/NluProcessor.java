package com.free.aiassist.nlu.api;

public interface NluProcessor {
    NluProcessor addNluHandler(NluHandler handler);
    NluProcessor addListener(OnProcessComplete onProcessComplete);
    void processText(String query);

    interface OnProcessComplete {
        void onProcessComplete(String query, NluIntentAndSlots intentAndSlots);
    }
}
