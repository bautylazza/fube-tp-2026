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


### Changed


### Fixed
- Execution permission to gradlew
- (T1.2)rename findByClienteID to findByClienteId

### Removed


## [0.1.0] - 02-09-2026

### Added
- Initial skeleton