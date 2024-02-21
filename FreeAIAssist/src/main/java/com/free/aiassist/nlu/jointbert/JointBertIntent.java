package com.free.aiassist.nlu.jointbert;

import lombok.Getter;

@Getter
public enum JointBertIntent {
    LAUNCH("LAUNCH"),
    QUERY("QUERY"),
    PLAY("PLAY"),
    WEATHER_QUERY("WEATHER_QUERY"),
    UNKNOWN("");

    private final String intentType;

    private JointBertIntent(String intentType) {
        this.intentType = intentType;
    }
}
