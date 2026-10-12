SET NAMES utf8mb4;

SET @tiene_dependencia = (SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'FUNCIONARIO_ACADEMICO' AND column_name = 'dependencia');
SET @tiene_cargo = (SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'FUNCIONARIO_ACADEMICO' AND column_name = 'cargo');

SET @sql = IF(@tiene_dependencia = 1 AND @tiene_cargo = 0,
    'ALTER TABLE FUNCIONARIO_ACADEMICO RENAME COLUMN dependencia TO cargo',
    'DO 0');
PREPARE sentencia FROM @sql;
EXECUTE sentencia;
DEALLOCATE PREPARE sentencia;

SET @sql = IF(@tiene_dependencia = 1 AND @tiene_cargo = 1,
    'UPDATE FUNCIONARIO_ACADEMICO SET cargo = dependencia WHERE cargo IS NULL OR TRIM(cargo) = ''''',
    'DO 0');
PREPARE sentencia FROM @sql;
EXECUTE sentencia;
DEALLOCATE PREPARE sentencia;

SET @sql = IF(@tiene_dependencia = 1 AND @tiene_cargo = 1,
    'ALTER TABLE FUNCIONARIO_ACADEMICO DROP COLUMN dependencia',
    'DO 0');
PREPARE sentencia FROM @sql;
EXECUTE sentencia;
DEALLOCATE PREPARE sentencia;

ALTER TABLE FUNCIONARIO_ACADEMICO MODIFY cargo VARCHAR(100) NOT NULL;

UPDATE ESTUDIANTE SET facultad = 'FIET' WHERE facultad IS NULL OR facultad <> 'FIET';

ALTER TABLE ASIGNATURA_SOLICITUD_ACADEMICA
    MODIFY numeroFaltas VARCHAR(20) NULL,
    MODIFY nota VARCHAR(20) NULL;

UPDATE SITUACION_ACADEMICA_ASIGNATURA SET nombre = 'No cursada' WHERE codigo = 'R0';
UPDATE SITUACION_ACADEMICA_ASIGNATURA SET nombre = 'Cursada por primera vez' WHERE codigo = 'R1';
UPDATE SITUACION_ACADEMICA_ASIGNATURA SET nombre = 'Cursada por segunda vez' WHERE codigo = 'R2';
UPDATE SITUACION_ACADEMICA_ASIGNATURA SET nombre = 'Cursada por tercera vez' WHERE codigo = 'R3';

UPDATE ETAPA_ETIQUETA_ROL r
JOIN ETAPA_SOLICITUD_ACADEMICA e ON e.uuidEtapa = r.Etapa_uuid
SET r.etiqueta = 'Aprobada por el Decano'
WHERE r.rol = 'FUNCIONARIO' AND e.codigo = 'APROBADA_POR_DECANO';

UPDATE ETAPA_ETIQUETA_ROL r
JOIN ETAPA_SOLICITUD_ACADEMICA e ON e.uuidEtapa = r.Etapa_uuid
SET r.etiqueta = 'Rechazada por el Decano'
WHERE r.rol = 'FUNCIONARIO' AND e.codigo = 'RECHAZADA_POR_DECANO';
