package com.timesync;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;

@Configuration
@ImportRuntimeHints(TimeSyncNativeRuntimeHints.class)
public class TimeSyncConfiguration {
}
