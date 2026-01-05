package com.bank.graphql_gateway.model;

public class SuspendAccountInput {
    private String reason;
    private String suspendedBy;

    public SuspendAccountInput() {
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getSuspendedBy() {
        return suspendedBy;
    }

    public void setSuspendedBy(String suspendedBy) {
        this.suspendedBy = suspendedBy;
    }
}
