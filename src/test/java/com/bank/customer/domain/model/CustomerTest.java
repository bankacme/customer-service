package com.bank.customer.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bank.customer.domain.exception.InvalidCustomerException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class CustomerTest {

    private final Clock clock = Clock.fixed(Instant.parse("2026-09-25T10:00:00Z"), ZoneOffset.UTC);
    private final CustomerName name = new CustomerName("Ana Torres");
    private final ContactInfo contact = new ContactInfo(
            new Email("ana@bank.com"), new PhoneNumber("987654321"), null);

    // --- create: happy paths -------------------------------------------------

    @Test
    void createsAPersonalCustomerWithDniAsStandardByDefault() {
        Customer customer = Customer.create(CustomerType.PERSONAL, null, name,
                new Document(DocumentType.DNI, "12345678"), contact, clock);

        assertThat(customer.type()).isEqualTo(CustomerType.PERSONAL);
        assertThat(customer.profile()).isEqualTo(CustomerProfile.STANDARD);
        assertThat(customer.status()).isEqualTo(CustomerStatus.ACTIVE);
        assertThat(customer.createdAt()).isEqualTo(clock.instant());
        assertThat(customer.updatedAt()).isEqualTo(clock.instant());
        assertThat(customer.id()).isNotNull();
    }

    @Test
    void createsAPersonalVipCustomer() {
        Customer customer = Customer.create(CustomerType.PERSONAL, CustomerProfile.VIP, name,
                new Document(DocumentType.CEX, "ABC123456"), contact, clock);

        assertThat(customer.profile()).isEqualTo(CustomerProfile.VIP);
    }

    @Test
    void createsABusinessPymeCustomerWithRuc() {
        Customer customer = Customer.create(CustomerType.BUSINESS, CustomerProfile.PYME, name,
                new Document(DocumentType.RUC, "20123456789"), contact, clock);

        assertThat(customer.type()).isEqualTo(CustomerType.BUSINESS);
        assertThat(customer.profile()).isEqualTo(CustomerProfile.PYME);
    }

    // --- create: Regla 2, coherencia tipo documento / tipo cliente -----------

    @Test
    void rejectsRucForAPersonalCustomer() {
        assertThatThrownBy(() -> Customer.create(CustomerType.PERSONAL, null, name,
                new Document(DocumentType.RUC, "20123456789"), contact, clock))
                .isInstanceOf(InvalidCustomerException.class)
                .satisfies(ex -> assertThat(((InvalidCustomerException) ex).getErrorCode())
                        .isEqualTo("DOCUMENT_TYPE_NOT_ALLOWED"));
    }

    @Test
    void rejectsDniForABusinessCustomer() {
        assertThatThrownBy(() -> Customer.create(CustomerType.BUSINESS, null, name,
                new Document(DocumentType.DNI, "12345678"), contact, clock))
                .isInstanceOf(InvalidCustomerException.class)
                .satisfies(ex -> assertThat(((InvalidCustomerException) ex).getErrorCode())
                        .isEqualTo("DOCUMENT_TYPE_NOT_ALLOWED"));
    }

    // --- create: Regla 3, coherencia perfil / tipo cliente --------------------

    @Test
    void rejectsVipForABusinessCustomer() {
        assertThatThrownBy(() -> Customer.create(CustomerType.BUSINESS, CustomerProfile.VIP, name,
                new Document(DocumentType.RUC, "20123456789"), contact, clock))
                .isInstanceOf(InvalidCustomerException.class)
                .satisfies(ex -> assertThat(((InvalidCustomerException) ex).getErrorCode())
                        .isEqualTo("PROFILE_NOT_ALLOWED"));
    }

    @Test
    void rejectsPymeForAPersonalCustomer() {
        assertThatThrownBy(() -> Customer.create(CustomerType.PERSONAL, CustomerProfile.PYME, name,
                new Document(DocumentType.DNI, "12345678"), contact, clock))
                .isInstanceOf(InvalidCustomerException.class)
                .satisfies(ex -> assertThat(((InvalidCustomerException) ex).getErrorCode())
                        .isEqualTo("PROFILE_NOT_ALLOWED"));
    }

    // --- update ---------------------------------------------------------------

    @Test
    void updateReplacesNameAndContactAndBumpsUpdatedAt() {
        Customer customer = activePersonalCustomer();
        Clock later = Clock.fixed(clock.instant().plusSeconds(60), ZoneOffset.UTC);
        CustomerName newName = new CustomerName("Ana Torres Lopez");

        Customer updated = customer.update(newName, contact, later);

        assertThat(updated.name()).isEqualTo(newName);
        assertThat(updated.createdAt()).isEqualTo(customer.createdAt());
        assertThat(updated.updatedAt()).isEqualTo(later.instant());
        // Immutability: the original instance must not have changed.
        assertThat(customer.name()).isEqualTo(name);
    }

    @Test
    void updateOnAnInactiveCustomerIsRejected() {
        Customer inactive = activePersonalCustomer().deactivate(clock);

        assertThatThrownBy(() -> inactive.update(name, contact, clock))
                .isInstanceOf(InvalidCustomerException.class)
                .satisfies(ex -> assertThat(((InvalidCustomerException) ex).getErrorCode())
                        .isEqualTo("CUSTOMER_INACTIVE"));
    }

    // --- changeProfile ----------------------------------------------------------

    @Test
    void changeProfileFromStandardToVipOnAPersonalCustomer() {
        Customer customer = activePersonalCustomer();

        Customer changed = customer.changeProfile(CustomerProfile.VIP, clock);

        assertThat(changed.profile()).isEqualTo(CustomerProfile.VIP);
    }

    @Test
    void changeProfileRevalidatesTypeCompatibility() {
        Customer customer = activePersonalCustomer();

        assertThatThrownBy(() -> customer.changeProfile(CustomerProfile.PYME, clock))
                .isInstanceOf(InvalidCustomerException.class)
                .satisfies(ex -> assertThat(((InvalidCustomerException) ex).getErrorCode())
                        .isEqualTo("PROFILE_NOT_ALLOWED"));
    }

    @Test
    void changeProfileOnAnInactiveCustomerIsRejected() {
        Customer inactive = activePersonalCustomer().deactivate(clock);

        assertThatThrownBy(() -> inactive.changeProfile(CustomerProfile.VIP, clock))
                .isInstanceOf(InvalidCustomerException.class)
                .satisfies(ex -> assertThat(((InvalidCustomerException) ex).getErrorCode())
                        .isEqualTo("CUSTOMER_INACTIVE"));
    }

    // --- deactivate: baja lógica idempotente -----------------------------------

    @Test
    void deactivateTurnsAnActiveCustomerIntoInactive() {
        Customer customer = activePersonalCustomer();

        Customer deactivated = customer.deactivate(clock);

        assertThat(deactivated.status()).isEqualTo(CustomerStatus.INACTIVE);
    }

    @Test
    void deactivatingAnAlreadyInactiveCustomerIsANoOpNotAnError() {
        Customer inactive = activePersonalCustomer().deactivate(clock);
        Clock later = Clock.fixed(clock.instant().plusSeconds(60), ZoneOffset.UTC);

        Customer result = inactive.deactivate(later);

        assertThat(result).isEqualTo(inactive);
        assertThat(result.updatedAt()).isEqualTo(inactive.updatedAt());
    }

    private Customer activePersonalCustomer() {
        return Customer.create(CustomerType.PERSONAL, null, name,
                new Document(DocumentType.DNI, "12345678"), contact, clock);
    }
}
