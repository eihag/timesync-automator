package com.timesync.integration.jira.model;

import com.timesync.AbstractDto;

public class Assignee extends AbstractDto {

    private String accountId;

    public String getAccountId() {
        return accountId;
    }

    public Assignee setAccountId(String accountId) {
        this.accountId = accountId;
        return this;
    }
}
