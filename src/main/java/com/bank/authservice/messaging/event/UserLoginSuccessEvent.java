package com.bank.authservice.messaging.event;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class UserLoginSuccessEvent {

    private String username;
    private Instant timestamp;
}
