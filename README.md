# customer-service

Dueño del maestro de clientes del banco (personas y empresas). Ficha completa:
`bank-docs/services/customer-service.md`. Contrato: `bank-docs/contracts/customer-service/`.

## Qué hace

Alta, consulta, actualización, cambio de perfil y baja lógica de clientes personales
(DNI/CEX/Pasaporte) y empresas (RUC). Perfil comercial `STANDARD`/`VIP`/`PYME`, con `VIP`
exclusivo de personas y `PYME` exclusivo de empresas. El documento y el tipo no cambian
después de creado el cliente. Eliminar es una baja lógica (`status = INACTIVE`), idempotente.

## Arquitectura

Hexagonal/DDD en 3 capas, con Spring solo en `infrastructure`:

- **`domain`** — `Customer` (aggregate root, record inmutable), value objects (`CustomerId`,
  `CustomerName`, `Document`, `Email`, `PhoneNumber`, `ContactInfo`), enums y las excepciones de
  negocio. Sin ninguna dependencia de Spring; probado con JUnit 5 puro.
- **`application`** — 7 casos de uso (uno por operación del contrato) contra puertos de entrada
  (`*UseCase`) y de salida (`CustomerRepositoryPort`, `CustomerEventPublisherPort`,
  `CustomerCachePort`), usando tipos RxJava 3 (`Single`/`Maybe`/`Completable`/`Flowable`).
  Tampoco depende de Spring: los 7 `*Impl` se cablean como `@Bean` desde
  `infrastructure/config/UseCaseConfig`.
- **`infrastructure`** — adaptador REST (`CustomerController`, implementa la interfaz generada
  del contrato), adaptador Mongo reactivo, mappers a mano (REST↔dominio, dominio↔documento) y los
  adaptadores *no-op* de Kafka/Redis que se reemplazan en P3.

Diagramas: `docs/uml/customer-domain.md` (modelo de dominio) y `docs/sequence/` (crear cliente,
cambiar perfil).

`RxJavaReactorBridge` (`infrastructure/support`) adapta entre RxJava 3 (puertos y casos de uso) y
Project Reactor (lo que exige WebFlux/el generador OpenAPI) solo en el borde REST.

## API

Ver el contrato completo en `bank-docs/contracts/customer-service/openapi.yaml`. Resumen:

| Método | Ruta | Qué hace |
|---|---|---|
| `POST` | `/api/v1/customers` | Crear un cliente |
| `GET` | `/api/v1/customers` | Listar (filtros opcionales: `type`, `profile`, `status`) |
| `GET` | `/api/v1/customers/by-document` | Buscar por `documentType` + `documentNumber` |
| `GET` | `/api/v1/customers/{id}` | Obtener por id |
| `PUT` | `/api/v1/customers/{id}` | Actualizar nombre y contacto |
| `PATCH` | `/api/v1/customers/{id}/profile` | Cambiar el perfil comercial |
| `DELETE` | `/api/v1/customers/{id}` | Baja lógica (idempotente) |

El prefijo `/api/v1` lo agrega `WebConfig` a todos los `@RestController`, porque el generador
OpenAPI no lo incluye en las interfaces generadas.

## Comandos
- Compilar, estilo, tests y cobertura: `.\mvnw verify` (reporte en `target/site/jacoco/index.html`)
- Solo tests: `.\mvnw test`
- Arrancar (necesita `config-server` arriba, puerto `8081`): `.\mvnw spring-boot:run`

## Probar con Postman

Colección `bankacme.postman_collection.json`, carpeta `customer-service` (19 pasos: los del
guion de demo que ya funcionan en P1 — sección 7.A de `bootcamp-bank-microservices-definition.md`
— más el resto del contrato y sus casos negativos). Variable `baseUrl` apunta a
`http://localhost:8081/api/v1`; ajustarla si el Config Server asigna otro puerto. Con el servicio
y Mongo arriba, correr la carpeta completa con el Collection Runner.

## Pruebas

| Capa | Qué cubre | Herramientas |
|---|---|---|
| Dominio | Value objects (formatos válidos/inválidos), reglas 2–5 y 7 del aggregate | JUnit 5 |
| Casos de uso | Los 7 casos de uso con puertos en memoria: duplicado, no encontrado, perfil incompatible, cliente inactivo | JUnit 5 + RxJava `TestObserver` |
| Adaptador Mongo | Guardar, buscar, índice único, filtros combinados | `@SpringBootTest` contra Mongo local |
| Controller | Contrato, códigos de estado, cuerpo de error | `@WebFluxTest` + `WebTestClient` |
| Cobertura | Todo el código | Jacoco (95%) |

## Resincronizar el contrato
Si `bank-docs/contracts/customer-service/openapi.yaml` cambia:
```powershell
.\copy-contracts.ps1 -ServiceName customer-service
```
Asume que `bank-docs` es una carpeta hermana (`C:\dev\bankacme\bank-docs`); si no,
pasa `-DocsRepo <ruta>`.
