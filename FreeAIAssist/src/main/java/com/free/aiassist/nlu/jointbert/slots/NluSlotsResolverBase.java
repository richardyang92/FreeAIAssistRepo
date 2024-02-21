package com.free.aiassist.nlu.jointbert.slots;

import com.fasterxml.jackson.databind.JsonNode;
import com.free.aiassist.nlu.api.NluSlotsResolver;
import com.free.aiassist.nlu.api.NluIntentAndSlots;

public abstract class NluSlotsResolverBase implements NluSlotsResolver {
    @Override
    public NluIntentAndSlots resolve(JsonNode slotsNode) {
        NluIntentAndSlots nluIntentAndSlots = new NluIntentAndSlots();
        resolveNluIntent(nluIntentAndSlots);
        resolveNluSlotsContent(nluIntentAndSlots, slotsNode);
        return nluIntentAndSlots;
    }

    protected abstract void resolveNluIntent(NluIntentAndSlots nluIntentAndSlots);

    protected abstract void resolveNluSlotsContent(NluIntentAndSlots nluIntentAndSlots, JsonNode slotsNode);
}
