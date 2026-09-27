# UML de dominio — `customer-service`

Aggregate `Customer` (Java record, inmutable) con sus value objects y enums. Todo en
`domain/model`, sin dependencias de Spring.

```mermaid
classDiagram
    class Customer {
        <<aggregate root, record>>
        +CustomerId id
        +CustomerType type
        +CustomerProfile profile
        +CustomerName name
        +Document document
        +ContactInfo contact
        +CustomerStatus status
        +Instant createdAt
        +Instant updatedAt
        +create(type, profile, name, document, contact, clock)$ Customer
        +update(newName, newContact, clock) Customer
        +changeProfile(newProfile, clock) Customer
        +deactivate(clock) Customer
    }

    class CustomerId {
        <<value object, record>>
        +String value
        +newId()$ CustomerId
    }

    class CustomerName {
        <<value object, record>>
        +String value
        max 150 caracteres
    }

    class Document {
        <<value object, record>>
        +DocumentType type
        +String number
        formato por tipo: DNI 8 dígitos,
        CEX 9-12 alfanumérico, PASSPORT 6-12,
        RUC 11 dígitos
    }

    class ContactInfo {
        <<value object, record>>
        +Email email
        +PhoneNumber phone
        +String address
        address opcional, max 200
    }

    class Email {
        <<value object, record>>
        +String value
        normalizado a minúsculas
    }

    class PhoneNumber {
        <<value object, record>>
        +String value
        formato 9 dígitos, empieza en 9
    }

    class CustomerType {
        <<enumeration>>
        PERSONAL
        BUSINESS
    }

    class CustomerProfile {
        <<enumeration>>
        STANDARD
        VIP
        PYME
    }

    class CustomerStatus {
        <<enumeration>>
        ACTIVE
        INACTIVE
    }

    class DocumentType {
        <<enumeration>>
        DNI
        CEX
        PASSPORT
        RUC
    }

    Customer "1" *-- "1" CustomerId
    Customer "1" *-- "1" CustomerName
    Customer "1" *-- "1" Document
    Customer "1" *-- "1" ContactInfo
    Customer --> CustomerType
    Customer --> CustomerProfile
    Customer --> CustomerStatus
    Document --> DocumentType
    ContactInfo "1" *-- "1" Email
    ContactInfo "1" *-- "1" PhoneNumber
```

## Invariantes que vive el aggregate (no el mapper ni el controller)

| # | Regla | Dónde se aplica |
|---|---|---|
| 2 | El tipo de documento debe ser coherente con el tipo de cliente (`PERSONAL` → DNI/CEX/PASSPORT, `BUSINESS` → RUC) | `Customer.create` → `InvalidCustomerException("DOCUMENT_TYPE_NOT_ALLOWED")` |
| 3 | El perfil debe ser compatible con el tipo (`VIP` solo `PERSONAL`, `PYME` solo `BUSINESS`, `STANDARD` ambos) | `Customer.create` y `Customer.changeProfile` → `InvalidCustomerException("PROFILE_NOT_ALLOWED")` |
| 4 | El número de documento debe cumplir el formato de su tipo | Constructor compacto de `Document` → `InvalidCustomerException("INVALID_DOCUMENT")` |
| 5 | Un cliente `INACTIVE` no admite `update` ni `changeProfile` | `Customer.requireActive()` → `InvalidCustomerException("CUSTOMER_INACTIVE")` |
| 7 | La baja es lógica e idempotente: repetirla sobre un `INACTIVE` no cambia nada ni vuelve a publicar el evento | `Customer.deactivate` + `DeleteCustomerUseCaseImpl` |

La unicidad del documento (regla 1) **no** vive en el aggregate: se resuelve en el caso de uso
(`existsByDocument`) y se refuerza con el índice único de Mongo — ver el diagrama de secuencia
de creación.
