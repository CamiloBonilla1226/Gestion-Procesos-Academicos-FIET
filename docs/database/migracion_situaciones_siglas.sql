SET NAMES utf8mb4;

SET @tiene_ambito = (SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'SITUACION_ACADEMICA_ASIGNATURA' AND column_name = 'ambito');

SET @sql = IF(@tiene_ambito = 0,
    'ALTER TABLE SITUACION_ACADEMICA_ASIGNATURA ADD COLUMN ambito ENUM(''CANCELACION'', ''MATRICULA'') NULL',
    'DO 0');
PREPARE sentencia FROM @sql;
EXECUTE sentencia;
DEALLOCATE PREPARE sentencia;

START TRANSACTION;

UPDATE SITUACION_ACADEMICA_ASIGNATURA SET ambito = 'MATRICULA'
WHERE uuidSituacionAcademica IN ('62aa0705-f878-408e-8111-78691c1a871a', 'fa14cee3-6e4f-4178-955c-ba73312e08cb', 'a6077d43-d5fc-4e8d-9424-18c549515c24');
UPDATE SITUACION_ACADEMICA_ASIGNATURA SET ambito = NULL WHERE uuidSituacionAcademica = '6eec86b1-53da-4c65-baa9-96bf67669c13';
INSERT IGNORE INTO SITUACION_ACADEMICA_ASIGNATURA (uuidSituacionAcademica, codigo, nombre, ambito) VALUES
('01e928be-eaa1-49f2-9361-c01474105a33', 'R1R', 'Recursada por primera vez', 'MATRICULA'),
('73e1a062-b426-4f52-9cd9-e884f9f65df3', 'C1', 'No vista', 'CANCELACION'),
('a36ac5d2-2c4f-4a23-a212-e1cb2e54fc1b', 'C2', 'Perdida', 'CANCELACION');

SELECT
    (SELECT COUNT(*) FROM ASIGNATURA_SOLICITUD_ACADEMICA a JOIN SITUACION_ACADEMICA_ASIGNATURA s ON s.uuidSituacionAcademica = a.SituacionMatricula_uuid
        WHERE s.ambito IS NULL OR s.ambito <> 'MATRICULA') AS situacion_asignatura_a_null,
    (SELECT COUNT(*) FROM ASIGNATURA_SOLICITUD_ACADEMICA a JOIN SITUACION_ACADEMICA_ASIGNATURA s ON s.uuidSituacionAcademica = a.SituacionCancelar_uuid
        WHERE s.ambito IS NULL OR s.ambito <> 'CANCELACION') AS situacion_cancelar_a_null;

UPDATE ASIGNATURA_SOLICITUD_ACADEMICA a
JOIN SITUACION_ACADEMICA_ASIGNATURA s ON s.uuidSituacionAcademica = a.SituacionMatricula_uuid
SET a.SituacionMatricula_uuid = NULL
WHERE s.ambito IS NULL OR s.ambito <> 'MATRICULA';

UPDATE ASIGNATURA_SOLICITUD_ACADEMICA a
JOIN SITUACION_ACADEMICA_ASIGNATURA s ON s.uuidSituacionAcademica = a.SituacionCancelar_uuid
SET a.SituacionCancelar_uuid = NULL
WHERE s.ambito IS NULL OR s.ambito <> 'CANCELACION';

UPDATE PROGRAMA_ACADEMICO SET sigla = 'PIET' WHERE uuidProgramaAcademico = '14299e0f-8da8-4c73-ad2b-b61e0c52b7f5';
UPDATE PROGRAMA_ACADEMICO SET sigla = 'PIS' WHERE uuidProgramaAcademico = '5d73d656-2504-44cc-ab6e-5cd10b500abf';
UPDATE PROGRAMA_ACADEMICO SET sigla = 'PIAI' WHERE uuidProgramaAcademico = '8606cf01-c111-4aef-92cd-a111cb7b7a38';
UPDATE PROGRAMA_ACADEMICO SET sigla = 'TTM' WHERE uuidProgramaAcademico = '7ebedec6-aeab-403b-8a97-9312061bd4b7';
UPDATE PROGRAMA_ACADEMICO SET sigla = 'PIAC' WHERE uuidProgramaAcademico = 'd21ec0bf-cee1-4a06-8ae7-00477e88cfe6';

COMMIT;

SELECT codigo, nombre, ambito FROM SITUACION_ACADEMICA_ASIGNATURA ORDER BY codigo;
SELECT nombre, sigla, ultimoConsecutivoAsignatura FROM PROGRAMA_ACADEMICO ORDER BY nombre;
