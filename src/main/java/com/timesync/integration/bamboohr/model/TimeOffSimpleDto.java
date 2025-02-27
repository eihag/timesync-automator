package com.timesync.integration.bamboohr.model;

import java.time.LocalDate;

public record TimeOffSimpleDto(LocalDate start, LocalDate end, String name) {
}
