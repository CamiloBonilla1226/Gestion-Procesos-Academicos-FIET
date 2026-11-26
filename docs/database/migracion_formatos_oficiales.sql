SET NAMES utf8mb4;

INSERT IGNORE INTO ETAPA_SOLICITUD_ACADEMICA (uuidEtapa, codigo, TipoSolicitudAcademica_uuid) VALUES
('b3034d80-46ee-4fad-8364-0c45fbd0afee', 'PENDIENTE_FIRMA_ESTUDIANTE', NULL);

INSERT IGNORE INTO ETAPA_ETIQUETA_ROL (Etapa_uuid, rol, etiqueta) VALUES
('b3034d80-46ee-4fad-8364-0c45fbd0afee', 'ESTUDIANTE', 'En gestión');

UPDATE ETAPA_ETIQUETA_ROL SET etiqueta = 'En gestión' WHERE Etapa_uuid = 'b3034d80-46ee-4fad-8364-0c45fbd0afee' AND rol = 'ESTUDIANTE';

INSERT IGNORE INTO TIPO_ANEXO_ACADEMICO (uuidTipoAnexoAcademico, TipoSolicitudAcademica_uuid, nombre, formatosPermitidos, obligatorio) VALUES
('afbc10a7-ff6c-4972-a710-a1f022e37f79', '3b6dccc9-ff78-48ef-8e66-05fb9932e302', 'Formato firmado por el estudiante', 'pdf', 0),
('a2b477a3-b542-4ecb-868a-e19069690c96', '7b3d4347-3525-4ab7-a2b4-723b1ced80c8', 'Formato firmado por el estudiante', 'pdf', 0),
('a9a4dd9e-c0df-441d-9562-c5fda346aa54', '459cde47-f3a3-4a74-9b44-1063a516326c', 'Formato firmado por el estudiante', 'pdf', 0),
('af35a439-4bdd-4b7a-8b14-409860ae5729', '3b6dccc9-ff78-48ef-8e66-05fb9932e302', 'Formato firmado por el Decano', 'pdf', 0),
('4e424980-063a-4b23-bf0f-e29b3ee30bec', '7b3d4347-3525-4ab7-a2b4-723b1ced80c8', 'Formato firmado por el Decano', 'pdf', 0),
('b90e833c-562e-462f-a1aa-6909f605ac6d', '459cde47-f3a3-4a74-9b44-1063a516326c', 'Formato firmado por el Decano', 'pdf', 0);

UPDATE TIPO_ANEXO_ACADEMICO SET obligatorio = 0, vigente = 0 WHERE uuidTipoAnexoAcademico IN ('ee145c67-0f6e-4971-84b7-42679747ed49', '3cba6336-4de6-492b-838d-602f28a9efbe');

SELECT e.codigo, r.rol, r.etiqueta FROM ETAPA_ETIQUETA_ROL r JOIN ETAPA_SOLICITUD_ACADEMICA e ON e.uuidEtapa = r.Etapa_uuid WHERE e.codigo = 'PENDIENTE_FIRMA_ESTUDIANTE';
SELECT t.nombre, a.nombre, a.obligatorio, a.vigente FROM TIPO_ANEXO_ACADEMICO a JOIN TIPO_SOLICITUD_ACADEMICA t ON t.uuidTipoSolicitudAcademica = a.TipoSolicitudAcademica_uuid WHERE a.nombre LIKE 'Formato firmado%' ORDER BY t.nombre, a.nombre;
