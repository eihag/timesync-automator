package com.timesync.integration.bamboohr.model;

import com.timesync.AbstractDto;

public class TimeOffRequestType extends AbstractDto {

    private String name;
    private String icon;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }
}
