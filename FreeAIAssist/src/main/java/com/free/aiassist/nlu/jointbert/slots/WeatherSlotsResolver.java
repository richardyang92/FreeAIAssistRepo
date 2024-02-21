package com.free.aiassist.nlu.jointbert.slots;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.free.aiassist.nlu.api.NluIntent;
import com.free.aiassist.nlu.api.NluIntentAndSlots;
import com.free.aiassist.nlu.jointbert.slots.bean.WeatherSlots;

public class WeatherSlotsResolver extends NluSlotsResolverBase {
    @Override
    protected void resolveNluIntent(NluIntentAndSlots nluIntentAndSlots) {
        nluIntentAndSlots.setNluIntent(NluIntent.WEATHER);
    }

    @Override
    protected void resolveNluSlotsContent(NluIntentAndSlots nluIntentAndSlots, JsonNode slotsNode) {
        WeatherSlots weatherSlots = new WeatherSlots();

        if (slotsNode.getNodeType() == JsonNodeType.OBJECT) {
            String dataTime = getTextFromJsonNode(slotsNode, "datetime_date");
            weatherSlots.setDataTime(dataTime);

            String location = getTextFromJsonNode(slotsNode, "location_city");
            weatherSlots.setLocationCity(location);

            String subFocus = getTextFromJsonNode(slotsNode, "subfocus");
            weatherSlots.setSubFocus(subFocus);

            nluIntentAndSlots.setNluSlots(weatherSlots);
        }
    }
}
