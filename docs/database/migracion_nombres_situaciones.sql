SET NAMES utf8mb4;

START TRANSACTION;

UPDATE SITUACION_ACADEMICA_ASIGNATURA SET nombre = 'Cursada por primera vez (R1)' WHERE uuidSituacionAcademica = '01e928be-eaa1-49f2-9361-c01474105a33';
UPDATE SITUACION_ACADEMICA_ASIGNATURA SET nombre = 'Cursada por segunda vez (R2)' WHERE uuidSituacionAcademica = 'fa14cee3-6e4f-4178-955c-ba73312e08cb';
UPDATE SITUACION_ACADEMICA_ASIGNATURA SET nombre = 'Cursada por tercera vez (R3)' WHERE uuidSituacionAcademica = 'a6077d43-d5fc-4e8d-9424-18c549515c24';
UPDATE SITUACION_ACADEMICA_ASIGNATURA SET nombre = 'No cursada' WHERE uuidSituacionAcademica = '73e1a062-b426-4f52-9cd9-e884f9f65df3';
UPDATE SITUACION_ACADEMICA_ASIGNATURA SET nombre = 'Como perdida' WHERE uuidSituacionAcademica = 'a36ac5d2-2c4f-4a23-a212-e1cb2e54fc1b';

UPDATE NOTIFICACION_RESOLUCION SET decision = 'niega la' WHERE decision = 'rechaza la' AND estado IN ('PROGRAMADA', 'FALLIDA');

COMMIT;

SELECT codigo, nombre, ambito FROM SITUACION_ACADEMICA_ASIGNATURA ORDER BY ambito, codigo;
SELECT estado, decision, COUNT(*) AS filas FROM NOTIFICACION_RESOLUCION GROUP BY estado, decision;
