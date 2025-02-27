package com.timesync.integration.bamboohr.model;

import com.timesync.AbstractDto;

import java.time.LocalDate;

public class TimeRegistrationEntry extends AbstractDto {

    LocalDate date;
    double hours;

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public double getHours() {
        return hours;
    }

    public void setHours(double hours) {
        this.hours = hours;
    }
}
