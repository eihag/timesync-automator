package com.timesync.integration.bamboohr.model;

import com.timesync.AbstractDto;

import java.time.LocalDate;

public class TimesheetRegisterClockEntry extends AbstractDto {
    private String  employeeId;
    private LocalDate date;
    private String start;
    private String end;

    public TimesheetRegisterClockEntry(String employeeId, LocalDate date, String start, String end) {
        this.employeeId = employeeId;
        this.date = date;
        this.start = start;
        this.end = end;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
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
