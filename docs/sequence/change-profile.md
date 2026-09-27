# Secuencia — Cambiar perfil (`PATCH /customers/{id}/profile`)

Implementado en `ChangeCustomerProfileUseCaseImpl` (R3) + `CustomerController` /
`GlobalExceptionHandler` (R5). No hay caso de uso separado para consultar: reutiliza
`findById` del puerto de salida.

```mermaid
sequenceDiagram
    autonumber
    actor C as Cliente HTTP (TELLER/ADMIN)
    participant Ctrl as CustomerController
    participant UC as ChangeCustomerProfileUseCaseImpl
    participant Repo as CustomerRepositoryPort
    participant Dom as Customer (aggregate)
    participant Cache as CustomerCachePort
    participant Evt as CustomerEventPublisherPort
    participant EH as GlobalExceptionHandler

    C->>Ctrl: PATCH /api/v1/customers/{id}/profile (ChangeProfileRequest)
    Ctrl->>UC: execute(CustomerId, CustomerProfile)

    UC->>Repo: findById(id)
    alt No existe
        Repo-->>UC: Maybe vacío
        UC-->>Ctrl: error CustomerNotFoundException
        Ctrl-->>EH: CustomerNotFoundException
        EH-->>C: 404 CUSTOMER_NOT_FOUND
    else Existe
        Repo-->>UC: Customer actual
        UC->>Dom: customer.changeProfile(newProfile, clock)
        Note over Dom: Regla 5: requireActive()<br/>Regla 3: requireProfileCompatibleWithType()
        alt INACTIVE
            Dom-->>UC: throws InvalidCustomerException
            UC-->>Ctrl: error InvalidCustomerException
            Ctrl-->>EH: InvalidCustomerException
            EH-->>C: 422 CUSTOMER_INACTIVE
        else Perfil incompatible con el tipo
            Dom-->>UC: throws InvalidCustomerException
            UC-->>Ctrl: error InvalidCustomerException
            Ctrl-->>EH: InvalidCustomerException
            EH-->>C: 422 PROFILE_NOT_ALLOWED
        else Válido
            Dom-->>UC: Customer con el nuevo perfil
            UC->>Repo: save(customer)
            Repo-->>UC: Customer guardado
            UC->>Cache: put(saved)
            Note over Cache: No-op hasta P3 (Redis real)
            Cache-->>UC: Completable complete
            UC->>Evt: publish(CustomerUpdated.from(saved))
            Note over Evt: No-op hasta P3 (Kafka real)
            Evt-->>UC: Completable complete
            UC-->>Ctrl: Customer
            Ctrl-->>C: 200 OK (CustomerDto)
        end
    end
```

## Notas

- El documento y el tipo **nunca** cambian aquí: solo `profile` y `updatedAt`. `update` (nombre y
  contacto) es un caso de uso distinto (`UpdateCustomerUseCaseImpl`) con la misma forma de guarda
  (`requireActive`) pero sin la validación de perfil.
- El orden de las dos validaciones dentro de `Customer.changeProfile` es `requireActive()` primero;
  por eso un cliente `INACTIVE` con un perfil también incompatible responde `CUSTOMER_INACTIVE`,
  no `PROFILE_NOT_ALLOWED`.
