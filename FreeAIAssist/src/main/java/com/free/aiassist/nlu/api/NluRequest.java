package com.free.aiassist.nlu.api;

import lombok.Data;

@Data
public class NluRequest {
    private String query;
    private String content;
    private NluIntent intent;
    private NluIntentAndSlots intentAndSlots;
}
