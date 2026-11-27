SET NAMES utf8mb4;

INSERT IGNORE INTO ETAPA_SOLICITUD_ACADEMICA (uuidEtapa, codigo, TipoSolicitudAcademica_uuid) VALUES
('5ce8cbcf-59de-40f0-90d2-345ff0f69419', 'EN_REVISION_CONSEJO', '7b3d4347-3525-4ab7-a2b4-723b1ced80c8'),
('88d34d62-6ba0-4815-b3c2-5e5a516b5ad4', 'APROBADA_POR_CONSEJO', '7b3d4347-3525-4ab7-a2b4-723b1ced80c8'),
('386500e7-2fc6-44bb-b76d-9a5e24a3a1ae', 'NO_APROBADA_POR_CONSEJO', '7b3d4347-3525-4ab7-a2b4-723b1ced80c8');

INSERT IGNORE INTO ETAPA_ETIQUETA_ROL (Etapa_uuid, rol, etiqueta) VALUES
('5ce8cbcf-59de-40f0-90d2-345ff0f69419', 'ESTUDIANTE', 'En trámite'),
('5ce8cbcf-59de-40f0-90d2-345ff0f69419', 'FUNCIONARIO', 'En Consejo'),
('5ce8cbcf-59de-40f0-90d2-345ff0f69419', 'DECANO', 'En Consejo'),
('88d34d62-6ba0-4815-b3c2-5e5a516b5ad4', 'ESTUDIANTE', 'En trámite'),
('88d34d62-6ba0-4815-b3c2-5e5a516b5ad4', 'FUNCIONARIO', 'Pendiente Notificar'),
('88d34d62-6ba0-4815-b3c2-5e5a516b5ad4', 'DECANO', 'Respondida'),
('386500e7-2fc6-44bb-b76d-9a5e24a3a1ae', 'ESTUDIANTE', 'En trámite'),
('386500e7-2fc6-44bb-b76d-9a5e24a3a1ae', 'FUNCIONARIO', 'Pendiente Notificar'),
('386500e7-2fc6-44bb-b76d-9a5e24a3a1ae', 'DECANO', 'Respondida');

SELECT e.codigo, r.rol, r.etiqueta FROM ETAPA_ETIQUETA_ROL r JOIN ETAPA_SOLICITUD_ACADEMICA e ON e.uuidEtapa = r.Etapa_uuid WHERE e.codigo LIKE '%CONSEJO' ORDER BY e.codigo, r.rol;
