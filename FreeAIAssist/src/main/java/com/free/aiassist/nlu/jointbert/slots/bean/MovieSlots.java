package com.free.aiassist.nlu.jointbert.slots.bean;

import com.free.aiassist.nlu.api.NluSlots;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString
public class MovieSlots extends NluSlots {
    private List<String> artists;
    private List<String> movies;

}
