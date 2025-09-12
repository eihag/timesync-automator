package com.timesync.integration.jira.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.timesync.AbstractDto;

public class TimeTracking extends AbstractDto {

    @JsonProperty("originalEstimateSeconds")
    private int originalEstimate;

    // Remaining time estimate
    @JsonProperty("remainingEstimateSeconds")
    private int remainingEstimate;

    @JsonProperty("timeSpentSeconds")
    private int timeSpent;

    public int getOriginalEstimate() {
        return originalEstimate;
    }

    public TimeTracking setTimeOriginalEstimate(int timeOriginalEstimate) {
        this.originalEstimate = timeOriginalEstimate;
        return this;
    }

    public int getRemainingEstimate() {
        return remainingEstimate;
    }

    public TimeTracking setTimeEstimate(int timeEstimate) {
        this.remainingEstimate = timeEstimate;
        return this;
    }

    public int getTimeSpent() {
        return timeSpent;
    }

    public TimeTracking setTimeSpent(int timeSpent) {
        this.timeSpent = timeSpent;
        return this;
    }

}
