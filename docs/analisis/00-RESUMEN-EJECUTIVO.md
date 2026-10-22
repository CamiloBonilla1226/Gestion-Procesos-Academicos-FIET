# 00 - Resumen ejecutivo

Actualizado el 2026-10-21 (T12.40) contra el árbol de trabajo; las cifras entre paréntesis de la tabla son las del corte T12.37 salvo que se indique otra cosa. El análisis original es del 2026-10-14; las cifras de ese corte se conservan en los documentos detallados donde sirven de comparación.

## Qué es el sistema

Aplicación web de la FIET (Universidad del Cauca) para gestionar solicitudes académicas. [BASE] es el prototipo de Julián David Camacho Erazo: solicitudes dirigidas al Consejo de Facultad, con usuarios, roles, tipos de solicitud, orden del día, respuestas, historial, carga masiva por Excel y exportación .docx. [EXTENSIÓN] agrega tres procesos: Cancelación de Matrícula, Cancelación de Asignatura y Examen Supletorio, con los actores Estudiante, Funcionario Académico y Decano. La extensión también notifica la respuesta final por correo electrónico al estudiante, a DARCA y al profesor del supletorio, y en Cancelación de Asignatura comunica la Resolución en una fecha programada ([correo-electronico.md](correo-electronico.md)).

## Arquitectura

Backend Spring Boot 3.5.4 (Java 17) en arquitectura hexagonal: `aplicacion` (35 puertos de entrada y 38 de salida), `dominio` (35 casos de uso, 74 modelos, 9 servicios) e `infraestructura` (16 controladores REST, 29 gateways, 32 repositorios JPA, 36 mapeadores propios, seguridad JWT, correo con Spring Mail, PDF con OpenPDF y una tarea programada). Frontend Angular 20 con PrimeNG, organizado por perfil en `core`, más `layouts` y `shared`. Base MySQL 8.0.33 con Hibernate `ddl-auto=update`. Despliegue con Docker Compose (backend y MySQL) y un Dockerfile de frontend.

## Módulos

Usuarios y roles, sesiones, solicitudes y tipos de solicitud, orden del día, respuestas, historial [BASE]; estudiantes, funcionarios académicos, programas y asignaturas, catálogos académicos, solicitudes académicas, cancelación de matrícula, cancelación de asignatura, exámenes supletorios, notificaciones del docente, correo de la respuesta final y comunicación programada de la Resolución [EXTENSIÓN]. Máquina de etapas en `dominio/servicios/MaquinaEtapas.java`.

## Cifras clave

| Concepto | Total | [BASE] | [EXTENSIÓN] |
|---|---|---|---|
| Endpoints (grep de anotaciones de mapeo) | 140 (138; 128 al corte original) | 62 | 78 |
| Clases `.java` en main | 527 (517) | 175 | 352 |
| Entidades `@Entity` | 34 (32) | 13 | 21 |
| Tablas físicas | 35 (33) | 14 | 21 |
| Casos de uso | 35 (34) | 8 | 27 |
| Componentes Angular | 159 (157) | 87 | 72 |
| Rutas (36 con pantalla, más la raíz) | 37 (35) | 18 | 19 |
| Servicios HTTP del frontend | 18 (17) | 9 | 9 |
| Tests backend (`@Test` + `@ParameterizedTest`) | 909 en 52 archivos (889 en 51); 1604 ejecutadas y en verde en la corrida completa de T12.40 | 1 archivo | 51 archivos |
| Specs frontend | 152 archivos, 647 `it(` (149 y 629); 675 ejecutados en T12.40, 58 fallan (los `should create` de Julián con NG0201, conocidos) | 87 | 65 |
| Constantes de `MensajesError` | 137 (132) | 22 | 115 |


## División entre Julián y lo nuevo

