# Skill — ms-report-service

> Contrato: `ms-report-service-openapi.yaml` (fuente de verdad).
> Reglas transversales: ver `common-rules.md`.
> **Reactividad**: Spring WebFlux + Reactor (`Mono`/`Flux`). No usa RxJava.

## Responsabilidad
- Solicitud asíncrona de reportes (`202`) y consulta de su resultado.
- **No es CRUD tradicional**.
- Reporte general por producto en rango de fechas.
- Reporte consolidado de últimos 10 movimientos de tarjetas.

## Endpoints
| Método | Ruta | operationId |
|---|---|---|
| POST | `/api/v1/reports/products` | `generateProductReport` |
| POST | `/api/v1/reports/cards/last-movements` | `generateCardMovementsReport` |
| GET | `/api/v1/reports/{reportId}` | `findReportById` |

## Headers
- GET: `X-Correlation-ID`, `X-Channel-ID`.
- POST: `X-Correlation-ID`, `X-Channel-ID`, `X-Transaction-ID` (UUID).

## Persistencia
- Base: `banking_report`.
- Colecciones: `reports`, `movement_projections`.
- `movement_projections` se alimenta **solo por eventos**; nunca por lectura directa de bases ajenas.
- Escritura directa a MongoDB propia vía `ReactiveMongoRepository`.

## Reglas de negocio
- `POST /products`:
  - `fromDate <= toDate`. Si no → 422.
  - Rango máximo 90 días. Si excede → 422.
  - Idempotente por `X-Transaction-ID`.
  - Devuelve 202 con `operationId` y `reportId`.
- `POST /cards/last-movements`:
  - Idempotente por `X-Transaction-ID`.
  - `cardType` opcional (`CREDIT`, `DEBIT`, `BOTH`).
  - Devuelve 202 con `operationId` y `reportId`.
- `GET /reports/{reportId}`:
  - Devuelve `PENDING`, `PROCESSING`, `COMPLETED`, `FAILED`.
  - Si `COMPLETED`, incluye `downloadUrl`.
  - Si `FAILED`, incluye `errorMessage`.

### Obtención de datos sin acceso cruzado a bases
- `ms-report-service` **no** lee MongoDB de otros servicios.
- **Parte II**: consume las APIs públicas de cada servicio (Resilience4j, timeout 2 s).
- **Parte III**: consume eventos de movimiento para construir `movement_projections`:
  - `banking.account.events.v1` → `AccountMovementRegistered`.
  - `banking.credit.events.v1` → `CreditPaymentApplied`.
  - `banking.card.events.v1` → `CardMovementRegistered`.
  - `banking.wallet.events.v1` → `WalletMovementRegistered`.
- **Estado de los contratos**: estos 4 eventos son **propuestas** y deben formalizarse en el catálogo Kafka oficial antes de consumirse. Hasta entonces:
  - Parte II opera con APIs REST.
  - Parte III no consume estos eventos hasta aprobación.
- Envelope obligatorio; deduplicación por `eventId`; orden por `aggregateId`.
- Frescura eventual: documentar lag aceptable.

## Eventos (Parte III)
- Tópico: `banking.report.events.v1`.
- Productor: `ms-report-service`.
- Eventos: `ReportRequested`, `ReportCompleted`, `ReportFailed`.
- **Estado del contrato**: payloads a formalizar en catálogo Kafka antes de implementar.

## Pruebas mínimas
- `fromDate > toDate` → 422.
- Rango > 90 días → 422.
- Mismo día → aceptado.
- Rango exacto 90 días → aceptado.
- `X-Transaction-ID` duplicado → mismo `reportId`.
- `GET` con `reportId` inexistente → 404.
- Reporte alimentado por eventos sin acceso directo a bases ajenas.
- Deduplicación por `eventId` en `movement_projections`.
- Controladores reactivos con Reactor.

## Criterios de aceptación
1. Endpoints y DTOs idénticos al YAML.
2. Persistencia solo en `banking_report`.
3. Procesamiento asíncrono respeta el contrato.
4. Validaciones de fechas cubiertas por tests.
5. No se accede a MongoDB de otros servicios.
6. Movimientos por eventos (Parte III) o API (Parte II).
7. Eventos consumidos y publicados formalizados en catálogo Kafka antes de implementarse.