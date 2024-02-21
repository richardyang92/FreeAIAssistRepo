package com.free.aiassist.nlu.jointbert.slots.bean;

import com.free.aiassist.nlu.api.NluSlots;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString
public class WeatherSlots extends NluSlots {
    private String dataTime;
    private String locationCity;
    private String subFocus;
}
