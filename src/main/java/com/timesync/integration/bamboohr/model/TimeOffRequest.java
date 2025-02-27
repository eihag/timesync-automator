package com.timesync.integration.bamboohr.model;

import com.timesync.AbstractDto;

public class TimeOffRequest extends AbstractDto {

    private TimeOffRequestStatus status;
    private TimeOffRequestType type;
    private String start;
    private String end;

    public TimeOffRequestStatus getStatus() {
        return status;
    }

    public void setStatus(TimeOffRequestStatus status) {
        this.status = status;
    }

    public TimeOffRequestType getType() {
        return type;
    }

    public void setType(TimeOffRequestType type) {
        this.type = type;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }
}
