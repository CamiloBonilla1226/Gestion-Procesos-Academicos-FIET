SET NAMES utf8mb4;

UPDATE ETAPA_ETIQUETA_ROL SET etiqueta = 'No aprobada' WHERE etiqueta = 'Rechazada';

UPDATE ETAPA_ETIQUETA_ROL SET etiqueta = 'No aprobada por el Decano' WHERE etiqueta = 'Rechazada por el Decano';

UPDATE NOTIFICACION_DOCENTE SET titulo = REPLACE(titulo, 'Examen supletorio rechazado,', 'Examen supletorio no aprobado,') WHERE titulo LIKE 'Examen supletorio rechazado,%';

SELECT e.codigo, r.rol, r.etiqueta FROM ETAPA_ETIQUETA_ROL r JOIN ETAPA_SOLICITUD_ACADEMICA e ON e.uuidEtapa = r.Etapa_uuid WHERE e.codigo IN ('RECHAZADA', 'RECHAZADA_POR_DECANO') ORDER BY e.codigo, r.rol;
