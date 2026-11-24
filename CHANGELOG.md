# CHANGELOG

Los cambios relevantes de este proyecto se documentan en este archivo.

El formato está basado en [Keep a Changelog](https://keepachangelog.com/es-ES/1.1.0/)
y el proyecto usa [Versionado Semántico](https://semver.org/lang/es/).

## [Unreleased]

### Added
- (T1.2):JPA entities for the three services
- (T1.2):JPA repositories
- (T1.1): Initial product Seed
- (T1.3): Error handling with ProblemDetail in servicio-clientes
- (T1.4): POST /api/v1/clientes and GET /api/v1/clientes/{id}
- (T1.6): OpenAPI tag and DTO examples for servicio-clientes
- (T1.5): GET /api/v1/productos/{id} and /disponibilidad in servicio-inventario
- (T2.1): POST /api/v1/reservas in servicio-inventario
- (T2.2): PATCH /api/v1/reservas/{id} in servicio-inventario
- (A4): PATCH /api/v1/reservas?pedidoId= in servicio-inventario
- (T1.3): Error handling with ProblemDetail in servicio-pedidos
- (T2.4): HTTP client from servicio-pedidos to servicio-clientes
- (T2.5): HTTP client from servicio-pedidos to servicio-inventario
- (T3.1): POST /api/v1/pedidos in servicio-pedidos
- (T3.2): GET /api/v1/pedidos/{id} and GET /api/v1/pedidos?clienteId= in servicio-pedidos
- (T3.3): PATCH /api/v1/pedidos/{id} in servicio-pedidos
- (T4.3): Spring Batch metadata tables migration in servicio-pedidos


### Changed


### Fixed
- Execution permission to gradlew
- (T1.2)rename findByClienteID to findByClienteId

### Removed


## [0.1.0] - 02-09-2026

### Added
- Initial skeleton