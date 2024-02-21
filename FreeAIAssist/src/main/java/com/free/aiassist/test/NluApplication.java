package com.free.aiassist.test;

import com.free.aiassist.nlu.NluTextProcessor;
import com.free.aiassist.nlu.api.NluProcessor;
import com.free.aiassist.nlu.jointbert.JointBertEnWordModifier;
import com.free.aiassist.nlu.jointbert.JointBertIntentParser;
import com.free.aiassist.nlu.jointbert.JointBertIntentQueries;
import com.free.aiassist.nlu.jointbert.JointBertSlotsParser;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NluApplication {
    private static final String API_URL = "http://localhost:5000/queryIntentAndFillSlots";
    public static void main(String[] args) {
        NluProcessor nluProcessor = new NluTextProcessor()
                .addNluHandler(new JointBertIntentQueries(API_URL))
                .addNluHandler(new JointBertIntentParser())
                .addNluHandler(new JointBertSlotsParser())
                .addNluHandler(new JointBertEnWordModifier());

        nluProcessor.processText("今天武汉天气怎么样");
    }
}
