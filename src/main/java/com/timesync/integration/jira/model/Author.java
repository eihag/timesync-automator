package com.timesync.integration.jira.model;

import com.timesync.AbstractDto;

public class Author extends AbstractDto {

    private String accountId;

    public String getAccountId() {
        return accountId;
    }

    public Author setAccountId(String accountId) {
        this.accountId = accountId;
        return this;
    }
}
