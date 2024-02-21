package com.free.aiassist.asr.constant;

import lombok.Getter;

@Getter
public enum Language {
    EN("en"), ZH("zh");

    private final String locale;

    Language(String locale) {
        this.locale = locale;
    }
}
