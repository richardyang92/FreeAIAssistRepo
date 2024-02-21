package com.free.aiassist.nlu.api;

import lombok.Data;

@Data
public class NluIntentAndSlots {
    private NluIntent nluIntent;
    private NluSlots nluSlots;
}
