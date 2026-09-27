package com.bank.customer.domain.event;

/** Document data as it looked at the moment the event was raised. */
public record DocumentSnapshot(String type, String number) {
}
