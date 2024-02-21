package com.free.aiassist.nlu.jointbert.slots;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.free.aiassist.nlu.api.NluIntent;
import com.free.aiassist.nlu.api.NluSlots;
import com.free.aiassist.nlu.api.NluIntentAndSlots;
import com.free.aiassist.nlu.jointbert.slots.bean.LaunchSlots;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LaunchSlotsResolver extends NluSlotsResolverBase {
    @Override
    protected void resolveNluIntent(NluIntentAndSlots nluIntentAndSlots) {
        nluIntentAndSlots.setNluIntent(NluIntent.LAUNCH);
    }

    @Override
    protected void resolveNluSlotsContent(NluIntentAndSlots nluIntentAndSlots, JsonNode slotsNode) {
        LaunchSlots launchSlots = new LaunchSlots();
        if (slotsNode.getNodeType() == JsonNodeType.OBJECT) {
            String name = getTextFromJsonNode(slotsNode, "name");
            launchSlots.setAppName(name);
            nluIntentAndSlots.setNluSlots(launchSlots);
        }
    }
}
