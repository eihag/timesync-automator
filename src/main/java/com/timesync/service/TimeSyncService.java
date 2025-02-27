package com.timesync.service;

import com.timesync.DateUtil;
import com.timesync.integration.bamboohr.BambooIcalClient;
import com.timesync.integration.bamboohr.BambooRestClient;
import com.timesync.integration.bamboohr.model.CompanyHolidaySimpleDto;
import com.timesync.integration.bamboohr.model.TimeOffSimpleDto;
import com.timesync.integration.bamboohr.model.TimeRegistrationEntry;
import com.timesync.integration.bamboohr.model.TimesheetRegisterClockEntries;
import com.timesync.integration.bamboohr.model.TimesheetRegisterClockEntry;
import com.timesync.integration.jira.JiraRestClient;
import com.timesync.integration.jira.model.Issue;
import com.timesync.integration.jira.model.IssueList;
import com.timesync.integration.jira.model.WorkLog;
import com.timesync.integration.jira.model.WorkLogList;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class TimeSyncService {
    private static final Logger LOG = LoggerFactory.getLogger(TimeSyncService.class);

    @Value("${bamboohr_employee_number}")
    private String employeeNumber;

    @Value("${start_time}")
    private String startTime;

    @Value("${work_day}")
    private double workDayHours;

    private final JiraRestClient jiraRestClient;
    private final BambooRestClient bambooRestClient;
    private final BambooIcalClient bambooIcalClient;

    public TimeSyncService(JiraRestClient jiraRestClient, BambooRestClient bambooRestClient, BambooIcalClient bambooIcalClient) {
        this.jiraRestClient = jiraRestClient;
        this.bambooRestClient = bambooRestClient;
        this.bambooIcalClient = bambooIcalClient;
    }

    public void reportJiraAndBamboo(LocalDate startDate, LocalDate endDate, boolean bambooOnly) {
        StringBuilder sb = new StringBuilder("\nDate\t\t\t\t\tJIRA\tBamboo\n");
        List<TimeRegistrationEntry> timeRegistrationEntries = bambooRestClient.getTimeRegistrationForDatePeriod(startDate, endDate);
        List<TimeOffSimpleDto> vacations = getVacations(startDate, endDate);
        List<CompanyHolidaySimpleDto> companyHolidays = bambooIcalClient.getCompanyHolidays();

        LocalDate date = startDate;
        while (!endDate.isBefore(date)) {
            if (!isCompanyHoliday(date, companyHolidays, sb) &&
                    !isVacation(date, vacations, sb) &&
                    isNotWeekend(date, sb)) {
                long jiraLoggedSeconds = bambooOnly ? 0 : getJiraWork(date);
                double bambooWorkHours = getBambooWorkHours(date, timeRegistrationEntries);
                writeDateEntry(date, jiraLoggedSeconds / 3600d + "\t\t" + bambooWorkHours, sb);
            }
            date = date.plusDays(1);
        }
        LOG.info("{}", sb);
    }

    public void logBambooWork(LocalDate startDate, LocalDate endDate, boolean dryRun, boolean autoFill) {
        if (dryRun) {
            LOG.info("DRY-RUN - not actually logging work");
        }
        StringBuilder sb = new StringBuilder("Registered in Bamboo:\n");

        TimesheetRegisterClockEntries newEntries = new TimesheetRegisterClockEntries();
        List<TimeRegistrationEntry> timeRegistrationEntries = bambooRestClient.getTimeRegistrationForDatePeriod(startDate, endDate);
        List<TimeOffSimpleDto> vacations = getVacations(startDate, endDate);
        List<CompanyHolidaySimpleDto> companyHolidays = bambooIcalClient.getCompanyHolidays();

        LocalDate date = startDate;
        while (!endDate.isBefore(date)) {
            if (!isCompanyHoliday(date, companyHolidays, sb) &&
                    !isVacation(date, vacations, sb) &&
                    isNotWeekend(date, sb)) {
                int loggedTime = (int) (getBambooWorkHours(date, timeRegistrationEntries) * 60);
                int expectedTime;
                if (autoFill) {
                    expectedTime = (int) (workDayHours * 60);
                } else {
                    expectedTime = getJiraWork(date) / 60;
                }

                if (loggedTime < expectedTime) {
                    addNewBambooTimesheetEntry(expectedTime, loggedTime, date, newEntries, sb);
                }
            }

            date = date.plusDays(1);
        }

        if (!dryRun && !newEntries.getEntries().isEmpty()) {
            bambooRestClient.registerTime(newEntries);
        }

        if (newEntries.getEntries().isEmpty()) {
            sb.append("None\n");
        }

        LOG.info("{}", sb);
    }

    private void addNewBambooTimesheetEntry(int expectedTime, int loggedTime, LocalDate date, TimesheetRegisterClockEntries newEntries, StringBuilder sb) {
        int remainingTime = expectedTime - loggedTime;
        if (remainingTime > 0) {
            LocalTime time = LocalTime.parse(startTime);
            LocalTime endTime = time.plusMinutes(remainingTime);

            TimesheetRegisterClockEntry entry = new TimesheetRegisterClockEntry(employeeNumber, date, startTime, endTime.toString());
            newEntries.addEntry(entry);
            writeDateEntry(date, remainingTime + " minutes", sb);
        }
    }

    private double getBambooWorkHours(LocalDate date, List<TimeRegistrationEntry> timeRegistrationEntries) {
        return timeRegistrationEntries.stream()
                .filter(t -> date.equals(t.getDate()))
                .map(t -> t.getHours())
                .findFirst().orElse(0d);
    }


    private boolean isVacation(LocalDate date, List<TimeOffSimpleDto> vacations, StringBuilder sb) {
        for (TimeOffSimpleDto vacation : vacations) {
            if (!date.isAfter(vacation.end()) && !vacation.start().isAfter(date)) {
                writeDateEntry(date, vacation.name(), sb);
                return true;
            }
        }
        return false;
    }

    private List<TimeOffSimpleDto> getVacations(LocalDate startDate, LocalDate endDate) {
        return bambooRestClient.getTimeOffRequestForDatePeriod(startDate, endDate).stream()
                .filter(r -> r.getStatus() != null && !"superceded".equals(r.getStatus().getStatus()))
                .map((r -> new TimeOffSimpleDto(DateUtil.parseDate(r.getStart()),
                        DateUtil.parseDate(r.getEnd()),
                        r.getType().getName())))
                .toList();
    }


    private boolean isCompanyHoliday(LocalDate date, List<CompanyHolidaySimpleDto> companyHolidays, StringBuilder sb) {
        for (CompanyHolidaySimpleDto vacation : companyHolidays) {
            if (date.equals(vacation.date())) {
                writeDateEntry(date, vacation.name(), sb);
                return true;
            }
        }
        return false;
    }

    private int getJiraWork(LocalDate date) {
        int alreadyLoggedSeconds = 0;
        IssueList issueList = jiraRestClient.getMyIssuesWithWorkLoggedForDate(date);
        if (issueList != null && issueList.getIssues() != null) {
            String myAccountId = null;
            for (Issue issue : issueList.getIssues()) {
                if (myAccountId == null) {
                    myAccountId = issue.getFields().getAssignee().getAccountId();
                }
                alreadyLoggedSeconds += getJiraIssueWorkLogTotals(issue, date, myAccountId);
            }
            LOG.debug("You already logged {}m of work in {} issue(s)", alreadyLoggedSeconds / 60, issueList.getIssues().size());
        } else {
            LOG.debug("No work logged");
        }
        return alreadyLoggedSeconds;
    }

    private int getJiraIssueWorkLogTotals(Issue issue, LocalDate date, String myAccountId) {
        WorkLogList workLogList = jiraRestClient.getWorkLogDetails(issue.getKey());

        if (workLogList != null && workLogList.getWorklogs() != null) {
            return workLogList.getWorklogs().stream()
                    .filter(w -> myAccountId.equals(w.getAuthor().getAccountId()))
                    .filter(w -> date.equals(w.getStarted().toLocalDate()))
                    .mapToInt(WorkLog::getTimeSpentSeconds)
                    .sum();
        }
        return 0;
    }

    private boolean isNotWeekend(LocalDate date, StringBuilder sb) {
        boolean isWeekend = date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY;
        if (isWeekend) {
            writeDateEntry(date, "Weekend", sb);
        }
        return !isWeekend;
    }

    private void writeDateEntry(LocalDate date, String message, StringBuilder sb) {
        sb.append(date)
                .append('\t')
                .append(StringUtils.rightPad(
                        StringUtils.capitalize(date.getDayOfWeek().name().toLowerCase()), 9))
                .append('\t')
                .append(message)
                .append('\n');
    }

}
