-- Uso: con el cliente mysql en utf8mb4 sobre la base cfiet, asignar primero el
-- uuid del Funcionario Académico a cargo de los tres procesos y luego cargar
-- este archivo. Se puede correr más de una vez (INSERT IGNORE con UUID fijos).
--   SET @funcionario_uuid = '<Usuario_uuid de FUNCIONARIO_ACADEMICO>';
--   source docs/database/seed-procesos-academicos.sql
-- Con Docker, desde backend/ (PowerShell):
--   docker compose cp ../docs/database/seed-procesos-academicos.sql cfiet_database:/tmp/seed.sql
--   docker compose exec -T cfiet_database mysql -u root -pmysql --default-character-set=utf8mb4 cfiet -e "SET @funcionario_uuid='<uuid>'; source /tmp/seed.sql"

INSERT IGNORE INTO TIPO_SOLICITUD_ACADEMICA (uuidTipoSolicitudAcademica, nombre, descripcion, FuncionarioAcademico_uuid) VALUES
('3b6dccc9-ff78-48ef-8e66-05fb9932e302', 'Cancelación de Matrícula', 'Cancelación de todas las asignaturas matriculadas en el periodo académico', @funcionario_uuid),
('7b3d4347-3525-4ab7-a2b4-723b1ced80c8', 'Cancelación de Asignatura', 'Cancelación de una o más asignaturas matriculadas en el periodo académico', @funcionario_uuid),
('459cde47-f3a3-4a74-9b44-1063a516326c', 'Examen Supletorio', 'Solicitud de examen supletorio por un examen no presentado', @funcionario_uuid);

INSERT IGNORE INTO ETAPA_SOLICITUD_ACADEMICA (uuidEtapa, codigo, TipoSolicitudAcademica_uuid) VALUES
('653e4772-562b-4d3c-becf-85f19a6dbdc9', 'RADICADA', NULL),
('28ee2417-f100-4972-82d3-a4d267a1a10f', 'EN_REVISION_DECANO', NULL),
('1dfc0089-f1b0-486d-bfc5-2025081d800f', 'APROBADA_POR_DECANO', NULL),
('fa3c502f-dc81-46eb-bace-88deb194564b', 'RECHAZADA_POR_DECANO', NULL),
('6e6b1b7e-f7db-4fb8-8c4a-3d67c18dbe5a', 'APROBADA', NULL),
('475e51b3-f854-4b4e-818e-a792ef59f64c', 'RECHAZADA', NULL),
('6e4561a7-b354-4f21-9825-f35263882b36', 'PENDIENTE_PAGO', '459cde47-f3a3-4a74-9b44-1063a516326c'),
('ac289652-cf4e-4142-bec4-8809cbcb74fc', 'EN_VERIFICACION_PAGO', '459cde47-f3a3-4a74-9b44-1063a516326c');

INSERT IGNORE INTO ETAPA_ETIQUETA_ROL (Etapa_uuid, rol, etiqueta) VALUES
('653e4772-562b-4d3c-becf-85f19a6dbdc9', 'ESTUDIANTE', 'En trámite'),
('653e4772-562b-4d3c-becf-85f19a6dbdc9', 'FUNCIONARIO', 'Pendiente'),
('28ee2417-f100-4972-82d3-a4d267a1a10f', 'ESTUDIANTE', 'En trámite'),
('28ee2417-f100-4972-82d3-a4d267a1a10f', 'FUNCIONARIO', 'En Gestión'),
('28ee2417-f100-4972-82d3-a4d267a1a10f', 'DECANO', 'Pendiente'),
('1dfc0089-f1b0-486d-bfc5-2025081d800f', 'ESTUDIANTE', 'En trámite'),
('1dfc0089-f1b0-486d-bfc5-2025081d800f', 'FUNCIONARIO', 'Pendiente de Respuesta'),
('1dfc0089-f1b0-486d-bfc5-2025081d800f', 'DECANO', 'Respondida'),
('fa3c502f-dc81-46eb-bace-88deb194564b', 'ESTUDIANTE', 'En trámite'),
('fa3c502f-dc81-46eb-bace-88deb194564b', 'FUNCIONARIO', 'Pendiente de Respuesta'),
('fa3c502f-dc81-46eb-bace-88deb194564b', 'DECANO', 'Respondida'),
('6e4561a7-b354-4f21-9825-f35263882b36', 'ESTUDIANTE', 'Pendiente de pago'),
('6e4561a7-b354-4f21-9825-f35263882b36', 'FUNCIONARIO', 'En Gestión'),
('6e4561a7-b354-4f21-9825-f35263882b36', 'DECANO', 'Respondida'),
('ac289652-cf4e-4142-bec4-8809cbcb74fc', 'ESTUDIANTE', 'En verificación de pago'),
('ac289652-cf4e-4142-bec4-8809cbcb74fc', 'FUNCIONARIO', 'Pendiente de Verificación'),
('ac289652-cf4e-4142-bec4-8809cbcb74fc', 'DECANO', 'Respondida'),
('6e6b1b7e-f7db-4fb8-8c4a-3d67c18dbe5a', 'ESTUDIANTE', 'Aprobada'),
('6e6b1b7e-f7db-4fb8-8c4a-3d67c18dbe5a', 'FUNCIONARIO', 'Respondida'),
('6e6b1b7e-f7db-4fb8-8c4a-3d67c18dbe5a', 'DECANO', 'Respondida'),
('475e51b3-f854-4b4e-818e-a792ef59f64c', 'ESTUDIANTE', 'Rechazada'),
('475e51b3-f854-4b4e-818e-a792ef59f64c', 'FUNCIONARIO', 'Respondida'),
('475e51b3-f854-4b4e-818e-a792ef59f64c', 'DECANO', 'Respondida');

