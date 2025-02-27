package com.timesync.integration.bamboohr.model;

import com.timesync.AbstractDto;

public class TimeOffRequestStatus extends AbstractDto {
    private String status;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
