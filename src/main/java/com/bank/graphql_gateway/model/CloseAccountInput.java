package com.bank.graphql_gateway.model;

public class CloseAccountInput {
    private String closureReason;
    private String closedBy;

    public CloseAccountInput() {
    }

    public String getClosureReason() {
        return closureReason;
    }

    public void setClosureReason(String closureReason) {
        this.closureReason = closureReason;
    }

    public String getClosedBy() {
        return closedBy;
    }

    public void setClosedBy(String closedBy) {
        this.closedBy = closedBy;
    }
}
