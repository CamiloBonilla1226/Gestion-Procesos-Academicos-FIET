# 00 - Resumen ejecutivo

## Qué es el sistema

Aplicación web de la FIET (Universidad del Cauca) para gestionar solicitudes académicas. [BASE] es el prototipo de Julián David Camacho Erazo: solicitudes dirigidas al Consejo de Facultad, con usuarios, roles, tipos de solicitud, orden del día, respuestas, historial, carga masiva por Excel y exportación .docx. [EXTENSIÓN] agrega tres procesos: Cancelación de Matrícula, Cancelación de Asignatura y Examen Supletorio, con los actores Estudiante, Funcionario Académico y Decano.

## Arquitectura

Backend Spring Boot 3.5.4 (Java 17) en arquitectura hexagonal: `aplicacion` (30 puertos de entrada y 30 de salida), `dominio` (30 casos de uso, 58 modelos, 7 servicios) e `infraestructura` (15 controladores REST, 25 gateways, 29 repositorios JPA, 32 mapeadores propios, seguridad JWT). Frontend Angular 20 con PrimeNG, organizado por perfil en `core`, más `layouts` y `shared`. Base MySQL 8.0.33 con Hibernate `ddl-auto=update`. Despliegue con Docker Compose (backend y MySQL) y un Dockerfile de frontend.

## Módulos

Usuarios y roles, sesiones, solicitudes y tipos de solicitud, orden del día, respuestas, historial [BASE]; estudiantes, funcionarios académicos, programas y asignaturas, catálogos académicos, solicitudes académicas, cancelación de matrícula, cancelación de asignatura y exámenes supletorios [EXTENSIÓN]. Máquina de etapas en `dominio/servicios/MaquinaEtapas.java`.

## Cifras clave

| Concepto | Total | [BASE] | [EXTENSIÓN] |
|---|---|---|---|
| Endpoints (grep de anotaciones de mapeo) | 128 | 62 | 66 |
| Clases `.java` en main | 459 | 175 | 284 |
| Entidades `@Entity` | 32 | 13 | 19 |
| Tablas físicas | 33 | 14 | 19 |
| Casos de uso / operaciones | 30 / 138 | 8 / 62 | 22 / 76 |
| Componentes Angular | 145 | 87 | 58 |
| Rutas (34 con pantalla, más la raíz) | 35 | 18 | 17 |
| Servicios HTTP del frontend | 17 | 9 | 8 |
| Tests backend (`@Test` + `@ParameterizedTest`) | 696 en 38 archivos | 1 archivo | 37 archivos |
| Specs frontend | 136 (524 `it(`) | 87 | 49 |
| Constantes de `MensajesError` | 115 | 22 | 93 |


## División entre Julián y lo nuevo

El primer commit (`d3a48cc`) contiene el código de Julián; la extensión solo agregó líneas a cuatro de sus archivos (`ApplicationConstantes`, `BeanConfiguracion`, `ConfiguracionSeguridad`, `MensajesError`). La extensión replica el estilo de puertos y casos de uso, pero sin comentarios, y agrega pruebas automatizadas que el prototipo casi no tenía.

## Los 10 hallazgos más importantes

1. La monografía dice JWT de 5 horas; el código usa 24 horas (`JwtServicio.java:36`).
2. La clave de firma del JWT está escrita en el código fuente.
3. No hay `@EnableMethodSecurity`: los `@PreAuthorize` no se evalúan; la seguridad real son las reglas por URL, y 22 endpoints [BASE] difieren de su anotación.
4. Todos los errores de negocio devuelven HTTP 500, diferenciados solo por `codigoError`.
5. La monografía dice que solo hay Postman y UAT; existen 696 métodos de test en backend y 136 specs en frontend.
6. El dominio depende de infraestructura (33 archivos) y de Spring (4 archivos [BASE]).
7. Los casos de uso no abren transacciones; lo hacen los controladores.
8. Faltan restricciones en base de datos (sin `ON DELETE`, sin CHECK) y las tablas de Julián no tienen DDL documentado.
9. La documentación se contradice (`etapas-por-proceso.md` P14 contra P46, README contra el código, 18 contra 19 tablas, 1233 contra 696 pruebas).
10. Las ramas Prod y Dev de la monografía no existen en el repositorio; solo hay `main`.
