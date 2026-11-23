SET NAMES utf8mb4;

START TRANSACTION;

UPDATE NOTIFICACION_RESOLUCION
SET textoComunicacion = REPLACE(textoComunicacion, '{numero}/178', '8.4.2-4.10/{numero}')
WHERE estado IN ('PROGRAMADA', 'FALLIDA') AND textoComunicacion LIKE '%{numero}/178%';

COMMIT;

SELECT SolicitudAcademica_uuid, estado, numeroResolucion, anioResolucion
FROM NOTIFICACION_RESOLUCION
WHERE estado IN ('PROGRAMADA', 'FALLIDA');
