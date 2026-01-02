package com.bank.authservice.messaging.event;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class UserLoginFailureEvent {

    private String username;
    private String reason;
    private Instant timestamp;
}