INSERT IGNORE INTO TIPO_ANEXO_ACADEMICO (uuidTipoAnexoAcademico, TipoSolicitudAcademica_uuid, nombre, formatosPermitidos, obligatorio) VALUES
('0870fd29-220d-4226-92b1-590804ac0974', '3b6dccc9-ff78-48ef-8e66-05fb9932e302', 'Paz y salvo - División de Bibliotecas', 'pdf', 1),
('fc630c85-6dff-4def-a072-ea26bd942d6e', '3b6dccc9-ff78-48ef-8e66-05fb9932e302', 'Paz y salvo - División de Deportes y Recreación', 'pdf', 1),
('7b26091a-d12e-48d2-85b1-10a578ef198e', '3b6dccc9-ff78-48ef-8e66-05fb9932e302', 'Paz y salvo - División de Salud Integral', 'pdf', 1),
('6898eb98-f150-4784-8255-0eab0eb61edd', '3b6dccc9-ff78-48ef-8e66-05fb9932e302', 'Cupón de Confirmación de la Intervención Psicosocial - División de Salud Integral', 'pdf', 1),
('c756365a-f0c4-4e9a-9326-2946aeacd638', '3b6dccc9-ff78-48ef-8e66-05fb9932e302', 'Paz y salvo - División Financiera', 'pdf', 1),
('22ca9f7d-967d-44ba-87fa-103aa63b297f', '3b6dccc9-ff78-48ef-8e66-05fb9932e302', 'Carné estudiantil o constancia de no trámite - DARCA', 'pdf,jpg,jpeg,png', 1),
('ee145c67-0f6e-4971-84b7-42679747ed49', '459cde47-f3a3-4a74-9b44-1063a516326c', 'Formato PM-FO-4-FOR-23 firmado por el docente que orienta la asignatura', 'pdf,jpg,png', 1),
('2bfc61be-7787-4772-8b86-ca64488f2578', '459cde47-f3a3-4a74-9b44-1063a516326c', 'Soporte de la justificación de la no presentación', 'pdf,jpg,png', 0),
('3cba6336-4de6-492b-838d-602f28a9efbe', '459cde47-f3a3-4a74-9b44-1063a516326c', 'Formato firmado por el docente de la asignatura con la que se cruza', 'pdf,jpg,png', 0),
('422163e2-81b8-48f8-a054-2c40033de17c', '459cde47-f3a3-4a74-9b44-1063a516326c', 'Recibo de pago', 'pdf', 0),
('8408772b-8b61-472e-ac44-a83923dea147', '459cde47-f3a3-4a74-9b44-1063a516326c', 'Comprobante de pago', 'pdf,jpg,png', 0);

INSERT IGNORE INTO SITUACION_ACADEMICA_ASIGNATURA (uuidSituacionAcademica, codigo, nombre) VALUES
('6eec86b1-53da-4c65-baa9-96bf67669c13', 'R0', 'Cursada por primera vez'),
('62aa0705-f878-408e-8111-78691c1a871a', 'R1', 'Cursada por segunda vez'),
('fa14cee3-6e4f-4178-955c-ba73312e08cb', 'R2', 'Cursada por tercera vez'),
('a6077d43-d5fc-4e8d-9424-18c549515c24', 'R3', 'Cursada por cuarta vez');
