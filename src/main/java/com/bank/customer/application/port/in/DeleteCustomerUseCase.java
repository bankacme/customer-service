package com.bank.customer.application.port.in;

import com.bank.customer.domain.model.CustomerId;
import io.reactivex.rxjava3.core.Completable;

public interface DeleteCustomerUseCase {

    Completable execute(CustomerId id);
}
