# Secuencia — Crear cliente (`POST /customers`)

Cubre validación, duplicado, guardado y publicación del evento, tal como está implementado en
`CreateCustomerUseCaseImpl` (R3) + `CustomerPersistenceAdapter` (R4) + `CustomerController` /
`GlobalExceptionHandler` (R5).

```mermaid
sequenceDiagram
    autonumber
    actor C as Cliente HTTP (TELLER/ADMIN)
    participant Ctrl as CustomerController
    participant UC as CreateCustomerUseCaseImpl
    participant Repo as CustomerRepositoryPort
    participant Mongo as CustomerPersistenceAdapter (Mongo)
    participant Dom as Customer (aggregate)
    participant Evt as CustomerEventPublisherPort
    participant EH as GlobalExceptionHandler

    C->>Ctrl: POST /api/v1/customers (CreateCustomerRequest)
    Note over Ctrl: Bean Validation del DTO (formato suelto: 6-12 alfanumérico)
    alt DTO inválido
        Ctrl-->>EH: WebExchangeBindException
        EH-->>C: 400 VALIDATION_ERROR
    end
    Ctrl->>UC: execute(CreateCustomerCommand)

    UC->>Repo: existsByDocument(type, number)
    Repo->>Mongo: existsByDocumentTypeAndDocumentNumber
    Mongo-->>Repo: boolean
    Repo-->>UC: Single<Boolean>

    alt Ya existe
        UC-->>Ctrl: error DuplicateDocumentException
        Ctrl-->>EH: DuplicateDocumentException
        EH-->>C: 409 DOCUMENT_ALREADY_REGISTERED
    else No existe
        UC->>Dom: Customer.create(type, profile, name, document, contact, clock)
        Note over Dom: Reglas 2-4: tipo-documento, tipo-perfil,<br/>formato del número por tipo
        alt Regla violada
            Dom-->>UC: throws InvalidCustomerException
            UC-->>Ctrl: error InvalidCustomerException
            Ctrl-->>EH: InvalidCustomerException
            EH-->>C: 422 (DOCUMENT_TYPE_NOT_ALLOWED / PROFILE_NOT_ALLOWED / INVALID_DOCUMENT)
        else Válido
            Dom-->>UC: Customer (ACTIVE, id nuevo)
            UC->>Repo: save(customer)
            Repo->>Mongo: insert
            Note over Mongo: Índice único (document.type, document.number)<br/>como segunda barrera ante una carrera
            alt Carrera: otro insert ganó primero
                Mongo-->>Repo: DuplicateKeyException
                Repo-->>UC: error DuplicateDocumentException
                UC-->>Ctrl: error DuplicateDocumentException
                Ctrl-->>EH: DuplicateDocumentException
                EH-->>C: 409 DOCUMENT_ALREADY_REGISTERED
            else Guardado OK
                Mongo-->>Repo: Customer guardado
                Repo-->>UC: Single<Customer>
                UC->>Evt: publish(CustomerCreated.from(saved))
                Note over Evt: No-op hasta P3 (Kafka real)
                Evt-->>UC: Completable complete
                UC-->>Ctrl: Customer
                Ctrl-->>C: 201 Created (CustomerDto)
            end
        end
    end
```

## Notas

- La verificación de duplicado en dos pasos (`existsByDocument` antes de `create`, e índice único
  de Mongo como respaldo) es intencional: la primera evita construir el aggregate innecesariamente
  en el caso normal; la segunda cubre la carrera entre dos peticiones concurrentes con el mismo
  documento, algo que una sola consulta previa no puede garantizar.
- `Customer.create(...)` es una llamada síncrona que puede lanzar una excepción de dominio; se
  envuelve en `Single.fromCallable(...)` para que ese error entre al canal reactivo de errores en
  vez de propagarse como una excepción no controlada antes de construir la cadena (ver R3).
- El publicador de eventos es un adaptador *no-op* hasta P3: hoy no hay Kafka real, pero el punto
  de extensión ya existe en el caso de uso.
