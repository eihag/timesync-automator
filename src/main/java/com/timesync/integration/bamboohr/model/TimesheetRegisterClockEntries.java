package com.timesync.integration.bamboohr.model;

import com.timesync.AbstractDto;

import java.util.ArrayList;
import java.util.List;

public class TimesheetRegisterClockEntries extends AbstractDto {

    private List<TimesheetRegisterClockEntry> entries;

    public TimesheetRegisterClockEntries() {
        entries = new ArrayList<>();
    }

    public List<TimesheetRegisterClockEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<TimesheetRegisterClockEntry> entries) {
        this.entries = entries;
    }

    public void addEntry(TimesheetRegisterClockEntry entry) {
        entries.add(entry);
    }

}
