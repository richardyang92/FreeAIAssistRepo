package com.free.aiassist.nlu.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;

import java.util.ArrayList;
import java.util.List;

public interface NluSlotsResolver {
    NluIntentAndSlots resolve(JsonNode slotsNode);

    default String getTextFromJsonNode(JsonNode jsonNode, String key) {
        JsonNode keyNode = jsonNode.get(key);
        if (keyNode == null) return "";
        List<String> valueArray = createListFromJsonNode(keyNode);
        if (valueArray == null || valueArray.isEmpty()) return "";
        return valueArray.get(0);
    }

    default List<String> createListFromJsonNode(JsonNode jsonNode) {
        if (jsonNode == null) return null;
        List<String> array = new ArrayList<>();
        for (int i = 0; i < jsonNode.size(); i++) {
            JsonNode artistNode = jsonNode.get(i);
            if (artistNode.getNodeType() == JsonNodeType.STRING) {
                array.add(artistNode.asText());
            }
        }
        return array;
    }
}
