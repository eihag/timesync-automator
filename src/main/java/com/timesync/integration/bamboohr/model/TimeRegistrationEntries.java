package com.timesync.integration.bamboohr.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.timesync.NativeSerializable;

import java.util.ArrayList;


@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TimeRegistrationEntries extends ArrayList<TimeRegistrationEntry> implements NativeSerializable {
}
