package com.bank.authservice.messaging.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor
public class UserLogoutEvent {

    private Instant timestamp;
}
