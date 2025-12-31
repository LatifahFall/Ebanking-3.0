package com.ebanking.payment.entity;

public enum ReversalReason {
    FRAUD,
    CUSTOMER_REQUEST,
    TECHNICAL_ERROR,
    DUPLICATE_PAYMENT,
    INSUFFICIENT_FUNDS,
    ACCOUNT_CLOSED,
    UNAUTHORIZED_TRANSACTION,
    SYSTEM_ERROR
}