El primer commit (`d3a48cc`) contiene el código de Julián. Al corte, la extensión solo había agregado líneas a cuatro de sus archivos (`ApplicationConstantes`, `BeanConfiguracion`, `ConfiguracionSeguridad`, `MensajesError`); hoy son once archivos de Julián con cambios, cuatro de ellos con líneas reemplazadas por la tarea T12.21 de seguridad, que lo pidió de forma explícita (documento 02). La extensión replica el estilo de puertos y casos de uso, pero sin comentarios, y agrega pruebas automatizadas que el prototipo casi no tenía.

## Los hallazgos más importantes

1. La monografía dice JWT de 5 horas; el código usa 24 horas (`JwtServicio.java:36`).
2. La clave de firma del JWT estaba escrita en el código fuente; desde T12.21 se lee de `JWT_SECRET` y desde T12.40 ya no tiene valor por defecto en ningún archivo versionado: sale solo de `backend/.env` y el arranque se detiene con un mensaje claro si falta (lo mismo con `CORREO_DARCA`). La clave anterior sigue en el historial de git.
3. No hay `@EnableMethodSecurity`: los `@PreAuthorize` no se evalúan; la seguridad real son las reglas por URL. Al corte 22 endpoints [BASE] diferían de su anotación; T12.21 los cubrió con reglas por URL y control de dueño.
4. Todos los errores de negocio devuelven HTTP 500, diferenciados solo por `codigoError`.
5. La monografía dice que solo hay Postman y UAT; existen 889 métodos de test en backend, 149 specs en frontend y 15 scripts de integración, entre ellos uno para el correo con un SMTP local.
6. El dominio depende de infraestructura (37 archivos, por `MensajesError`) y de Spring (4 archivos [BASE]); el correo, el PDF y la tarea programada entran por puertos y no agregan dependencias del dominio hacia Spring.
7. Los casos de uso no abren transacciones; lo hacen los controladores.
8. Faltan restricciones en base de datos (sin `ON DELETE`, sin CHECK) y las tablas de Julián no tienen DDL documentado.
9. La documentación se contradice: `etapas-por-proceso.md` tiene tablas y decisiones antiguas que las decisiones P50 a P62 reemplazan sin corregirlas (rechazo con Resolución, seis anexos, fecha acordada, DARCA fuera de la aplicación), además de P14 contra P46 y README contra el código. T12.40 corrigió esas tablas y las decisiones P9, P21, P44 y P58, el diccionario (`observacionEvaluacion` opcional) y el README.
10. Las ramas Prod y Dev de la monografía no existen en el repositorio; solo hay `main`.
11. Correo: un fallo de correo nunca revierte la respuesta, pero los correos inmediatos no tienen reintento y, si falla el correo al estudiante de la comunicación programada, DARCA recibe una constancia que dice que no se envió (punto abierto, [correo-electronico.md](correo-electronico.md)).
12. Zona horaria: el backend en Docker usaba UTC y uno local la hora de Colombia, con fechas corridas 5 horas. Desde T12.40 el compose fija `America/Bogota` en el backend, en MySQL y en la conexión; las filas anteriores conservan la hora con que se escribieron.

## Cambios de T12.38 a T12.40

- T12.38 (P63): en Cancelación de Asignatura después del Decano el mensaje inmediato es obligatorio, el Funcionario edita los textos que recibirán el estudiante y DARCA (`textoComunicacion`, `textoDarca`), la firma del estudiante cierra la comunicación y DARCA recibe la Resolución firmada de inmediato (ya no hay re-subida ni reprogramación después de la firma), y el estudiante ve la respuesta desde la firma o desde la fecha programada. Borrado de respuestas por rango en "Solicitudes respondidas" (dos endpoints nuevos, `EliminacionRespuestasCUImplAdaptador`). Detalle en [correo-electronico.md](correo-electronico.md) y en el documento 03 (filas 100a y 100b).
- T12.40: secretos sin valor por defecto, zona horaria fija, `t5_solicitudes.ps1` con el PDF único de paz y salvos, selector de hora del examen cruzado en el formulario del supletorio y documentación de reglas corregida. Tablas sin cambio (35: 14 [BASE] y 21 [EXTENSIÓN]); T12.38 solo agregó columnas a `NOTIFICACION_RESOLUCION`.
