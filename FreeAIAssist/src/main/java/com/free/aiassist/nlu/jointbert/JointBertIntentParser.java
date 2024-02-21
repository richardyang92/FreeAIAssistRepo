package com.free.aiassist.nlu.jointbert;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.free.aiassist.nlu.api.NluHandlerBase;
import com.free.aiassist.nlu.api.NluIntent;
import com.free.aiassist.nlu.api.NluRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JointBertIntentParser extends NluHandlerBase {

    @Override
    public void handleNluRequest(NluRequest request) {
        String content = request.getContent();
        try {
            JsonNode contentNode = this.objectMapper.readTree(content);
            JsonNode intentNode = contentNode.get("intent");
            String intent = "";
            if (intentNode.getNodeType() == JsonNodeType.STRING) {
                intent = intentNode.textValue();
            }
            log.info("intent={}", intent);

            if (intent.equals(JointBertIntent.LAUNCH.getIntentType())) {
                request.setIntent(NluIntent.LAUNCH);
            } else if (intent.equals(JointBertIntent.PLAY.getIntentType())) {
                request.setIntent(NluIntent.MUSIC);
            } else if (intent.equals(JointBertIntent.QUERY.getIntentType())) {
                request.setIntent(NluIntent.MOVIE);
            } else if (intent.equals(JointBertIntent.WEATHER_QUERY.getIntentType())) {
                request.setIntent(NluIntent.WEATHER);
            } else {
                request.setIntent(NluIntent.UNKNOWN);
            }
        } catch (JsonProcessingException e) {
            log.error(e.getLocalizedMessage());
        } finally {
            log.info("request.getIntent()={}", request.getIntent());
            if (request.getIntent() != NluIntent.UNKNOWN
                    && this.nextHandler != null) {
                this.nextHandler.handleNluRequest(request);
            }
        }
    }


}
