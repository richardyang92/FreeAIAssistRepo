package com.free.aiassist.nlu.jointbert;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.free.aiassist.nlu.api.*;
import com.free.aiassist.nlu.jointbert.slots.LaunchSlotsResolver;
import com.free.aiassist.nlu.jointbert.slots.MovieSlotsResolver;
import com.free.aiassist.nlu.jointbert.slots.MusicSlotsResolver;
import com.free.aiassist.nlu.jointbert.slots.WeatherSlotsResolver;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JointBertSlotsParser extends NluHandlerBase {
    @Override
    public void handleNluRequest(NluRequest request) {
        NluIntent nluIntent = request.getIntent();
        NluIntentAndSlots nluIntentAndSlots;
        try {
            JsonNode contentNode = this.objectMapper.readTree(request.getContent());
            JsonNode slotsNode = contentNode.get("slots");
            log.info("slotsNode={}, nluIntent={}", slotsNode, nluIntent);
            NluSlotsResolver slotsResolver = getNluSlotsResolver(nluIntent);
            if (slotsResolver != null) {
                nluIntentAndSlots = slotsResolver.resolve(slotsNode);
                request.setIntentAndSlots(nluIntentAndSlots);
            }
        } catch (JsonProcessingException e) {
            log.error(e.getLocalizedMessage());
        } finally {
            log.info("nluSlots={}", request.getIntentAndSlots());
            if (this.nextHandler != null) {
                this.nextHandler.handleNluRequest(request);
            }
        }
    }

    private NluSlotsResolver getNluSlotsResolver(NluIntent nluIntent) {
        NluSlotsResolver slotsResolver;

        if (nluIntent == NluIntent.MOVIE) {
            slotsResolver = new MovieSlotsResolver();
        } else if (nluIntent == NluIntent.LAUNCH) {
            slotsResolver = new LaunchSlotsResolver();
        } else if (nluIntent == NluIntent.MUSIC) {
            slotsResolver = new MusicSlotsResolver();
        } else if (nluIntent == NluIntent.WEATHER) {
            slotsResolver = new WeatherSlotsResolver();
        } else {
            slotsResolver = null;
        }
        return slotsResolver;
    }
}
