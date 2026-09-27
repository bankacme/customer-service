package com.bank.customer.application.port.in;

import com.bank.customer.domain.model.CustomerProfile;
import com.bank.customer.domain.model.CustomerStatus;
import com.bank.customer.domain.model.CustomerType;

/** Every field is optional; {@code null} means "don't filter by this". */
public record CustomerFilter(CustomerType type, CustomerProfile profile, CustomerStatus status) {

    public static CustomerFilter none() {
        return new CustomerFilter(null, null, null);
    }
}
