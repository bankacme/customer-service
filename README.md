# customer-service

Dueño del maestro de clientes del banco (personas y empresas). Ficha completa:
`bank-docs/services/customer-service.md`. Contrato: `bank-docs/contracts/customer-service/`.

Construido a partir de `bank-service-template`, siguiendo la receta R1–R10 de
`bank-docs/implementation-plan.md` (sección 3).

## Estado (receta R1–R10)
- [x] R1. Esqueleto: contrato copiado, genera interfaces/DTOs, arranca vacío
- [ ] R2. Dominio
- [ ] R3. Casos de uso y puertos
- [ ] R4. Persistencia
- [ ] R5. Adaptadores de entrada
- [ ] R6. Configuración y arranque real
- [ ] R7. No aplica (sin clientes salientes en P1)
- [ ] R8. Calidad (Checkstyle/Jacoco)
- [ ] R9. Postman
- [ ] R10. Cierre (README, diagramas, etiqueta)

## Comandos
- Compilar, estilo, tests y cobertura: `.\mvnw verify` (reporte en `target/site/jacoco/index.html`)
- Arrancar (necesita `config-server` arriba): `.\mvnw spring-boot:run`

## Resincronizar el contrato
Si `bank-docs/contracts/customer-service/openapi.yaml` cambia:
```powershell
.\copy-contracts.ps1 -ServiceName customer-service
```
Asume que `bank-docs` es una carpeta hermana (`C:\dev\bankacme\bank-docs`); si no,
pasa `-DocsRepo <ruta>`.
