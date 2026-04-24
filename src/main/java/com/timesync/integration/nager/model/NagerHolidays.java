package com.timesync.integration.nager.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.timesync.NativeSerializable;

import java.util.ArrayList;


@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NagerHolidays extends ArrayList<NagerHoliday> implements NativeSerializable {
}
