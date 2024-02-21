package com.free.aiassist.nlu.jointbert;

import com.free.aiassist.nlu.api.*;
import com.free.aiassist.nlu.jointbert.slots.bean.LaunchSlots;
import com.free.aiassist.nlu.jointbert.slots.bean.MovieSlots;
import com.free.aiassist.nlu.jointbert.slots.bean.MusicSlots;
import com.free.aiassist.nlu.jointbert.slots.bean.WeatherSlots;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class JointBertEnWordModifier extends NluHandlerBase {
    @Override
    public void handleNluRequest(NluRequest request) {
        NluIntentAndSlots nluIntentAndSlots = request.getIntentAndSlots();
        if (nluIntentAndSlots == null) return;
        NluIntent nluIntent = nluIntentAndSlots.getNluIntent();
        NluSlots nluSlots = nluIntentAndSlots.getNluSlots();
        if (nluIntent == null || nluSlots == null) return;
        switch (nluIntent) {
            case LAUNCH:
                LaunchSlots launchSlots = (LaunchSlots) nluSlots;
                modifyLaunchSlotsEnWords(launchSlots);
                break;
            case MOVIE:
                MovieSlots movieSlots = (MovieSlots) nluSlots;
                modifyMovieSlotsEnWords(movieSlots);
                break;
            case MUSIC:
                MusicSlots musicSlots = (MusicSlots) nluSlots;
                modifyMusicSlotsEnWords(musicSlots);
                break;
            case WEATHER:
                WeatherSlots weatherSlots = (WeatherSlots) nluSlots;
                modifyWeatherSlotsEnWords(weatherSlots);
                break;
            default:
                break;
        }
        log.info("after modify: {}", nluSlots);
    }

    private void modifyLaunchSlotsEnWords(LaunchSlots launchSlots) {
        String name = launchSlots.getAppName();
        launchSlots.setAppName(modifyEnWord(name));

    }

    private void modifyMovieSlotsEnWords(MovieSlots movieSlots) {
        List<String> artists = movieSlots.getArtists();
        List<String> movies = movieSlots.getMovies();
        movieSlots.setArtists(modifyEnWordList(artists));
        movieSlots.setMovies(modifyEnWordList(movies));
    }

    private void modifyMusicSlotsEnWords(MusicSlots musicSlots) {
        List<String> artists = musicSlots.getArtists();
        List<String> songs = musicSlots.getSongs();
        musicSlots.setArtists(modifyEnWordList(artists));
        musicSlots.setSongs(modifyEnWordList(songs));
    }

    private void modifyWeatherSlotsEnWords(WeatherSlots weatherSlots) {
        String dataTime = weatherSlots.getDataTime();
        weatherSlots.setDataTime(modifyEnWord(dataTime));
        String location = weatherSlots.getLocationCity();
        weatherSlots.setLocationCity(modifyEnWord(location));
        String subFocus = weatherSlots.getSubFocus();
        weatherSlots.setSubFocus(modifyEnWord(subFocus));

    }

    private List<String> modifyEnWordList(List<String> wordList) {
        if (wordList == null || wordList.isEmpty()) return wordList;
        List<String> newWordList = new ArrayList<>();
        for (String word : wordList) {
            String newWord = modifyEnWord(word);
            newWordList.add(newWord);
        }
        return newWordList;
    }

    private String modifyEnWord(String word) {
        if (word.isEmpty()) return word;
        if (!isEnWord(word)) return word;
        StringBuilder sb = new StringBuilder();
        char[] chs = word.toCharArray();
        for (int i = 0; i < chs.length; i++) {
            if (isEnChar(chs[i])) {
                sb.append(chs[i]);
                if (i < chs.length - 1
                        && isEnChar(chs[i + 1])) {
                    sb.append(' ');
                }
            } else {
                if (isSpaceChar(chs[i])
                        || isSharpChar(chs[i]))
                    continue;
                sb.append(chs[i]);
            }
        }
        return sb.toString();
    }

    private boolean isEnWord(String word) {
        char[] chs = word.toCharArray();
        boolean isEnWord = true;
        for (char ch : chs) {
            if (!isEnChar(ch)
                    && !isSpaceChar(ch)
                    && !isSharpChar(ch)) {
                isEnWord = false;
                break;
            }
        }
        return isEnWord;
    }

    private boolean isEnChar(char ch) {
        return (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z');
    }

    private boolean isSpaceChar(char ch) {
        return ch == ' ';
    }

    private boolean isSharpChar(char ch) {
        return ch == '#';
    }
}
