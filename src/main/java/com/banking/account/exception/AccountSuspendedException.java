package com.banking.account.exception;

/**
 * Exception thrown when attempting to perform operations on a suspended account
 */
public class AccountSuspendedException extends RuntimeException {

    private Long accountId;
    private String suspensionReason;

    public AccountSuspendedException(String message) {
        super(message);
    }

    public AccountSuspendedException(String message, Throwable cause) {
        super(message, cause);
    }

    public AccountSuspendedException(String message, Long accountId) {
        super(message);
        this.accountId = accountId;
    }

    public AccountSuspendedException(String message, Long accountId, String suspensionReason) {
        super(message);
        this.accountId = accountId;
        this.suspensionReason = suspensionReason;
    }

    public Long getAccountId() {
        return accountId;
    }

    public String getSuspensionReason() {
        return suspensionReason;
    }
}