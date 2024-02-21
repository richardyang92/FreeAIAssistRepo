package com.free.aiassist.nlu.jointbert.slots;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.free.aiassist.nlu.api.NluIntent;
import com.free.aiassist.nlu.api.NluSlots;
import com.free.aiassist.nlu.api.NluIntentAndSlots;
import com.free.aiassist.nlu.jointbert.slots.bean.MusicSlots;

import java.util.List;

public class MusicSlotsResolver extends NluSlotsResolverBase {
    @Override
    protected void resolveNluIntent(NluIntentAndSlots nluIntentAndSlots) {
        nluIntentAndSlots.setNluIntent(NluIntent.MUSIC);
    }

    @Override
    protected void resolveNluSlotsContent(NluIntentAndSlots nluIntentAndSlots, JsonNode slotsNode) {
        JsonNode artistList = slotsNode.get("artist");
        JsonNode songList = slotsNode.get("song");

        MusicSlots musicSlots = new MusicSlots();

        if (artistList != null
                && artistList.getNodeType() == JsonNodeType.ARRAY) {
            List<String> artists = createListFromJsonNode(artistList);
            musicSlots.setArtists(artists);
        }

        if (songList != null
                && songList.getNodeType() == JsonNodeType.ARRAY) {
            List<String> songs = createListFromJsonNode(songList);
            musicSlots.setSongs(songs);
        }
        nluIntentAndSlots.setNluSlots(musicSlots);
    }
}
