package com.timesync;

import com.timesync.service.TimeSyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class TimeSyncCommandLineRunner implements CommandLineRunner {
    private static final Logger LOG = LoggerFactory.getLogger(TimeSyncCommandLineRunner.class);

    private final TimeSyncService timeSyncService;

    public TimeSyncCommandLineRunner(TimeSyncService timeSyncService) {
        this.timeSyncService = timeSyncService;
    }

    @Override
    public void run(String... args) {
        if (args == null || args.length < 2) {
            showHelp();
            return;
        }
        LocalDate startDate = DateUtil.parseDate(args[1]);
        LocalDate endDate = DateUtil.parseDate(args[2]);

        switch (args[0]) {
            case "dry-run",
                 "log-work" -> runTimeSyncService(startDate, endDate, args);
            case "report" -> runReport(startDate, endDate);
            default -> {
                LOG.error("Unknown argument: '{}'", args[0]);
                showHelp();
            }
        }
    }

    private void runTimeSyncService(LocalDate startDate, LocalDate endDate, String... args) {
        boolean dryRun = "dry-run".equalsIgnoreCase(args[0]);
        final boolean autoFill;
        if (args.length >= 3 && "--autofill".equalsIgnoreCase(args[3])) {
            autoFill = true;
        } else {
            autoFill = false;
        }
        timeSyncService.logBambooWork(startDate, endDate, dryRun, autoFill);
    }

    private void runReport(LocalDate startDate, LocalDate endDate) {
        timeSyncService.reportJiraAndBamboo(startDate, endDate, false);
    }

    public void showHelp() {
        LOG.info("Arguments: ");
        LOG.info(" dry-run <startDate> <endDate>");
        LOG.info(" log-work <startDate> <endDate>");
        LOG.info(" report <startDate> <endDate>");
        LOG.info("");
    }
}
