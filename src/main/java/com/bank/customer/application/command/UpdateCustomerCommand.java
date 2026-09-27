package com.bank.customer.application.command;

import com.bank.customer.domain.model.ContactInfo;
import com.bank.customer.domain.model.CustomerName;

public record UpdateCustomerCommand(CustomerName name, ContactInfo contact) {
}
