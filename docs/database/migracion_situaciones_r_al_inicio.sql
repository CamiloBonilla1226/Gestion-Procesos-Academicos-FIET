SET NAMES utf8mb4;

START TRANSACTION;

UPDATE SITUACION_ACADEMICA_ASIGNATURA SET nombre = '(R1) Cursada por primera vez' WHERE uuidSituacionAcademica = '01e928be-eaa1-49f2-9361-c01474105a33';
UPDATE SITUACION_ACADEMICA_ASIGNATURA SET nombre = '(R2) Cursada por segunda vez' WHERE uuidSituacionAcademica = 'fa14cee3-6e4f-4178-955c-ba73312e08cb';
UPDATE SITUACION_ACADEMICA_ASIGNATURA SET nombre = '(R3) Cursada por tercera vez' WHERE uuidSituacionAcademica = 'a6077d43-d5fc-4e8d-9424-18c549515c24';

COMMIT;

SELECT codigo, nombre, ambito FROM SITUACION_ACADEMICA_ASIGNATURA ORDER BY ambito, codigo;
