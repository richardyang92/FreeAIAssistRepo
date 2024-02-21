package com.free.aiassist.nlu.jointbert.slots;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.free.aiassist.nlu.api.NluIntent;
import com.free.aiassist.nlu.api.NluIntentAndSlots;
import com.free.aiassist.nlu.jointbert.slots.bean.MovieSlots;

import java.util.List;

public class MovieSlotsResolver extends NluSlotsResolverBase {
    @Override
    protected void resolveNluIntent(NluIntentAndSlots nluIntentAndSlots) {
        nluIntentAndSlots.setNluIntent(NluIntent.MOVIE);
    }

    @Override
    protected void resolveNluSlotsContent(NluIntentAndSlots nluIntentAndSlots, JsonNode slotsNode) {
        JsonNode artistList = slotsNode.get("artist");
        JsonNode movieList = slotsNode.get("name");

        MovieSlots movieSlots = new MovieSlots();

        if (artistList != null
                && artistList.getNodeType() == JsonNodeType.ARRAY) {
            List<String> artists = createListFromJsonNode(artistList);
            movieSlots.setArtists(artists);
        }

        if (movieList != null
                && movieList.getNodeType() == JsonNodeType.ARRAY) {
            List<String> movies = createListFromJsonNode(movieList);
            movieSlots.setMovies(movies);
        }
        nluIntentAndSlots.setNluSlots(movieSlots);
    }
}
