SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS PROGRAMA_ACADEMICO (
    uuidProgramaAcademico        VARCHAR(100) NOT NULL,
    nombre                       VARCHAR(100) NOT NULL,
    sigla                        VARCHAR(10)  NOT NULL,
    ultimoConsecutivoAsignatura  INT          NOT NULL DEFAULT 0,
    PRIMARY KEY (uuidProgramaAcademico),
    UNIQUE KEY uk_programa_nombre (nombre),
    UNIQUE KEY uk_programa_sigla (sigla)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT IGNORE INTO PROGRAMA_ACADEMICO (uuidProgramaAcademico, nombre, sigla, ultimoConsecutivoAsignatura) VALUES
('14299e0f-8da8-4c73-ad2b-b61e0c52b7f5', 'Ingeniería Electrónica y Telecomunicaciones', 'IET', 0),
('5d73d656-2504-44cc-ab6e-5cd10b500abf', 'Ingeniería de Sistemas', 'ISI', 0),
('8606cf01-c111-4aef-92cd-a111cb7b7a38', 'Ingeniería en Automática Industrial', 'IAI', 0),
('7ebedec6-aeab-403b-8a97-9312061bd4b7', 'Tecnología en Telemática', 'TTE', 0),
('d21ec0bf-cee1-4a06-8ae7-00477e88cfe6', 'Ingeniería en Inteligencia Artificial y Ciencia de Datos', 'IAC', 0);

SET @isi = '5d73d656-2504-44cc-ab6e-5cd10b500abf';

SET @sql = (SELECT IF(COUNT(*) = 0,
    'ALTER TABLE ESTUDIANTE ADD COLUMN ProgramaAcademico_uuid VARCHAR(100) NULL',
    'DO 0')
    FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'ESTUDIANTE' AND column_name = 'ProgramaAcademico_uuid');
PREPARE sentencia FROM @sql;
EXECUTE sentencia;
DEALLOCATE PREPARE sentencia;

SET @sql = (SELECT IF(COUNT(*) = 1,
    'UPDATE ESTUDIANTE e LEFT JOIN PROGRAMA_ACADEMICO p ON p.nombre = TRIM(e.programaAcademico) SET e.ProgramaAcademico_uuid = COALESCE(p.uuidProgramaAcademico, @isi) WHERE e.ProgramaAcademico_uuid IS NULL OR e.ProgramaAcademico_uuid NOT IN (SELECT uuidProgramaAcademico FROM PROGRAMA_ACADEMICO)',
    'DO 0')
    FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'ESTUDIANTE' AND column_name = 'programaAcademico');
PREPARE sentencia FROM @sql;
EXECUTE sentencia;
DEALLOCATE PREPARE sentencia;

UPDATE ESTUDIANTE SET ProgramaAcademico_uuid = @isi
WHERE ProgramaAcademico_uuid IS NULL
   OR ProgramaAcademico_uuid NOT IN (SELECT uuidProgramaAcademico FROM PROGRAMA_ACADEMICO);

ALTER TABLE ESTUDIANTE MODIFY ProgramaAcademico_uuid VARCHAR(100) NOT NULL;

SET @sql = (SELECT IF(COUNT(*) = 0,
    'ALTER TABLE ESTUDIANTE ADD CONSTRAINT fk_estudiante_programa FOREIGN KEY (ProgramaAcademico_uuid) REFERENCES PROGRAMA_ACADEMICO (uuidProgramaAcademico)',
    'DO 0')
    FROM information_schema.table_constraints
    WHERE table_schema = DATABASE() AND table_name = 'ESTUDIANTE' AND constraint_name = 'fk_estudiante_programa');
PREPARE sentencia FROM @sql;
EXECUTE sentencia;
DEALLOCATE PREPARE sentencia;

SET @sql = (SELECT IF(COUNT(*) = 1,
    'ALTER TABLE ESTUDIANTE DROP COLUMN programaAcademico',
    'DO 0')
    FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'ESTUDIANTE' AND column_name = 'programaAcademico');
PREPARE sentencia FROM @sql;
EXECUTE sentencia;
DEALLOCATE PREPARE sentencia;

SET @sql = (SELECT IF(COUNT(*) = 0,
    'ALTER TABLE ASIGNATURA ADD COLUMN ProgramaAcademico_uuid VARCHAR(100) NULL',
    'DO 0')
    FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'ASIGNATURA' AND column_name = 'ProgramaAcademico_uuid');
PREPARE sentencia FROM @sql;
EXECUTE sentencia;
DEALLOCATE PREPARE sentencia;

DROP TEMPORARY TABLE IF EXISTS migracion_asignatura;

CREATE TEMPORARY TABLE migracion_asignatura AS
SELECT uuidAsignatura,
       ROW_NUMBER() OVER (ORDER BY codigoAsignatura, uuidAsignatura) AS posicion
FROM ASIGNATURA
WHERE ProgramaAcademico_uuid IS NULL
   OR ProgramaAcademico_uuid NOT IN (SELECT uuidProgramaAcademico FROM PROGRAMA_ACADEMICO);

SET @base = (SELECT CAST(GREATEST(
    (SELECT ultimoConsecutivoAsignatura FROM PROGRAMA_ACADEMICO WHERE uuidProgramaAcademico = @isi),
    COALESCE((SELECT MAX(CAST(SUBSTRING(codigoAsignatura, 5) AS UNSIGNED)) FROM ASIGNATURA
              WHERE ProgramaAcademico_uuid = @isi AND codigoAsignatura REGEXP '^ISI-[0-9]+$'), 0)) AS UNSIGNED));

UPDATE ASIGNATURA a JOIN migracion_asignatura m ON m.uuidAsignatura = a.uuidAsignatura
SET a.codigoAsignatura = CONCAT('MIG-', a.uuidAsignatura);

UPDATE ASIGNATURA a JOIN migracion_asignatura m ON m.uuidAsignatura = a.uuidAsignatura
SET a.ProgramaAcademico_uuid = @isi,
    a.codigoAsignatura = CONCAT('ISI-', LPAD(CAST(m.posicion + @base AS CHAR), GREATEST(3, LENGTH(CAST(m.posicion + @base AS CHAR))), '0'));

DROP TEMPORARY TABLE IF EXISTS migracion_asignatura;

ALTER TABLE ASIGNATURA MODIFY ProgramaAcademico_uuid VARCHAR(100) NOT NULL;

SET @sql = (SELECT IF(COUNT(*) = 0,
    'ALTER TABLE ASIGNATURA ADD CONSTRAINT fk_asignatura_programa FOREIGN KEY (ProgramaAcademico_uuid) REFERENCES PROGRAMA_ACADEMICO (uuidProgramaAcademico)',
    'DO 0')
    FROM information_schema.table_constraints
    WHERE table_schema = DATABASE() AND table_name = 'ASIGNATURA' AND constraint_name = 'fk_asignatura_programa');
PREPARE sentencia FROM @sql;
EXECUTE sentencia;
DEALLOCATE PREPARE sentencia;

UPDATE PROGRAMA_ACADEMICO p
SET p.ultimoConsecutivoAsignatura = GREATEST(p.ultimoConsecutivoAsignatura, COALESCE((
    SELECT MAX(CAST(SUBSTRING(a.codigoAsignatura, LENGTH(p.sigla) + 2) AS UNSIGNED))
    FROM ASIGNATURA a
    WHERE a.ProgramaAcademico_uuid = p.uuidProgramaAcademico
      AND a.codigoAsignatura REGEXP CONCAT('^', p.sigla, '-[0-9]+$')), 0));
